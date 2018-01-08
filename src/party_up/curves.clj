(ns party-up.curves
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.core.async :as async]
            [incanter.core :as incanter]
            [incanter.stats :as stats]
            [incanter.charts :as charts]
            [incanter.io :as io]))


(defn position-on-segment [position [start-value end-value]]
  (+ (* (- 1 position) start-value)
     (* position end-value)))


(defn de-casteljau-algorithm [position points]
  (let [new-points (map (partial position-on-segment position)
                        (partition 2 1 points))]
    (if (= 1 (count new-points))
      (first new-points)
      (recur position new-points))))


(defn bezier [points]
  ;; TODO we could handle single points by duplicating them as start and end
  (assert (<= 2 (count points)) "Bezier requires a minimum of two points")

  (fn [position]
    ;; TODO is rounding with int the best move?
    (int (de-casteljau-algorithm position points))))


(defn view [curves]
  (let [curves (if (coll? curves) curves [curves])]
    (let [plot (charts/function-plot
                (first curves) 0.0 1.0 :x-label "time" :y-label "value")]
      (-> (if (pos? (count (rest curves)))
            (reduce #(charts/add-function %1 %2 0.0 1.0) plot (rest curves))
            plot)
          (charts/set-theme :dark)
          incanter/view))))


(defn flip [curve-fns]
  (if (coll? curve-fns)
    (map flip curve-fns)
    (fn [position]
      (curve-fns (- 1 position)))))


(defn invert
  ([curve-fns] (invert 255 curve-fns))
  ([max-value curve-fns]
   (if (coll? curve-fns)
     (map invert curve-fns)
     (fn [position]
       (- max-value (curve-fns position))))))


(defn ^:private get-relative-index [position num-items]
  (min (int (* position num-items))
       (dec num-items)))


(defn combine [& curve-fns]
  ;; allows both single and nested fn's
  (let [curve-fns (flatten curve-fns)]
    (fn [position]
      (let [fn-index (get-relative-index position (count curve-fns))
            curve-fn (nth curve-fns fn-index)
            position-per (/ 1 (count curve-fns))
            curve-position (/ (- position (* fn-index position-per))
                              position-per) ]
        (curve-fn curve-position)))))


(defn duplicate
  ([curve-fns] (duplicate 2 curve-fns))
  ([number curve-fns]
   (if (coll? curve-fns)
     (map (partial duplicate number) curve-fns)
     (combine (replicate number curve-fns)))))


(defn append [modifier-fn curve-fns]
  (if (coll? curve-fns)
    (map (partial append modifier-fn) curve-fns)
    (combine curve-fns (modifier-fn curve-fns))))


(defn poly [points]
  (combine (map bezier (partition 2 1 points))))


(defn point [value]
  (poly [value value]))


(defn bpm
  ([beats-per-minute] (bpm beats-per-minute 1))
  ([beats-per-minute beats] (->> beats-per-minute (/ 60) (* 1000) (* beats))))


(defn slice
  ([end curve-fn] (slice 0 end curve-fn))
  ([start end curve-fn]
   (let [length (- end start)]
     (fn [position]
       (curve-fn (-> position (* length) (+ start)))))))


;; TODO rename or alias chunk?
(defn beats [number curve-fn]
  (->> number
       inc
       range
       (partition 2 1)
       (map (partial map (partial * (/ 1 number))))
       (map (fn [[start end]] (slice start end curve-fn)))))


(defn track [& curve-fns]
  (flatten curve-fns))


;; TODO syncing fns


(defn stretch
  ([tracks] (stretch (apply max (map count tracks)) tracks))
  ;; TODO need to flatten and handle single tracks
  ([length tracks]
   (->> tracks
        (map combine)
        (map (partial beats length)))))


(defn truncate [tracks]
  (let [length (apply min (map count tracks))]
    (map (partial take length) tracks)))


(defn greatest-common-denominator
  [a b]
  (if (zero? b) a (recur b, (mod a b))))


(defn least-common-multiple
  [a b]
  (/ (* a b) (greatest-common-denominator a b)))


(defn unsync [tracks]
  (let [multiple (reduce least-common-multiple (map count tracks))]
    (for [track tracks]
         (let [number (/ multiple (count track))]
           (beats multiple (duplicate number (combine track)))))))


(defn fill [tracks]
  (let [length (apply max (map count tracks))]
    (for [track tracks]
         (let [number (- length (count track))]
           (concat track (replicate number (point ((last track) 1))))))))


;; TODO play


(defn get-values [tracks _time]
  ((apply juxt (map combine tracks)) _time))


(defn start-play-loop [duration handler]
  (let [start-time (tc/to-long (t/now))]
    (async/go-loop []
          (let [current-time (tc/to-long (t/now))
                elapsed (- current-time start-time)
                position (min 1 (/ elapsed duration))]
            (handler position)
            (when (< position 1)
              ;; this should be handled in universe anyway?
              (async/<! (async/timeout 92))
              (recur))))))


(defn play [handler tracks params]
  (let [duration (bpm (:bpm params) (max (map count tracks)))
        tracked-handler (fn [_time]
                          (apply handler (get-values tracks _time)))]
    (start-play-loop duration tracked-handler)))
