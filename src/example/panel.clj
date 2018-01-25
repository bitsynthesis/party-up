(ns example.panel
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [party-up.devices :refer [defdevice Panel] :as dvc]
            [party-up.devices.mirror-wash :as mw :refer [->MirrorWash]]
            [party-up.universe :as uni]
            [party-up.curves :refer [bpm beats combine get-values flip fill play poly track view] :as crv]))


(def uni1 (uni/universe "/dev/ttyUSB0"))
(def panel1 (->MirrorWash uni1 0))
(def panel2 (->MirrorWash uni1 192))

(def uni2 (uni/universe "/dev/ttyUSB1"))
(def panel3 (->MirrorWash uni2 0))
(def panel4 (->MirrorWash uni2 192))

(def uni3 (uni/universe "/dev/ttyUSB2"))
(def panel5 (->MirrorWash uni3 0))
(def panel6 (->MirrorWash uni3 192))

;; (def grid (mw/arrangement [[panel1 panel2]]))
;; (def grid6 (mw/arrangement [[panel1 panel2]
;;                             [panel3 panel4]
;;                             [panel5 panel6]]))


(def grid3 (mw/arrangement [[panel1]
                            [panel3]
                            [panel5]]))


(doseq [u [uni1 uni2 uni3]]
  (uni/blackout u)
  (uni/start u))

;; (doseq [u [uni1 uni2 uni3]]
;;   (uni/stop u))

;; (reset! (:state uni1) (vec (replicate 512 0)))
;; (reset! (:state uni1) (vec (replicate 512 250)))

;; (mw/monitor grid)
(mw/monitor grid3)


