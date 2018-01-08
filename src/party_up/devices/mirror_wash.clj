(ns party-up.devices.mirror-wash
  (:require [party-up.devices :refer [defdevice Panel]]
            [party-up.universe :as uni]
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


(def dummy-panel
  {:state (atom (vec (replicate 512 0)))
   :address 0})


(defn arrangement [rows]
  (assert (apply = (map count rows)) "All rows must be the same length.")
  (apply (partial mapv vector) rows))


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
    (get-mirror-wash-state-value @(:state (:universe panel))
                                 [panel-x panel-y rgb]
                                 (:address panel))))


;; TODO don't require rgb
(defn set-value [_arrangement [x y rgb] value]
  (let [[panel [panel-x panel-y]] (get-panel-and-location _arrangement [x y])
        index (+ (get-in mirror-wash-state-lookup [panel-x panel-y rgb])
                 (:address panel))]
    (uni/set-state (:universe panel) [index value])))


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


(defdevice MirrorWash
  Panel
  {:get-matrix (fn [_])
   :set-matrix (fn [_ _])})

;;


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


;; TODO provide shader functions access to previous arrangement matrix
(defn apply-shader [_arrangement shader position]
  (let [[columns rows] (get-dimensions _arrangement)
        new-matrix (nested-lists-to-vectors
                    (for [x (range columns)]
                      (for [y (range rows)]
                        (shader [x y] position))))]
    ;; TODO provide a better means for updating a whole arrangement
    (doseq [x (range columns)
            y (range rows)
            :let [[r g b] (get-in new-matrix [x y])]]
      (set-value _arrangement [x y 0] r)
      (set-value _arrangement [x y 1] g)
      (set-value _arrangement [x y 2] b))))


(defn restrict-range
  "Limit the range of an integer value."
  [value minimum maximum]
  (-> value
      (min maximum)
      (max minimum)))


(defn ^:private in-range
  [[minimum maximum] value]
  (and (<= minimum value)
       (< value maximum)))


(defn hsl-to-rgb
  "|---|---------|-----------------------|
   | h | 0 - 360 | hue in degrees        |
   | s | 0 - 100 | saturation in percent |
   | l | 0 - 100 | lightness in percent  |

   http://www.rapidtables.com/convert/color/hsl-to-rgb.htm

   Convert HSL color to RGB."
  [h s l]
  (let [h_ (restrict-range h 0 360)
        s_ (/ (restrict-range s 0 100) 100)
        l_ (/ (restrict-range l 0 100) 100)
        c (* s_ (- 1 (Math/abs (float (- (* 2 l_) 1)))))
        x (* c (- 1 (Math/abs (float (- (mod (/ h_ 60) 2)  1)))))
        m (- l_ (/ c 2))
        rgb_ (condp in-range h_
               [0 60]    [c x 0]
               [60 120]  [x c 0]
               [120 180] [0 c x]
               [180 240] [0 x c]
               [240 300] [x 0 c]
               [300 360] [c 0 x])]
    (mapv #(-> % (+ m) (* 255) Math/ceil int)
          rgb_)))


(defn rgb-to-hsl [r g b]
  (let [r_ (/ r 255)
        g_ (/ g 255)
        b_ (/ b 255)
        c-min (min r_ g_ b_)
        c-max (max r_ g_ b_)
        delta (- c-max c-min)
        l (int (* 100 (/ (+ c-min c-max) 2)))
        s (if (zero? delta)
            0
            (if (< 0.5 l)
              (int (* 100 (/ delta (- 2 c-max c-min))))
              (int (* 100 (/ delta (+ c-max c-min))))))
        h (if (zero? delta)
            0
            (condp = c-max
              r_ (int (* 360 (/ (/ (- g_ b_) delta) 6)))
              g_ (int (* 360 (+ 1/3 (/ (/ (- b_ r_) 6) delta))))
              b_ (int (* 360 (+ 2/3 (/ (/ (- r_ g_) 6) delta))))))]
    [h s l]))


;; (rgb-to-hsl 255 0 0)
;; (rgb-to-hsl 255 255 0)
;; (rgb-to-hsl 0 0 255)


;; (defn bars [[x y] position]
;;   (let [red (-> x (* 10) (+ (* 5 position)) (Math/cos) (Math/abs) (* 200) int)
;;         green 0
;;         blue (-> y (* 10) (+ (* 10 position)) (Math/sin) (Math/abs) (* 200) int)]
;;     [red green blue]))
;;
;;
;; (def my-uni1 (uni/universe "/dev/ttyDUMMY"))
;; (def my-uni2 (uni/universe "/dev/ttyDUMMY"))
;; (def my-uni3 (uni/universe "/dev/ttyDUMMY"))
;;
;; (def my-arrangement (arrangement [[(panel my-uni1 0) (panel my-uni2 0) (panel my-uni3 0)]
;;                                   [(panel my-uni1 192) (panel my-uni2 192) (panel my-uni3 192)]]))
;;
;; (monitor my-arrangement)
;;
;; (defn square [x]
;;   (* x x))
;;
;; (defn euclidean-distance [x y]
;;   (Math/sqrt (->> (map - y x) (map square) (reduce +))))
;;
;;
;; (defn circle [[x y] position]
;;   (let [red (-> [x y] (euclidean-distance [3 3]) (* 30) (* position) int (min 255))
;;         green 0
;;         blue 0]
;;     [red green blue]))
;;
;;
;; (def loopin (atom true))
;;
;; (future
;;  (while @loopin
;;         (doseq [i (range 100)]
;;           (let [position (/ i 100)]
;;             (apply-shader my-arrangement circle position)
;;             (Thread/sleep 50)))))
;;

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
