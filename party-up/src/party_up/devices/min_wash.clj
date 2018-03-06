(ns party-up.devices.min-wash
  (:require [clojure.math.numeric-tower :as math]
            [party-up.devices :refer :all]))


(defn ^:private min-wash-brightness [value]
  (let [min-value 8
        max-value 134
        value-range (- max-value min-value)]
    (if (#{0 255} value)
      value
      (-> value
          (/ 255)
          (* value-range)
          int
          (- value-range)
          math/abs
          (+ min-value)))))


(defn ^:private min-wash-strobe [value]
  (let [min-value 135
        max-value 239
        value-range (- max-value min-value)]
    (if (#{0 255} value)
      value
      (-> value
          (/ 255)
          (* value-range)
          int
          (+ min-value)))))


(def ^:private min-wash-color-presets
  {:no-function   0
   :rgbw          8
   :red           22
   :green         36
   :blue          50
   :white         64
   :cyan          78
   :magenta       92
   :yellow        106
   :yellow-green  120
   :light-blue    134
   :midnight-blue 148
   :purple        162
   :light-purple  176
   :pink          190
   :light-red     204
   :orange        218
   :color-chase   232})


(defdevice MinWash
  Brightness
  {:brightness      (channel 5 min-wash-brightness)}
  Color
  {:color           (channel 10)
   :color-speed     (channel 11)
   :red             (channel 6)
   :green           (channel 7)
   :blue            (channel 8)}
  Movement
  {:pan             (channel 0)
   :pan-fine        (channel 1)
   :tilt            (channel 2)
   :tilt-fine       (channel 3)
   :pan-tilt-speed  (channel 4 (comp math/abs (partial + -255)))}
  Strobe
  {:strobe          (channel 5 min-wash-strobe)})
