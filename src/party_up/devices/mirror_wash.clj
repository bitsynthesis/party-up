(ns party-up.devices.mirror-wash
  (:require [party-up.universe :as uni]
            [quil.core :as q]
            [quil.middleware :as qm]))


(defn nested-lists-to-vectors [lists]
  (if (coll? lists)
    (mapv nested-lists-to-vectors lists)
    lists))


(def mirror-wash-state-lookup
  (->> (range 192)
       (partition 3)
       (partition 8)
       nested-lists-to-vectors))


(defn get-state-value [state index offset]
  (if (coll? index)
    (map #(get-state-value state % offset) index)
    (nth state (+ index offset))))


(defn get-mirror-wash-state-value
  ([state location] (get-mirror-wash-state-value state location 0))
  ([state location offset]
   (let [index (get-in mirror-wash-state-lookup location)]
     (get-state-value state index offset))))


;; TODO extract panel / arrangement to generic matrix ns


(defn panel [_universe address]
  {:state (:state _universe)
   :address address})


(def dummy-panel
  {:state (atom (vec (replicate 512 0)))
   :address 0})


(defn arrangement [rows]
  (assert (apply = (map count rows)) "All rows must be the same length.")
  (apply (partial map vector) rows))


;; TODO don't hardcode 8x8 panel size
(defn get-panel-and-location [_arrangement [x y]]
  (let [row (nth _arrangement (int (/ x 8)))
        panel (nth row (int (/ y 8)))
        relative-x (mod x 8)
        relative-y (mod y 8)]
    [panel [relative-x relative-y]]))


;; TODO don't require rgb
(defn get-value [_arrangement [x y rgb]]
  (let [[panel [panel-x panel-y]] (get-panel-and-location _arrangement [x y])]
    (get-mirror-wash-state-value @(:state panel)
                                 [panel-x panel-y rgb]
                                 (:address panel))))


;; TODO don't require rgb
(defn set-value [_arrangement [x y rgb] value]
  (let [[panel [panel-x panel-y]] (get-panel-and-location _arrangement [x y])
        index (+ (get-in mirror-wash-state-lookup [panel-x panel-y rgb])
                 (:address panel))]
    (uni/set-state panel [index value])))


(defn get-dimensions [_arrangement]
  (let [columns (-> _arrangement count (* 8))
        rows (-> _arrangement first count (* 8))]
    [columns rows]))


(defn get-arrangement-matrix [_arrangement]
  (let [[columns rows] (get-dimensions _arrangement)]
    (nested-lists-to-vectors
     (for [x (range columns)]
       (for [y (range rows)]
         (for [rgb (range 3)]
           (get-value _arrangement [x y rgb])))))))










;;
;;
;; (def grid [8 8])
;;
;;
;; (def row [[0 255 255]
;;           [255 0 0]
;;           [0 0 255]
;;           [255 255 0]
;;           [0 255 255]
;;           [255 0 0]
;;           [0 0 255]
;;           [255 255 0]])
;;
;;
;; (def matrix (vec (replicate 8 row)))


(def pixel-size 20)


(defn setup [_arrangement]
  (q/frame-rate 30)
  (q/color-mode :rgb)
  ; setup function returns initial state.
  {:arrangement _arrangement})


(defn update-state [state]
  state)


(defn draw-state [state]
  ; clear the sketch by filling it with light-grey color
  (q/background 50 50 50)
  (let [[columns rows] (get-dimensions (:arrangement state))]
    (doseq [x (range columns)
            y (range rows)]

      (let [[r g b] (map #(get-value (:arrangement state) [x y %])
                         (range 3))]
        (q/fill r g b)
        (q/rect (* x pixel-size) (* y pixel-size) pixel-size pixel-size)))))


(defn monitor [_arrangement]
  (q/defsketch first-quil
    :size (map (partial * pixel-size) (get-dimensions _arrangement))
    :setup (partial setup _arrangement)
    :update update-state
    :draw draw-state
    :features []
    :middleware [qm/fun-mode]))


;;


;; (defn apply-shader [_arrangement shader position]
;;   (let [[columns rows] (get-dimensions _arrangement)]
;;     (doseq [x (range columns)
;;             y (range rows)]
;;       (let [[r g b] (shader [x y] position)]
;;         (set-value _arrangement [x y 0] r)
;;         (set-value _arrangement [x y 1] g)
;;         (set-value _arrangement [x y 2] b)))))
;;
;;
;; (defn bars [[x y] position]
;;   (let [red (-> x (* 10) (+ (* 5 position)) (Math/cos) (Math/abs) (* 200) int)
;;         green 0 #_(-> [x y] (euclidean-distance [7 7]) (Math/cos) (* 10) int (min 200))
;;         blue (-> y (* 10) (+ (* 10 position)) (Math/sin) (Math/abs) (* 200) int)]
;;     [red green blue]))
;;
;;
;; (def my-universe (uni/universe "/dev/ttyDUMMY"))
;;
;; (def my-arrangement (arrangement [[(panel my-universe 0)]
;;                                   [(panel my-universe 192)]]))
;;
;; (monitor my-arrangement)
;;
;;
;; (def loopin (atom true))
;;
;; (future
;;  (while @loopin
;;         (doseq [i (range 100)]
;;           (let [position (/ i 100)]
;;             (apply-shader my-arrangement bars position)
;;             (Thread/sleep 50)))))


;; (do
;;   (uni/blackout my-universe)
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
;;   ;; red
;;   (uni/set-state my-universe [0 200 3 200 6 200 9 200 12 200 15 200 18 200 21 200
;;                               24 200
;;                               192 200
;;                               381 200
;;                               ]))