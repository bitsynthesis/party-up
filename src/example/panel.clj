(ns example.panel
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [party-up.devices :refer [defdevice Panel] :as dvc]
            [party-up.devices.mirror-wash :as mw :refer [->MirrorWash]]
            [party-up.universe :as uni]
            [party-up.curves :refer [bpm beats combine get-values flip fill play poly track view] :as crv]))


;; (def uni1 (uni/universe "/dev/ttyUSB0"))
;; (def panel1 (->MirrorWash uni1 0))
;; (def panel2 (->MirrorWash uni1 192))
;;
;; (def uni2 (uni/universe "/dev/ttyUSB1"))
;; (def panel3 (->MirrorWash uni2 0))
;; (def panel4 (->MirrorWash uni2 192))
;;
;; (def uni3 (uni/universe "/dev/ttyUSB2"))
;; (def panel5 (->MirrorWash uni3 0))
;; (def panel6 (->MirrorWash uni3 192))
;;
;; ;; (def grid (mw/arrangement [[panel1 panel2]]))
;; ;; (def grid6 (mw/arrangement [[panel1 panel2]
;; ;;                             [panel3 panel4]
;; ;;                             [panel5 panel6]]))
;;
;;
;; (def grid3 (mw/arrangement [[panel1]
;;                             [panel3]
;;                             [panel5]]))
;;
;;
;; (doseq [u [uni1 uni2 uni3]]
;;   (uni/blackout u)
;;   (uni/start u))
;;
;; ;; (doseq [u [uni1 uni2 uni3]]
;; ;;   (uni/stop u))
;;
;; ;; (reset! (:state uni1) (vec (replicate 512 0)))
;; ;; (reset! (:state uni1) (vec (replicate 512 250)))
;;
;; ;; (mw/monitor grid)
;; (mw/monitor grid3)
;;
;;
;; (defn checkers [location _]
;;   [(if (every? #(zero? (mod % 2)) location) 255 0)
;;    0
;;    127])
;;
;;
;; (def log (atom []))
;;
;;
;; (defn basic-rain-shader [_arrangement [x y] _]
;;   (let [h (if (zero? y)
;;             (-> (rand) (* 360) int)
;;             (first
;;              (mw/rgb-to-hsl
;;               (mw/get-value _arrangement [x (dec y) 0])
;;               (mw/get-value _arrangement [x (dec y) 1])
;;               (mw/get-value _arrangement [x (dec y) 2]))))
;;         s 100
;;         l (if (zero? h) 0 50)]
;;     (if (< 180 h 300)
;;       (mw/hsl-to-rgb h s l)
;;       (mw/hsl-to-rgb 0 0 0))))
;;
;;
;; ;; (def loopin (atom true))
;; ;;
;; ;;
;; ;; (future
;; ;;  (while @loopin
;; ;;   (mw/apply-shader grid3 (partial basic-rain-shader grid3) 1)
;; ;;   (Thread/sleep 100)))
;;
;;
;;
;; ;; whoop whoop
;;
;;
;; (def ramp (poly [0 200]))
;;
;;
;; (def my-seq (crv/unsync [(track (beats 3 ramp) (flip ramp))
;;                          (track (flip ramp))]))
;;
;;
;; (view (map combine my-seq))
;;
;;
;; (defn do-stuff [value1 value2]
;;   (dvc/red panel1 value1)
;;   (dvc/green panel3 value2))
;;
;;
;; (do-stuff 200 100)
;;
;;
;; ;; (play do-stuff my-seq {:bpm 60})
;; ;;
;; ;;
;; ;; (defn blocking-play-loop [duration handler]
;; ;;   (let [start-time (tc/to-long (t/now))]
;; ;;     (loop []
;; ;;           (let [current-time (tc/to-long (t/now))
;; ;;                 elapsed (- current-time start-time)
;; ;;                 position (min 1 (/ elapsed duration))]
;; ;;             (handler position)
;; ;;             (when (< position 1)
;; ;;               ;; this should be handled in universe anyway?
;; ;;               (Thread/sleep 50)
;; ;;               (recur))))))
;; ;;
;; ;; (get-values my-seq 0.1) (bpm 120 (count (first my-seq)))
;; ;; (defn rly-now [_time]
;; ;;   (apply do-stuff ((apply juxt (map combine my-seq)) _time)))
;; ;;
;; ;; (doseq [_ (range 4)]
;; ;;   (blocking-play-loop (bpm 120 4) rly-now))
;; ;;
;; ;;
;; ;;
;; ;; ;; (reset! (:state uni1) (vec (replicate 512 250)))
