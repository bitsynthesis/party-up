(ns party-up.curves-test
  (:require [clojure.test :refer [deftest is]]
            [party-up.curves :as curves]))


;; allow for simple evenly spaced control points
;; or finer grain control as a pct of total time with requirement that each
;; pct is greater than the point before


(deftest bezier-two-points
  (let [curve-fn (curves/bezier [100 200])]
    (is (= 100 (curve-fn 0)))
    (is (= 110 (curve-fn 0.1)))
    (is (= 190 (curve-fn 0.9)))
    (is (= 200 (curve-fn 1)))))


(deftest bezier-multiple-linear-points
  (let [curve-fn (curves/bezier [100 200 300])]
    (is (= 100 (curve-fn 0)))
    (is (= 120 (curve-fn 0.1)))
    (is (= 280 (curve-fn 0.9)))
    (is (= 300 (curve-fn 1)))))


(deftest bezier-multiple-nonlinear-points
  (let [curve-fn (curves/bezier [100 200 400 0])]
    (is (= 100 (curve-fn 0)))
    (is (= 182 (curve-fn 0.25)))
    (is (= 206 (curve-fn 0.33)))
    (is (= 237 (curve-fn 0.5)))
    (is (= 227 (curve-fn 0.66)))
    (is (= 198 (curve-fn 0.75)))
    (is (= 0 (curve-fn 1)))))


(deftest flip-curve
  (let [curve-fn (curves/flip (curves/bezier [100 200 400 0]))]
    (is (= 0 (curve-fn 0)))
    (is (= 198 (curve-fn 0.25)))
    (is (= 225 (curve-fn 0.33)))
    (is (= 237 (curve-fn 0.5)))
    (is (= 209 (curve-fn 0.66)))
    (is (= 182 (curve-fn 0.75)))
    (is (= 100 (curve-fn 1)))))


(deftest flip-two-curves
  (let [[curve-fn1 curve-fn2] (curves/flip [(curves/bezier [100 200 400 0])
                                            (curves/bezier [0 100])])]
    (is (= 0 (curve-fn1 0)))
    (is (= 182 (curve-fn1 0.75)))
    (is (= 100 (curve-fn1 1)))
    (is (= 100 (curve-fn2 0)))
    (is (= 25 (curve-fn2 0.75)))
    (is (= 0 (curve-fn2 1)))))


(deftest invert-curve
  (let [curve-fn (curves/invert (curves/bezier [100 200 400 0]))]
    (is (= 155 (curve-fn 0)))
    (is (= 73 (curve-fn 0.25)))
    (is (= 49 (curve-fn 0.33)))
    (is (= 18 (curve-fn 0.5)))
    (is (= 28 (curve-fn 0.66)))
    (is (= 57 (curve-fn 0.75)))
    (is (= 255 (curve-fn 1)))))


(deftest invert-two-curves
  (let [[curve-fn1 curve-fn2] (curves/invert [(curves/bezier [100 200 400 0])
                                              (curves/bezier [0 100])])]
    (is (= 155 (curve-fn1 0)))
    (is (= 57 (curve-fn1 0.75)))
    (is (= 255 (curve-fn1 1)))
    (is (= 255 (curve-fn2 0)))
    (is (= 180 (curve-fn2 0.75)))
    (is (= 155 (curve-fn2 1)))))


(deftest combine-curves
  (let [curve-fn (curves/combine (curves/bezier [100 200])
                                 (curves/bezier [200 300 500 100]))]
    (is (= 100 (curve-fn 0)))
    (is (= 150 (curve-fn 0.25)))
    (is (= 200 (curve-fn 0.5)))
    (is (= 100 (curve-fn 1)))))


(deftest combine-collection-of-curves
  (let [curve-fn (curves/combine [(curves/bezier [100 200])
                                  (curves/bezier [200 300 500 100])])]
    (is (= 100 (curve-fn 0)))
    (is (= 150 (curve-fn 0.25)))
    (is (= 200 (curve-fn 0.5)))
    (is (= 100 (curve-fn 1)))))


(deftest combine-single-curves-and-collections
  (let [curve-fn (curves/combine (curves/bezier [100 200])
                                 [(curves/bezier [200 300 500 100])])]
    (is (= 100 (curve-fn 0)))
    (is (= 150 (curve-fn 0.25)))
    (is (= 200 (curve-fn 0.5)))
    (is (= 100 (curve-fn 1)))))


(deftest duplicate-curve
  (let [curve-fn (curves/duplicate (curves/bezier [0 100]))]
    (is (= 0 (curve-fn 0)))
    (is (= 50 (curve-fn 0.25)))
    (is (= 99 (curve-fn 0.49999)))
    (is (= 0 (curve-fn 0.5)))
    (is (= 50 (curve-fn 0.75)))
    (is (= 100 (curve-fn 1)))))


(deftest duplicate-curve-four-times
  (let [curve-fn (curves/duplicate 4 (curves/bezier [0 100]))]
    (is (= 0 (curve-fn 0)))
    (is (= 99 (curve-fn 0.24999)))
    (is (= 0 (curve-fn 0.25)))
    (is (= 99 (curve-fn 0.49999)))
    (is (= 0 (curve-fn 0.5)))
    (is (= 99 (curve-fn 0.74999)))
    (is (= 0 (curve-fn 0.75)))
    (is (= 100 (curve-fn 1)))))


(deftest append-flip-to-curve
  (let [curve-fn (curves/append curves/flip (curves/bezier [0 100]))]
    (is (= 0 (curve-fn 0)))
    (is (= 50 (curve-fn 0.25)))
    (is (= 100 (curve-fn 0.5)))
    (is (= 50 (curve-fn 0.75)))
    (is (= 0 (curve-fn 1)))))


(deftest append-flip-to-two-curves
  (let [[curve-fn1 curve-fn2] (curves/append
                               curves/flip
                               [(curves/bezier [0 100])
                                (curves/bezier [100 200 400 0])])]
    (is (= 0 (curve-fn1 0)))
    (is (= 50 (curve-fn1 0.25)))
    (is (= 100 (curve-fn1 0.5)))
    (is (= 50 (curve-fn1 0.75)))
    (is (= 0 (curve-fn1 1)))
    (is (= 100 (curve-fn2 0)))
    (is (= 237 (curve-fn2 0.25)))
    (is (= 0 (curve-fn2 0.5)))
    (is (= 237 (curve-fn2 0.75)))
    (is (= 100 (curve-fn2 1)))))
