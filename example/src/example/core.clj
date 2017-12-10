(ns example.core
  (:require [party-up.universe :as uni]
            [party-up.curves :as curves]
            [party-up.devices :refer :all]
            [party-up.devices.min-wash :refer [->MinWash]]))


(def my-universe (uni/universe "/dev/ttyUSB0"))
(def my-min-wash (->MinWash my-universe 0))


(pan my-min-wash 0)
(pan my-min-wash 255)


(def my-min-spot (->MinWash my-universe 13))


(def min-spot-gobo (channel-handler my-min-spot 12))


(min-spot-gobo 0)
(min-spot-gobo 80)


(pan my-min-wash 255)

;; set the initial state
(doto my-min-wash
      (pan 0)
      (tilt 0)
      (brightness 100)
      (red 200)
      (green 0)
      (blue 200))


(doto my-min-spot
      (pan 0)
      (tilt 127)
      (brightness 100)
      (red 0)
      (green 200)
      (blue 200))


(defn dual-devices [min-spot-cmds min-wash-cmds]
  (doseq [[cmd value] (partition 2 min-spot-cmds)]
    (cmd my-min-spot value))
  (doseq [[cmd value] (partition 2 min-wash-cmds)]
    (cmd my-min-wash value)))




(doseq [_ (range 1)]
  (dual-devices [pan 0 tilt 127]
                [pan 0 tilt 127])

  (Thread/sleep 4000)

  (dual-devices [pan 0 tilt 15]
                [pan 0 tilt 0])

  (Thread/sleep 4000)

  (dual-devices [pan 40 tilt 15]
                [pan 0 tilt 127])

  (Thread/sleep 4000)

  (dual-devices [pan 0 tilt 127]
                [pan 190 tilt 0])

  (Thread/sleep 4000)

  (strobe my-min-wash 200)
  (doto my-min-spot
        (pan 40)
        (tilt 15))

  (Thread/sleep 4000)

  (strobe my-min-spot 200)

  (Thread/sleep 4000)

  (dual-devices [pan 0 tilt 15]
                [pan 0 tilt 0])

  (Thread/sleep 4000)

  (Thread/sleep 4000)

  (strobe my-min-spot 1)
  (strobe my-min-wash 1)

  (dual-devices [brightness 100]
                [brightness 100])

  (println "DONE")
)



;; (uni/stop my-universe)
