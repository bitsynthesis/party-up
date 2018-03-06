(ns party-up.matrix
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.core.async :as async]
            [clojure.core.matrix :as matrix]
            [party-up.universe :as uni]
            [party-up.curves :as curves]
            [party-up.devices :refer :all]
            [party-up.devices.min-wash :refer [->MinWash]]
            [party-up.matrix-preview :as matrixp]))


;; (def default-state (vec (replicate 192 0)))
;;
;;
;; (defn square [x]
;;   (* x x))
;;
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
;;        nested-lists-to-vectors))
;;
;;
;; (def mirror-wash-rgb-lookup
;;   (into {} (for [x (range 8) y (range 8) rgb (range 3)]
;;              [(get-in mirror-wash-state-lookup [x y rgb]) [x y rgb]])))
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
;; (def my-matrix
;;   (for [x (range 8)
;;         y (range 8)]
;;     (get-mirror-wash-state-value my-state [x y])))
;;
;;
;; (def my-uni {:state (atom (vec (range 512)))})
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
;; (update-bars my-universe 0.5)
;; (reset! (:state my-universe) (vec (replicate 512 0)))
;; (reset! (:state my-universe) (vec (replicate 512 250)))
;;
;;
;; ;; (doseq [_ (range 3)]
;; ;;   (doseq [i (range 100)]
;; ;;     (let [position (/ i 100)]
;; ;;       (uni/set-state
;; ;;        my-universe
;; ;;        (map-indexed (fn [i v] [i v])
;; ;;                     (apply-shader circle mirror-wash-state-lookup default-state position)))
;; ;;       (Thread/sleep 100))))
;; ;;
;; ;;
;; ;;
;; ;; (def loopin (atom true))
;; ;;
;; ;; (doseq [i (range 100)]
;; ;;           (let [position (/ i 100)]
;; ;;             (update-bars my-universe position)
;; ;;             (Thread/sleep 100)))
;; ;;
;; ;; (future
;; ;;  (while @loopin
;; ;;         (doseq [i (range 100)]
;; ;;           (let [position (/ i 100)]
;; ;;             (update-bars my-universe position)
;; ;;             (Thread/sleep 50)))))
