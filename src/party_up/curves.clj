(ns party-up.curves
  (:require [incanter.core :as incanter]
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


(defn view-curves [curves]
  (let [plot (charts/function-plot
              (first curves) 0.0 1.0 :x-label "time" :y-label "value")]
    (-> (if (pos? (count (rest curves)))
          (reduce #(charts/add-function %1 %2 0.0 1.0) plot (rest curves))
          plot)
     (charts/set-theme :dark)
     incanter/view)))


(defn invert [curve-fn]
  (fn [position]
    (curve-fn (- 1 position))))


(defn- get-combined-fn-index [position num-fns]
  (min (int (* position num-fns))
       (dec num-fns)))


(defn combine [& curve-fns]
  (fn [position]
    (let [fn-index (get-combined-fn-index position (count curve-fns))
          curve-fn (nth curve-fns fn-index)
          position-per (/ 1 (count curve-fns))
          curve-position (/ (- position (* fn-index position-per))
                            position-per) ]
      (curve-fn curve-position))))
