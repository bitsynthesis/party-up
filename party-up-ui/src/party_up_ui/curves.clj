(ns party-up-ui.curves
  (:require [incanter.core :as incanter]
            [incanter.charts :as charts]))


(defn view [curves]
  (let [curves (if (coll? curves) curves [curves])]
    (let [chart (charts/function-plot
                (first curves) 0.0 1.0 :x-label "time" :y-label "value")]
      (-> (if (pos? (count (rest curves)))
            (reduce #(charts/add-function %1 %2 0.0 1.0) chart (rest curves))
            chart)
          (charts/set-theme :dark)
          incanter/view)
      chart)))


;; see src/example/view.clj for refreshble implementation with jfreechart
