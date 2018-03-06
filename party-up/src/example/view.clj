(ns example.view
  (:import (org.jfree.chart ChartFactory ChartFrame)
           (org.jfree.chart.plot PlotOrientation)
           (org.jfree.data.xy XYSeries XYSeriesCollection)))


;; TODO split out the data partitioning from data setting
(defn _create-dataset [_fn _name]
  (let [min-range 0
        max-range 1
        step-size (float (/ (- max-range min-range) 500))
        x (range min-range max-range step-size)
        y (map _fn x)
        series (XYSeries. _name)
        dataset (XYSeriesCollection.)]
    (dorun
     (map (fn [_x _y]
              (if (and (not (nil? _x))
                       (not (nil? _y)))
                (.add series (double _x) (double _y))))
          x y))
    (.addSeries dataset series)
    dataset))




(defn create-dataset
  "Create an empty dataset."
  []
  (XYSeriesCollection.))


(defn create-line
  "Add a series to a dataset."
  [dataset _name]
  (let [series (XYSeries. _name)]
    (.addSeries dataset series)
    series))


(defn create-window [chart]
  (let [window-title "Better Plot :P"
        width 500
        height 400
        frame (ChartFrame. window-title chart)]
    (doto frame
          (.setSize width height)
          (.setVisible true))))


(defn view-dataset
  "Create a chart and display it in a new window."
  [dataset]
  (let [show-legend true
        use-tooltips true
        generate-urls false
        chart (ChartFactory/createXYLineChart
               "my fancy chart"
               "time"
               "value"
               dataset
               PlotOrientation/VERTICAL
               show-legend
               use-tooltips
               generate-urls)]
    (create-window chart)
    chart))


(defn ^:private create-function-data
  "Calculate actual values for a function."
  [_fn]
  (let [min-range 0
        max-range 1
        step-size (float (/ (- max-range min-range) 500))
        x (range min-range max-range step-size)
        y (map _fn x)]
    (zipmap x y)))


(defn draw-function
  "Calculate actual values for a function and update the named series."
  [series _fn]
  (let [data (create-function-data _fn)]
    (.clear series)
    (dorun
     (map (fn [[x y]]
              (if (and (not (nil? x))
                       (not (nil? y)))
                (.add series (double x) (double y))))
          data))))


(def my-dataset (create-dataset))
(def my-window (view-dataset my-dataset))
(def my-line (create-line my-dataset 0))
(def my-line2 (create-line my-dataset 1))

(draw-function my-line (partial * 800))
(draw-function my-line2 (comp (partial + 200) (partial * -1) (partial * 200)))


;; create-dataset
;; create-line dataset _name
;; draw-function series _fn
;; view-dataset dataset


;; TODO update dataset


(defn update-dataset [chart curve-fn label]
  (let [data-set (create-dataset curve-fn label)]
    (.setDataset (.getPlot chart) i data-set)))


;;


(def my-data (create-dataset (partial * 250) "my sweet line"))


(def my-window (ChartFactory/createXYLineChart
               "my fancy ass chart"
               "bottom label"
               "side label"
               my-data
               PlotOrientation/VERTICAL
               true ;; Show Legend
               true ;; Use tooltips
               false ;; Configure chart to generate URLs?
               ))


(def my-plot (.getPlot my-window))


(.getDatasetCount my-plot)
(.getDataset my-plot)

(-> my-plot
    .getDataset
    .getSeriesCount)

(-> my-plot
    .getDataset
    (.getSeries "my sweet line")
    .clear)


(create-window my-window)









;; (let [plot (.getPlot v)
;;       f (partial * 100)
;;       i 0
;;       min-range 0
;;       max-range 1
;;       step-size (float (/ (- max-range min-range) 500)) ;; what is this 500?
;;       x (range min-range max-range step-size)
;;       y (map f x)
;;       label "FOOBAR"
;;       data-series (XYSeries. label) ;; args?
;;       data-set (XYSeriesCollection.)]
;;   (dorun
;;    (map (fn [_x _y]
;;             (if (and (not (nil? _x))
;;                      (not (nil? _y)))
;;               (.add data-series (double _x) (double _y))))
;;         x y))
;;   (.addSeries data-set data-series)
;;   (.setDataset plot i data-set))