(defn checkers [location _]
  [(if (every? #(zero? (mod % 2)) location) 255 0)
   0
   127])


(def log (atom []))


(defn basic-rain-shader [_arrangement [x y] _]
  (let [h (if (zero? y)
            (-> (rand) (* 360) int)
            (first
             (mw/rgb-to-hsl
              (mw/get-value _arrangement [x (dec y) 0])
              (mw/get-value _arrangement [x (dec y) 1])
              (mw/get-value _arrangement [x (dec y) 2]))))
        s 100
        l (if (zero? h) 0 50)]
    (if (< 180 h 300)
      (mw/hsl-to-rgb h s l)
      (mw/hsl-to-rgb 0 0 0))))


;; (def loopin (atom true))
;;
;;
;; (future
;;  (while @loopin
;;   (mw/apply-shader grid3 (partial basic-rain-shader grid3) 1)
;;   (Thread/sleep 100)))



;; whoop whoop


(def ramp (poly [0 200]))


(def my-seq
  (crv/duplicate 4 (crv/fill [(track ramp ramp)
                              (track (poly [100 50]))
                              (track (poly [25 75]))])))


;; TODO duplicate tracks as well as curves


;; (do-stuff 200 200)



;; (play do-stuff my-seq {:bpm 60})


(defn blocking-play-loop [duration handler]
  (let [start-time (tc/to-long (t/now))]
    (loop []
          (let [current-time (tc/to-long (t/now))
                elapsed (- current-time start-time)
                position (min 1 (/ elapsed duration))]
            (handler position)
            (when (< position 1)
              ;; this should be handled in universe anyway?
              (Thread/sleep 50)
              (recur))))))


(defn do-stuff [hue saturation lightness]
  (let [[r g b] (mw/hsl-to-rgb hue (/ saturation 2) lightness)]
    (doseq [p [panel1 panel3 panel5]]
      (dvc/red p r)
      (dvc/green p g)
      (dvc/blue p b))))


(def my-seq
  (crv/unsync [(track (crv/bezier [0 120 720 0]))
               (track (crv/bezier [50 100 0 50]))
               (track
                (apply crv/combine
                       [(replicate 8 [(crv/point 50) (crv/point 75)])
                        (replicate 6 (crv/point 50))]))]))


(def v (view (map combine my-seq)))

(core/set-data v (map combine my-seq))

(require '[incanter.charts :as charts])

(charts/add-function v (crv/point 500) 0 500)
(charts/add-function v (crv/point 200) 0 300)
(.getDatasetCount (.getPlot v))
(.getDataset (.getPlot v) 1)
(.getRenderer (.getPlot v))


;; ;; FROM INCANTER
;; (defn- data-as-list
;;   "
;;   data-as-list [x data]
;;   If x is a collection, return it
;;   If x is a single value, and data is undefined, return x in vector
;;   If x is a single value, and data is defined, return ($ x data)
;;   "
;;   [x data]
;;   (if (coll? x)
;;     (to-list x)
;;     (if data
;;       (let [selected ($ x data)]
;;         (if (coll? selected)
;;           selected
;;           [selected]))
;;       [x])))

(import '[org.jfree.data.xy XYSeries XYSeriesCollection])


(let [plot (.getPlot v)
      f (partial * 100)
      i 0
      min-range 0
      max-range 1
      step-size (float (/ (- max-range min-range) 500)) ;; what is this 500?
      x (range min-range max-range step-size)
      y (map f x)
      label "FOOBAR"
      data-series (XYSeries. label) ;; args?
      data-set (XYSeriesCollection.)]
  (dorun
   (map (fn [_x _y]
            (if (and (not (nil? _x))
                     (not (nil? _y)))
              (.add data-series (double _x) (double _y))))
        x y))
  (.addSeries data-set data-series)
  (.setDataset plot i data-set))



;; ;; FROM INCANTER
;; (defmethod add-lines* org.jfree.data.xy.XYSeriesCollection
;;   ([chart x y & options]
;;      (let [opts (when options (apply assoc {} options))
;;            data (or (:data opts) $data)
;;            _x (data-as-list x data)
;;            _y (data-as-list y data)
;;            data-plot (.getPlot chart)
;;            n (.getDatasetCount data-plot)
;;            series-lab (or (:series-label opts) (format "%s, %s" 'x 'y))
;;            data-series (XYSeries. series-lab (:auto-sort opts true))
;;            points? (true? (:points opts))
;;            line-renderer (XYLineAndShapeRenderer. true points?)
;;            ;; data-set (.getDataset data-plot)
;;            data-set (XYSeriesCollection.)]
;;        (dorun
;;         (map (fn [x y]
;;                (if (and (not (nil? x))
;;                         (not (nil? y)))
;;                  (.add data-series (double x) (double y))))
;;              _x _y))
;;       (.addSeries data-set data-series)
;;       (doto data-plot
;;         (.setSeriesRenderingOrder org.jfree.chart.plot.SeriesRenderingOrder/FORWARD)
;;         (.setDatasetRenderingOrder org.jfree.chart.plot.DatasetRenderingOrder/FORWARD)
;;         (.setDataset n data-set)
;;         (.setRenderer n line-renderer))
;;       chart)))

;; ;; FROM INCANTER
;; (defn add-function*
;;   ([chart function min-range max-range & options]
;;     (let [opts (when options (apply assoc {} options))
;;            step-size (or (:step-size opts)
;;                          (float (/ (- max-range min-range) 500)))
;;            x (range-inclusive min-range max-range step-size)
;;            series-lab (or (:series-label opts)
;;                           (format "%s" 'function))]
;;        (add-lines chart x (map function x) :series-label series-lab))))



(defn view-clear [chart]
  (let [plot (.getPlot chart)]
    (doseq [i (range (.getDatasetCount plot))]
      (-> plot
          (.getDataset i)
          ;; .getSeries
          .removeAllSeries
          ;; first
          ;; .clear
          ))))

(doseq [f (map combine my-seq)]
  (charts/add-function v f 0.0 1.0))

(view-clear v)

;; (require '[incanter.core :as core])



(doseq [_ (range 4)]
  (blocking-play-loop (bpm 120 4) #(apply do-stuff (crv/seek my-seq %))))


;; ;;
;; ;;
;; ;;
;; ;; ;; (reset! (:state uni1) (vec (replicate 512 250)))
