(ns example.random
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.core.async :as async]
            [clojure.core.matrix :as matrix]
            [party-up.universe :as uni]
            [party-up.curves :as curves]
            [party-up.devices :refer :all]
            [party-up.devices.min-wash :refer [->MinWash]]))


;; some random sketch stuff


;; (def my-universe (uni/universe "/dev/ttyUSB0"))
;;
;;
;; (def my-min-wash (->MinWash my-universe 0))
;;
;;
;; (def mirror-rgb (->Device 0 my-universe))
;;
;;
;; (def pixel1 (partial (channel 0) mirror-rgb))
;;
;;
;; (pixel1 100)
;;
;; (def default-state (vec (replicate 192 0)))
;;
;; (def rainbow (map vector
;;                   (range 512)
;;                   (flatten (replicate 16 [255 0 0
;;                                           0 255 0
;;                                           0 0 255
;;                                           0 0 0]))))
;;
;; (defn square [x]
;;   (* x x))
;;
;; (defn euclidean-distance [x y]
;;   (Math/sqrt (->> (map - y x) (map square) (reduce +))))
;;
;;
;; (defn nested-lists-to-vectors [lists]
;;   (if (coll? lists)
;;     (mapv nested-lists-to-vectors lists)
;;     lists))
;;
;;
;; (def mirror-wash-state-lookup
;;   (->> (range 192)
;;        (partition 3)
;;        (partition 8)
;;        ;; (partition 4)
;;        ;; (partition 4)
;;        ;; (partition 2)
;;        ;; (map (partial apply map concat))
;;        ;; (apply concat)
;;        nested-lists-to-vectors))
;;
;;
;; (def mirror-wash-rgb-lookup
;;   (into {} (for [x (range 8) y (range 8) rgb (range 3)]
;;              [(get-in mirror-wash-state-lookup [x y rgb]) [x y rgb]])))
;;
;;
;; (defn get-mirror-wash-state-index [[x y rgb]]
;;   (get-in mirror-wash-state-lookup [x y rgb]))
;;
;;
;; (defn get-state-value [state index]
;;   (if (coll? index)
;;     (map (partial get-state-value state) index)
;;     (nth state index)))
;;
;;
;; (defn get-mirror-wash-state-value [state position]
;;   (let [index (get-in mirror-wash-state-lookup position)]
;;     (get-state-value state index)))
;;
;;
;; (defn recursive-zip [coll1 coll2]
;;   (if (and (coll? coll1) (coll? coll2))
;;     (map recursive-zip coll1 coll2)
;;     [coll1 coll2]))
;;
;;
;; (defn set-mirror-wash-state-value [state position value]
;;   (let [index (get-in mirror-wash-state-lookup position)
;;         index-value (flatten (recursive-zip index value))]
;;     (apply assoc state index-value)))
;;
;;
;; ;; TODO position naming is confusing! position should be used for point in time
;; (defn apply-shader [shader matrix state position-time]
;;   (let [index-values
;;         (for [x (range (count matrix))
;;               y (range (count (first matrix)))
;;               :let [index (get-in mirror-wash-state-lookup [x y])]]
;;           [index (shader [x y] position-time)])]
;;     (apply assoc (vec state) (flatten (map (partial apply map vector) index-values)))))
;;
;;
;; (defn bars [[x y] position]
;;   (let [red (-> x (* 10) (+ (* 5 position)) (Math/cos) (Math/abs) (* 200) int)
;;         green 0 #_(-> [x y] (euclidean-distance [7 7]) (Math/cos) (* 10) int (min 200))
;;         blue (-> y (* 10) (+ (* 10 position)) (Math/sin) (Math/abs) (* 200) int)]
;;     [red green blue]))
;;
;;
;; (defn update-bars [_universe position]
;;   (uni/set-state
;;    _universe
;;    (map-indexed (fn [i v] [i v])
;;                 (apply-shader bars
;;                               mirror-wash-state-lookup
;;                               default-state
;;                               position))))
;;
;;
;; (defn circle [[x y] position-time]
;;   (let [red (-> [x y] (euclidean-distance [3 3]) (* 30) (* position-time) int (min 255))
;;         green 0
;;         blue 0]
;;     [red green blue]))
;;
;; (do
;;   (uni/blackout my-universe)
;;
;;   (Thread/sleep 5000)
;;
;;   ;; green
;;   (uni/set-state my-universe [97 150 100 150 103 150 106 150
;;                               145 200 148 200 151 200 154 200])
;;
;;   ;; blue
;;   (uni/set-state my-universe [110 200 113 200 116 200 119 200
;;                               122 200 125 200 128 200 131 200
;;                               50 200
;;                               ])
;;
;;   (uni/set-state my-universe [0 200 3 200 6 200 9 200 12 200 15 200 18 200 21 200
;;                               24 200
;;                               ]))
;;
;; (update-bars my-universe 0.5)
;; (reset! (:state my-universe) (vec (replicate 512 0)))
;; (reset! (:state my-universe) (vec (replicate 512 250)))
;;
;;
;;
;; (doseq [_ (range 3)]
;;   (doseq [i (range 100)]
;;     (let [position (/ i 100)]
;;       (uni/set-state
;;        my-universe
;;        (map-indexed (fn [i v] [i v])
;;                     (apply-shader circle mirror-wash-state-lookup default-state position)))
;;       (Thread/sleep 100))))
;;
;;
;;
;; (def loopin (atom true))
;;
;; (doseq [i (range 100)]
;;           (let [position (/ i 100)]
;;             (update-bars my-universe position)
;;             (Thread/sleep 100)))
;;
;; (future
;;  (while @loopin
;;         (doseq [i (range 100)]
;;           (let [position (/ i 100)]
;;             (update-bars my-universe position)
;;             (Thread/sleep 50)))))
;;
;;
;;
;;
;; (set-mirror-wash-state-value (into [] (range 192)) [2 4] [0 255 0])
;;
;;
;; (get-mirror-wash-state-value (range 192) [2 4])
;;
;;
;;
;; (mirror-wash-state-to-rgb-matrix default-state)
;;
;;
;;
;; (count (state-to-rgb-matrix (range 48) [4 4]))
;;
;;
;; (defn bars [state position]
;;   (let [matrix (state-to-rgb-matrix state dimensions)]
;;     (for [y (range (count matrix))]
;;       (for [x (range (count (nth matrix y)))]
;;         (let [red (min 255 (int (Math/sin (* 20 (euclidean-distance [x y] [0 0])))))
;;               green 0
;;               blue 0]
;;           [red green blue])))))
;;
;; ;; TODO need to take in the universe state, can convert to rows / columns if needed
;; ;; but probably needs to be cached with partial or something
;; (defn mirror-wash [_universe position]
;;   (let [new-matrix (bars @(:state _universe) position)
;;         flat-matrix (flatten new-matrix)
;;         address-values (map vector
;;                             (range (count flat-matrix))
;;                             flat-matrix)]
;;     (uni/set-state _universe address-values)))
;;
;; (def m (state-to-rgb-matrix default-matrix (range 192)))
;;
;;
;; (doseq [on-off (flatten (replicate 20 [true false]))]
;;   (let [value (if on-off 0 255)]
;;     (uni/set-state my-universe (map vector (range 512) (replicate 512 value)))
;;     (Thread/sleep 92)))
;;
;; (uni/set-state my-universe (map vector (range 512) (replicate 512 255)))
;;
;; (uni/set-state my-universe rainbow)
;;
;; (def reps 25)
;; (mirror-wash my-universe (/ reps 1))
;; (loop [position 1]
;;   (mirror-wash my-universe (/ reps position))
;;   (when (< position reps)
;;     (Thread/sleep 100)
;;     (recur (inc position))))
;;
;; (uni/set-state my-universe new-state)
;;
;; (uni/set-state my-universe rainbow)
;;
;; (uni/set-state my-universe [0 255 1 0 2 0
;;                             3 0 4 255 5 0
;;                             6 0 7 0 8 255
;;                             9 0 10 0 11 0])
;;
;;
;; (uni/set-state my-universe (map vector (range 512) (replicate 512 255)))
;;
;;
;; ;; (uni/start my-universe)
;;
;; ;; (brightness my-min-wash 100)
;; ;; (pan my-min-wash 255)
;; ;; (pan my-min-wash 50)
;; ;; (tilt my-min-wash 255)
;; ;; (tilt my-min-wash 140)
;; ;; (red my-min-wash 255)
;; ;; (green my-min-wash 180)
;; ;; (green my-min-wash 0)
;; ;; (blue my-min-wash 60)
;; ;; (strobe my-min-wash 255)
;; ;; (strobe my-min-wash 254)
;;
;; ;; (uni/set-state my-universe 0 255)
;; ;; (async/go (async/>! @(:channel my-universe) [0 255]))
;; ;; (async/go (async/>! @(:channel my-universe) [0 127]))
;;
;;
;; ;; (:port my-universe)
;; ;; (.closePort @(:port my-universe))
;; ;; (.status @(:port my-universe))
;;
;;
;; (pan my-min-wash 0)
;; (tilt my-min-wash 0)
;; (do (pan my-min-wash 123) (tilt my-min-wash 134))
;; (pan my-min-wash 255)
;;
;; (def duration 3000)
;;
;; (def debug (atom []))
;;
;;
;; (defn play [duration handler]
;;   (let [start-time (tc/to-long (t/now))]
;;     (async/go-loop []
;;           (let [current-time (tc/to-long (t/now))
;;                 elapsed (- current-time start-time)
;;                 position (min 1 (/ elapsed duration))]
;;             (handler position)
;;             (when (< position 1)
;;               ;; this should be handled in universe anyway?
;;               (async/<! (async/timeout 92))
;;               (recur))))))
;;
;;
;; (doto my-min-wash
;;       (brightness 255)
;;       (red 255)
;;       (green 255)
;;       (blue 255))
;;
;;
;; (def my-curve (curves/bezier [0 25]))
;; (def my-curve2 (curves/bezier [25 35 45 255]))
;; (def my-curve3 (curves/bezier [255 30 15 7 4 2 1 0]))
;;
;; (def super-curve
;;   (let [half-curve (curves/combine my-curve my-curve2 my-curve3)]
;;     (curves/combine half-curve (curves/invert half-curve))))
;;
;; (def green-curve (curves/bezier [0 10 20 30 512 0]))
;; (def blue-curve (curves/bezier [0 0 0 0 255 0]))
;;
;;
;; (def base (replicate 3 (curves/combine (curves/invert my-curve3) my-curve3)))
;; (def combined-base
;;   (curves/combine my-curve base super-curve (curves/invert my-curve)))
;;
;; ;; (curves/view-curves [combined-base])
;;
;;
;; (play 20000 (comp (partial brightness my-min-wash) combined-base))
;;
;;
;; ;; can't repeat cause async
;; (play 5000 (juxt (comp (partial pan my-min-wash) super-curve)
;;                  (comp (partial blue my-min-wash) my-curve3)
;;                  (comp (partial green my-min-wash) my-curve2)
;;                  (comp (partial tilt my-min-wash) super-curve)))
;;
;;
;; ;; (tilt my-min-wash 0)
;; ;; (tilt my-min-wash 255)
;; ;; (pan-tilt-speed my-min-wash 0)
;; ;; (pan-tilt-speed my-min-wash 255)
;; ;; (color my-min-wash 0)
;; ;; (color-speed my-min-wash 0)
;;
;; (uni/stop my-universe)
;;
;;
;; ;; ;; # BEGIN GENERATOR THOUGHTS
;; ;;
;; ;; ;; TODO need time param
;; ;; ;; TODO ability to loop
;; ;; (def my-generator
;; ;;   (uni/generator [pan my-min-wash my-curve1]
;; ;;                 [tilt my-min-wash my-curve2]))
;; ;;
;; ;;
;; ;; (defgenerator my-generator [position]
;; ;;   (pan my-min-wash (my-curve1 position))
;; ;;   (tilt my-min-wash (my-curve2 position)))
;; ;;
;; ;;
;; ;; ;; TODO combine should maybe take a single collection
;; ;; (apply
;; ;;  curves/combine
;; ;;  (replicate 4 (juxt (comp (partial pan my-min-wash) my-curve1)
;; ;;                     (comp (partial tilt my-min-wash) my-curve2))))
;; ;;
;; ;;
;; ;; ;; echo function which repeats but reducing values each time
;; ;; (defn duplicate [duplications & curves]
;; ;;   (apply curves/combine (replicate 4 (apply juxt curves))))
;; ;;
;; ;;
;; ;; (duplicate 4
;; ;;            #(pan my-min-wash (my-curve1 %))
;; ;;            #(tilt my-min-wash (my-curve2 %)))
;; ;;
;; ;;
;; ;;
;; ;;
;; ;; (def duration-ms 3000)
;; ;; (def repetitions 4)
;; ;;
;; ;;
;; ;; (uni/start-generator my-generator duration-ms repetitions)
;; ;; (uni/stop-generator my-generator)
;; ;;
;; ;;
;; ;;
;; ;; ;; # END
;;
;; ;; (doto my-min-wash
;; ;;       (pan 255)
;; ;;       (tilt 255)
;; ;;       (red 255)
;; ;;       (green 255)
;; ;;       (blue 255)
;; ;;       (brightness 255))
;;
;;
;; ;; (defdevice MinWash
;; ;;   Movement
;; ;;   {:pan       (channel 0)
;; ;;    :pan-fine  (channel 1)
;; ;;    :tilt      (channel 2 #(/ % 2))
;; ;;    :tilt-fine feature-missing}
;; ;;   Strobe
;; ;;   {:strobe feature-missing})
;;
;;
;; ;; TODO would be nice be able to dynamically creat records
;; ;; (def my-min-wash (uni/device MinWash my-universe 1))
;;
;;
;; ;; (def min-wash-pan (uni/device-function min-wash 0))
;; ;; (def min-wash-red (uni/device-function min-wash 6))
;; ;; (def min-wash-green (uni/device-function min-wash 7))
;; ;; (def min-wash-blue (uni/device-function min-wash 8))
;; ;; (def min-wash-tilt (uni/device-function min-wash 2))
;;
;; ;; (defn min-wash-brightness [level]
;; ;;   (let [device-fn (uni/device-function min-wash 5)]
;; ;;     (device-fn (-> (/ level 2)
;; ;;                    (- 127)
;; ;;                    math/abs
;; ;;                    (+ 7)))))
;;
;;
;; ;; (uni/set-state my-universe 6 200)
;; ;; (uni/update-universe my-universe)
;; ;;
;; ;;
;;
;; ;;
;; ;; (curves/view-curves [green-curve (curves/invert green-curve) super-curve])
;; ;;
;; ;; (doseq [_ (range 4)]
;; ;;   (doseq [i (range 101)
;; ;;           :let [position (/ i 100)]]
;; ;;     ;; (min-wash-brightness (super-curve position))
;; ;;     (min-wash-red (super-curve position))
;; ;;     (min-wash-green (green-curve position))
;; ;;     (min-wash-blue ((curves/invert green-curve) position))
;; ;;     (Thread/sleep 10)))
;; ;;
;;
;; ;; (view-curves [my-curve my-curve2])
;; ;; (view-curves [(invert my-curve2)])
;; ;; (view-curves [(combine (combine my-curve my-curve2 my-curve3)
;; ;;                        (invert (combine my-curve my-curve2 my-curve3)))])
;; ;;
;; ;; (view-curves [my-curve my-curve2 my-curve3])
;;
;;
;;
;; ;; generators... functions which produce a series of values to execute a
;; ;; pattern over time
;; ;; ... this should probably be its own lib, as it's the same shit in
;; ;; mx50-commander
;;
;;
;; ;; alt
;;
;;
;; ;; (defn min-wash-strobe [speed]
;; ;;   (set-state min-wash 1 speed))
