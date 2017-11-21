(ns party-up.example
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.core.async :as async]
            [party-up.core :as pu]
            [party-up.curves :as curves]
            [party-up.devices :refer :all]
            [party-up.devices.min-wash :refer [->MinWash]]))


(def my-universe (pu/universe "/dev/ttyUSB1"))


(pu/start-universe my-universe)


(def my-min-wash (->MinWash my-universe 0))

;; (brightness my-min-wash 100)
;; (pan my-min-wash 255)
;; (pan my-min-wash 50)
;; (tilt my-min-wash 255)
;; (tilt my-min-wash 140)
;; (red my-min-wash 255)
;; (green my-min-wash 180)
;; (green my-min-wash 0)
;; (blue my-min-wash 60)
;; (strobe my-min-wash 255)
;; (strobe my-min-wash 254)

;; (pu/set-state my-universe 0 255)
;; (async/go (async/>! @(:channel my-universe) [0 255]))
;; (async/go (async/>! @(:channel my-universe) [0 127]))


;; (:port my-universe)
;; (.closePort @(:port my-universe))
;; (.status @(:port my-universe))


(pan my-min-wash 0)
(tilt my-min-wash 0)
(do (pan my-min-wash 123) (tilt my-min-wash 134))
(pan my-min-wash 255)

(def duration 3000)

(def debug (atom []))


(defn play [duration handler]
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


(doto my-min-wash
      (brightness 255)
      (red 255)
      (green 255)
      (blue 255))


(play 20000 (comp (partial brightness my-min-wash) combined-base))


;; can't repeat cause async
(play 5000 (juxt (comp (partial pan my-min-wash) super-curve)
                 (comp (partial blue my-min-wash) my-curve3)
                 (comp (partial green my-min-wash) my-curve2)
                 (comp (partial tilt my-min-wash) super-curve)))

(curves/view-curves [(curves/combine (curves/invert my-curve3) my-curve3)])

(def base (replicate 3 (curves/combine (curves/invert my-curve3) my-curve3)))
(def combined-base
  (curves/combine my-curve base super-curve (curves/invert my-curve)))
(curves/view-curves
 [combined-base])

(combined-base 0.5)

;; (tilt my-min-wash 0)
;; (tilt my-min-wash 255)
;; (pan-tilt-speed my-min-wash 0)
;; (pan-tilt-speed my-min-wash 255)
;; (color my-min-wash 0)
;; (color-speed my-min-wash 0)

(pu/stop-universe my-universe)


;; ;; # BEGIN GENERATOR THOUGHTS
;;
;; ;; TODO need time param
;; ;; TODO ability to loop
;; (def my-generator
;;   (pu/generator [pan my-min-wash my-curve1]
;;                 [tilt my-min-wash my-curve2]))
;;
;;
;; (defgenerator my-generator [position]
;;   (pan my-min-wash (my-curve1 position))
;;   (tilt my-min-wash (my-curve2 position)))
;;
;;
;; ;; TODO combine should maybe take a single collection
;; (apply
;;  curves/combine
;;  (replicate 4 (juxt (comp (partial pan my-min-wash) my-curve1)
;;                     (comp (partial tilt my-min-wash) my-curve2))))
;;
;;
;; ;; echo function which repeats but reducing values each time
;; (defn duplicate [duplications & curves]
;;   (apply curves/combine (replicate 4 (apply juxt curves))))
;;
;;
;; (duplicate 4
;;            #(pan my-min-wash (my-curve1 %))
;;            #(tilt my-min-wash (my-curve2 %)))
;;
;;
;;
;;
;; (def duration-ms 3000)
;; (def repetitions 4)
;;
;;
;; (pu/start-generator my-generator duration-ms repetitions)
;; (pu/stop-generator my-generator)
;;
;;
;;
;; ;; # END

;; (doto my-min-wash
;;       (pan 255)
;;       (tilt 255)
;;       (red 255)
;;       (green 255)
;;       (blue 255)
;;       (brightness 255))


;; (defdevice MinWash
;;   Movement
;;   {:pan       (channel 0)
;;    :pan-fine  (channel 1)
;;    :tilt      (channel 2 #(/ % 2))
;;    :tilt-fine feature-missing}
;;   Strobe
;;   {:strobe feature-missing})


;; TODO would be nice be able to dynamically creat records
;; (def my-min-wash (pu/device MinWash my-universe 1))


;; (def min-wash-pan (pu/device-function min-wash 0))
;; (def min-wash-red (pu/device-function min-wash 6))
;; (def min-wash-green (pu/device-function min-wash 7))
;; (def min-wash-blue (pu/device-function min-wash 8))
;; (def min-wash-tilt (pu/device-function min-wash 2))

;; (defn min-wash-brightness [level]
;;   (let [device-fn (pu/device-function min-wash 5)]
;;     (device-fn (-> (/ level 2)
;;                    (- 127)
;;                    math/abs
;;                    (+ 7)))))


;; (pu/set-state my-universe 6 200)
;; (pu/update-universe my-universe)
;;
;;

;; (def my-curve (curves/bezier [0 25]))
;; (def my-curve2 (curves/bezier [25 35 45 255]))
;; (def my-curve3 (curves/bezier [255 30 15 7 4 2 1 0]))

;; (def super-curve
;;   (let [half-curve (curves/combine my-curve my-curve2 my-curve3)]
;;     (curves/combine half-curve (curves/invert half-curve))))

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



;; generators... functions which produce a series of values to execute a
;; pattern over time
;; ... this should probably be its own lib, as it's the same shit in
;; mx50-commander


;; alt


;; (defn min-wash-strobe [speed]
;;   (set-state min-wash 1 speed))
