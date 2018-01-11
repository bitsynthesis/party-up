(ns party-up.curves-test
  (:require [clojure.test :refer [deftest is]]
            [party-up.curves :as crv]))


;; TODO
;; allow for simple evenly spaced control points
;; or finer grain control as a pct of total time with requirement that each
;; pct is greater than the point before


(defn test-curve-fn [curve-fn position-values]
  (doseq [[position value] (partition 2 position-values)]
    (is (= value (curve-fn position)))))


(deftest bezier-two-points
  (test-curve-fn
   (crv/bezier [100 200])
   [0.0 100
    0.1 110
    0.9 190
    1.0 200]))


(deftest bezier-multiple-linear-points
  (test-curve-fn
   (crv/bezier [100 200 300])
   [0.0 100
    0.1 120
    0.9 280
    1.0 300]))


(deftest bezier-multiple-nonlinear-points
  (test-curve-fn
   (crv/bezier [100 200 400 0])
   [0.00 100
    0.25 182
    0.33 206
    0.50 237
    0.66 227
    0.75 198
    1.00 0]))


(deftest flip-curve
  (test-curve-fn
   (crv/flip (crv/bezier [100 200 400 0]))
   [0.00 0
    0.25 198
    0.33 225
    0.50 237
    0.66 209
    0.75 182
    1.00 100]))


(deftest flip-two-curves
  (let [[curve-fn1 curve-fn2] (crv/flip [(crv/bezier [100 200 400 0])
                                         (crv/bezier [0 100])])]
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
   (crv/invert (crv/bezier [100 200 400 0]))
   [0.00 155
    0.25 73
    0.33 49
    0.50 18
    0.66 28
    0.75 57
    1.00 255]))


(deftest invert-curve-with-thousand-max
  (test-curve-fn
   (crv/invert 1000 (crv/bezier [100 200 400 0]))
   [0.00 900
    0.25 818
    0.33 794
    0.50 763
    0.66 773
    0.75 802
    1.00 1000]))


