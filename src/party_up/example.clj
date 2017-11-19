(ns party-up.example
  (:require [party-up.core :as pu]
            [party-up.curves :as curves]
            [party-up.devices :refer :all]
            [clojure.math.numeric-tower :as math]))


(def my-universe (pu/universe "/dev/ttyUSB1"))


(def min-wash (pu/device my-universe 0))


(def min-wash-pan (pu/device-function min-wash 0))
(def min-wash-red (pu/device-function min-wash 6))
(def min-wash-green (pu/device-function min-wash 7))
(def min-wash-blue (pu/device-function min-wash 8))
(def min-wash-tilt (pu/device-function min-wash 2))

(defn min-wash-brightness [level]
  (let [device-fn (pu/device-function min-wash 5)]
    (device-fn (-> (/ level 2)
                   (- 127)
                   math/abs
                   (+ 7)))))


(pu/start-universe my-universe)

;; (min-wash-pan 195)
;; (min-wash-tilt 50)
;;
;; (min-wash-tilt 75)
;;
;; (do
;;   (min-wash-brightness 200)
;;   (min-wash-red 200)
;;   (min-wash-green 255)
;;   (min-wash-blue 255))
;;
;; (pu/set-state my-universe 6 200)
;; (pu/update-universe my-universe)
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
;; (curves/view-curves [green-curve (curves/invert green-curve) super-curve])
;;
;; (doseq [_ (range 4)]
;;   (doseq [i (range 101)
;;           :let [position (/ i 100)]]
;;     ;; (min-wash-brightness (super-curve position))
;;     (min-wash-red (super-curve position))
;;     (min-wash-green (green-curve position))
;;     (min-wash-blue ((curves/invert green-curve) position))
;;     (Thread/sleep 10)))
;;

;; (view-curves [my-curve my-curve2])
;; (view-curves [(invert my-curve2)])
;; (view-curves [(combine (combine my-curve my-curve2 my-curve3)
;;                        (invert (combine my-curve my-curve2 my-curve3)))])
;;
;; (view-curves [my-curve my-curve2 my-curve3])


(defn set-device-state [device channel value]
  (pu/set-state (:universe device)
                (+ channel (:starting-address device))
                value))


(defn channel
  ([number] (channel number identity))
  ([number modifier]
   (fn [device value]
     (let [device-fn (pu/device-function device number)]
       (device-fn (modifier value))))))


;; (defrecord MinWash [universe starting-address])
;;
;;
;; (extend MinWash
;;   Movement
;;   {:pan (device-channel 0)
;;    :pan-fine (device-channel 1)
;;    :tilt (device-channel 2 #(/ % 2))
;;    :tilt-fine (device-channel 3 #(/ % 2))})


(defdevice MinWash
  Movement
  {:pan       (channel 0)
   :pan-fine  (channel 1)
   :tilt      (channel 2 #(/ % 2))
   :tilt-fine feature-missing})


;; TODO would be nice be able to dynamically creat records
(def my-min-wash (pu/device MinWash my-universe 1))


(def my-min-wash (->MinWash my-universe 0))


(pan my-min-wash 255)
(pan my-min-wash 0)
(tilt my-min-wash 255)
(tilt my-min-wash 0)


;; generators... functions which produce a series of values to execute a
;; pattern over time
;; ... this should probably be its own lib, as it's the same shit in
;; mx50-commander


;; alt


;; (defn min-wash-strobe [speed]
;;   (set-state min-wash 1 speed))
