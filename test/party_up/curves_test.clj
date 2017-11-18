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


(deftest bezier-invert-curve
  (let [curve-fn (curves/invert (curves/bezier [100 200 400 0]))]
    (is (= 100 (curve-fn 1)))
    (is (= 182 (curve-fn 0.75)))
    (is (= 209 (curve-fn 0.66)))
    (is (= 237 (curve-fn 0.5)))
    (is (= 225 (curve-fn 0.33)))
    (is (= 198 (curve-fn 0.25)))
    (is (= 0 (curve-fn 0)))))


(deftest combine-curves
  (let [curve-fn1 (curves/bezier [100 200])
        curve-fn2 (curves/bezier [200 300 500 100])
        combined-fn (curves/combine curve-fn1 curve-fn2)]
    (is (= 100 (combined-fn 0)))
    (is (= 150 (combined-fn 0.25)))
    (is (= 200 (combined-fn 0.5)))
    (is (= 100 (combined-fn 1)))))
