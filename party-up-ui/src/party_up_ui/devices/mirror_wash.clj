(ns party-up-ui.devices.mirror-wash
  (:require [party-up.devices.mirror-wash :as mw]
            [quil.core :as q]
            [quil.middleware :as qm]))


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
  (let [[columns rows] (mw/get-dimensions (:arrangement state))]
    (doseq [x (range columns)
            y (range rows)]

      (let [[r g b] (map #(mw/get-value (:arrangement state) [x y %])
                         (range 3))]
        (q/fill r g b)
        (q/rect (* x pixel-size) (* y pixel-size) pixel-size pixel-size)))))


(defn monitor [_arrangement]
  (q/defsketch first-quil
    :size (map (partial * pixel-size) (mw/get-dimensions _arrangement))
    :setup (partial setup _arrangement)
    :update update-state
    :draw draw-state
    :features []
    :middleware [qm/fun-mode]))
