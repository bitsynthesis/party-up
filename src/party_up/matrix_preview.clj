(ns party-up.matrix-preview
  (:require [quil.core :as q]
            [quil.middleware :as qm]))


(def pixel-size 20)


(def grid [8 8])


(def row [[0 255 255]
          [255 0 0]
          [0 0 255]
          [255 255 0]
          [0 255 255]
          [255 0 0]
          [0 0 255]
          [255 255 0]])


(def matrix (vec (replicate 8 row)))


(defn setup [_universe]
  (q/frame-rate 30)
  (q/color-mode :rgb)
  ; setup function returns initial state.
  {:matrix @(:state _universe)})


(defn update-state [state]
  state)


(defn draw-state [state]
  ; clear the sketch by filling it with light-grey color
  (q/background 220)
  (doseq [x (range (count (:matrix state)))
          y (range (count (first (:matrix state))))]
    (let [[r g b] (get-in (:matrix state) [x y])]
      (q/fill r g b)
      (q/rect (* x pixel-size) (* y pixel-size) pixel-size pixel-size))))


(defn monitor [_universe]
  (q/defsketch first-quil
    :size (map (partial * pixel-size) grid)
    :setup (partial setup _universe)
    :update update-state
    :draw draw-state
    :features []
    :middleware [qm/fun-mode]))