(deftest invert-two-curves
  (let [[curve-fn1 curve-fn2] (crv/invert [(crv/bezier [100 200 400 0])
                                           (crv/bezier [0 100])])]
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
   (crv/combine (crv/bezier [100 200])
                (crv/bezier [200 300 500 100]))
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest combine-collection-of-curves
  (test-curve-fn
   (crv/combine [(crv/bezier [100 200])
                 (crv/bezier [200 300 500 100])])
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest combine-single-curves-and-collections
  (test-curve-fn
   (crv/combine (crv/bezier [100 200])
                [(crv/bezier [200 300 500 100])])
   [0.00 100
    0.25 150
    0.50 200
    1.00 100]))


(deftest duplicate-curve
  (test-curve-fn
   (crv/duplicate (crv/bezier [0 100]))
   [0.00000 0
    0.25000 50
    0.49999 99
    0.50000 0
    0.75000 50
    1.00000 100]))


(deftest duplicate-curve-four-times
  (test-curve-fn
   (crv/duplicate 4 (crv/bezier [0 100]))
   [0.00000 0
    0.24999 99
    0.25000 0
    0.49999 99
    0.50000 0
    0.74999 99
    0.75000 0
    1.00000 100]))


(deftest duplicate-two-curves
  (let [[curve-fn1 curve-fn2] (crv/duplicate [identity inc])]
    (test-curve-fn
     curve-fn1
     [0.00 0.0
      0.25 0.5
      0.50 0.0
      0.75 0.5
      1.00 1.0])
    (test-curve-fn
     curve-fn2
     [0.00 1.0
      0.25 1.5
      0.50 1.0
      0.75 1.5
      1.00 2.0])))


(deftest append-flip-to-curve
  (test-curve-fn
   (crv/append crv/flip identity)
   [0.00 0.0
    0.25 0.5
    0.50 1.0
    0.75 0.5
    1.00 0.0]))


(deftest append-flip-to-two-curves
  (let [[curve-fn1 curve-fn2] (crv/append crv/flip [identity inc])]
    (test-curve-fn
     curve-fn1
     [0.00 0.0
      0.25 0.5
      0.50 1.0
      0.75 0.5
      1.00 0.0])
    (test-curve-fn
     curve-fn2
     [0.00 1.0
      0.25 1.5
      0.50 2.0
      0.75 1.5
      1.00 1.0])))


(deftest draw-linear-polygon
  (test-curve-fn
   (crv/poly [0 100 50 50 0])
   [0.000 0
    0.125 50
    0.250 100
    0.375 75
    0.500 50
    0.625 50
    0.750 50
    0.875 25
    1.000 0]))


(deftest point
  (test-curve-fn
   (crv/point 123)
   [0.0 123
    0.1 123
    0.5 123
    0.9 123
    1.0 123]))


(deftest bpm-to-ms
  (is (= 600 (crv/bpm 100)))
  (is (= 500 (crv/bpm 120)))
  (is (= 2400 (crv/bpm 100 4)))
  (is (= 2000 (crv/bpm 120 4))))


(deftest slice-half
  (test-curve-fn
   (crv/slice 0.5 identity)
   [0.0 0.0
    0.5 0.25
    1.0 0.5]))


(deftest slice-end
  (test-curve-fn
   (crv/slice 0.9 1 identity)
   [0.0 0.9
    0.5 0.95
    1.0 1.0]))


(deftest slice-and-flip
  (test-curve-fn
   (crv/slice 1 0.9 identity)
   [0.0 1.0
    0.5 0.95
    1.0 0.9]))


(deftest beats
  (let [[curve-fn1 curve-fn2 curve-fn3 curve-fn4]
        (crv/beats 4 identity)]
    (test-curve-fn
     curve-fn1
     [0.0 0.0
      0.5 0.125
      1.0 0.25])
    (test-curve-fn
     curve-fn2
     [0.0 0.25
      0.5 0.375
      1.0 0.5])
    (test-curve-fn
     curve-fn3
     [0.0 0.5
      0.5 0.625
      1.0 0.75])
    (test-curve-fn
     curve-fn4
     [0.0 0.75
      0.5 0.875
      1.0 1.0])))


(deftest track
  (is (= [:a] (crv/track :a)))
  (is (= [:a :b :c] (crv/track :a :b :c)))
  (is (= [:a :b :c] (crv/track [:a :b :c])))
  (is (= [:a :b :c] (crv/track :a [:b :c]))))


(deftest stretch-single-track
  (let [stretched (crv/stretch 10 (crv/track identity))]
    (is (= 10 (count stretched)))
    (is (= 0.1 ((first stretched) 1.0)))
    (is (= 1.0 ((last stretched) 1.0)))))


(deftest stretch-multi-track
  (let [stretched (crv/stretch 10 [(crv/track identity)
                                   (crv/track identity identity)])
        [track1 track2] stretched]
    (is (= [10 10] (map count stretched)))

    (is (= 0.05 ((nth track1 0) 0.5)))
    (is (= 0.1 ((nth track1 0) 1.0)))
    (is (= 0.2 ((nth track1 1) 1.0)))
    (is (= 1.0 ((nth track1 9) 1.0)))

    (is (= 0.2 ((nth track2 0) 1.0)))
    (is (= 0.4 ((nth track2 1) 1.0)))
    (is (= 1.0 ((nth track2 9) 1.0)))))


(deftest stretch-multi-track-to-max
  (let [stretched (crv/stretch [(crv/track identity)
                                (crv/track identity identity)])
        [track1 track2] (map crv/combine stretched)]
    (is (= [2 2] (map count stretched)))

    (is (= 0.5 (track1 0.5)))
    (is (= 1.0 (track1 1.0)))

    (is (= 0.2 (track2 0.1)))
    (is (= 0.9 (track2 0.45)))
    (is (= 0.0 (track2 0.5)))
    (is (= 1.0 (track2 1.0)))))


(deftest stretch-single-track
  (let [stretched (crv/stretch 10 (crv/track identity))]
    (is (= 10 (count stretched)))
    (is (= 0.1 ((first stretched) 1.0)))
    (is (= 1.0 ((last stretched) 1.0)))))


;; TODO sync fns



