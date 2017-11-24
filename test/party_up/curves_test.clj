(ns party-up.curves-test
  (:require [clojure.test :refer [deftest is]]
            [party-up.curves :as curves]))


;; allow for simple evenly spaced control points
;; or finer grain control as a pct of total time with requirement that each
;; pct is greater than the point before


(defn test-curve-fn [curve-fn position-values]
  (doseq [[position value] (partition 2 position-values)]
    (is (= value (curve-fn position)))))


(deftest bezier-two-points
  (test-curve-fn
   (curves/bezier [100 200])
   [0.0 100
    0.1 110
    0.9 190
    1.0 200]))


(deftest bezier-multiple-linear-points
  (test-curve-fn
   (curves/bezier [100 200 300])
   [0.0 100
    0.1 120
    0.9 280
    1.0 300]))


(deftest bezier-multiple-nonlinear-points
  (test-curve-fn
   (curves/bezier [100 200 400 0])
   [0.00 100
    0.25 182
    0.33 206
    0.50 237
    0.66 227
    0.75 198
    1.00 0]))


(deftest flip-curve
  (test-curve-fn
   (curves/flip (curves/bezier [100 200 400 0]))
   [0.00 0
    0.25 198
    0.33 225
    0.50 237
    0.66 209
    0.75 182
    1.00 100]))


(deftest flip-two-curves
  (let [[curve-fn1 curve-fn2] (curves/flip [(curves/bezier [100 200 400 0])
                                            (curves/bezier [0 100])])]
    (test-curve-fn
     curve-fn1
     [0.00 0
      0.75 182
      1.00 100])
    (test-curve-fn
     curve-fn2
     [0.00 100
      0.75 25
      1.00 0])))


(deftest invert-curve
  (test-curve-fn
   (curves/invert (curves/bezier [100 200 400 0]))
   [0.00 155
    0.25 73
    0.33 49
    0.50 18
    0.66 28
    0.75 57
    1.00 255]))


(deftest invert-curve-with-thousand-max
  (test-curve-fn
   (curves/invert 1000 (curves/bezier [100 200 400 0]))
   [0.00 900
    0.25 818
    0.33 794
    0.50 763
    0.66 773
    0.75 802
    1.00 1000]))


(deftest invert-two-curves
  (let [[curve-fn1 curve-fn2] (curves/invert [(curves/bezier [100 200 400 0])
                                              (curves/bezier [0 100])])]
    (test-curve-fn
     curve-fn1
     [0.00 155
      0.75 57
      1.00 255])
    (test-curve-fn
     curve-fn2
     [0.00 255
      0.75 180
      1.00 155])))


(deftest combine-curves
  (test-curve-fn
   (curves/combine (curves/bezier [100 200])
                   (curves/bezier [200 300 500 100]))
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest combine-collection-of-curves
  (test-curve-fn
   (curves/combine [(curves/bezier [100 200])
                    (curves/bezier [200 300 500 100])])
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest combine-single-curves-and-collections
  (test-curve-fn
   (curves/combine (curves/bezier [100 200])
                   [(curves/bezier [200 300 500 100])])
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest duplicate-curve
  (test-curve-fn
   (curves/duplicate (curves/bezier [0 100]))
   [0.00000 0
    0.25000 50
    0.49999 99
    0.50000 0
    0.75000 50
    1.00000 100]))


(deftest duplicate-curve-four-times
  (test-curve-fn
   (curves/duplicate 4 (curves/bezier [0 100]))
   [0.00000 0
    0.24999 99
    0.25000 0
    0.49999 99
    0.50000 0
    0.74999 99
    0.75000 0
    1.00000 100]))


(deftest duplicate-two-curves
  (let [[curve-fn1 curve-fn2] (curves/duplicate [(curves/bezier [0 100])
                                                 (curves/bezier [100 0])])]
    (test-curve-fn
     curve-fn1
     [0.00000 0
      0.25000 50
      0.49999 99
      0.50000 0
      0.75000 50
      1.00000 100])
    (test-curve-fn
     curve-fn2
     [0.00000 100
      0.25000 50
      0.49999 0
      0.50000 100
      0.75000 50
      1.00000 0])))


(deftest append-flip-to-curve
  (test-curve-fn
   (curves/append curves/flip (curves/bezier [0 100]))
   [0.00 0
    0.25 50
    0.50 100
    0.75 50
    1.00 0]))


(deftest append-flip-to-two-curves
  (let [[curve-fn1 curve-fn2] (curves/append curves/flip
                               [(curves/bezier [0 100])
                                (curves/bezier [100 200 400 0])])]
    (test-curve-fn
     curve-fn1
     [0.00 0
      0.25 50
      0.50 100
      0.75 50
      1.00 0])
    (test-curve-fn
     curve-fn2
     [0.00 100
      0.25 237
      0.50 0
      0.75 237
      1.00 100])))


(deftest draw-linear-polygon
  (test-curve-fn
   (curves/poly [0 100 50 50 0])
   [0.000 0
    0.125 50
    0.250 100
    0.375 75
    0.500 50
    0.625 50
    0.750 50
    0.875 25
    1.000 0]))
