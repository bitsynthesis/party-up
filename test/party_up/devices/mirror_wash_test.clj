(ns party-up.devices.mirror-wash-test
  (:require [clojure.test :refer [deftest is]]
            [party-up.devices.mirror-wash :as mw]))


(deftest look-up-single-values
  (let [test-state (vec (range 512))]
    (doseq [[location value] [[[0 0 0] 0]
                              [[0 0 1] 1]
                              [[0 7 2] 23]
                              [[1 0 0] 24]
                              [[7 7 2] 191]]]
      (is (= value (mw/get-mirror-wash-state-value test-state location))))))


(deftest look-up-rgb-values
  (let [test-state (vec (range 512))]
    (doseq [[location value] [[[0 0] [0 1 2]]
                              [[0 7] [21 22 23]]
                              [[1 0] [24 25 26]]
                              [[7 7] [189 190 191]]]]
      (is (= value (mw/get-mirror-wash-state-value test-state location))))))


(deftest look-up-offset-values
  (let [test-state (vec (range 512))
        offset 192]
    (doseq [[location value] [[[0 0 0] 192]
                              [[0 0 1] 193]
                              [[0 7 2] 215]
                              [[1 0 0] 216]
                              [[7 7 2] 383]]]
      (is (= value
             (mw/get-mirror-wash-state-value test-state location offset))))))


(defn create-test-arrangement
  " Create an arrangement of panels
        uni1 0   | uni2 0
        -------------------
        uni1 192 | uni2 192"
  [state1-atom state2-atom]
  (let [test-uni1 {:state state1-atom}
        test-uni2 {:state state2-atom}
        test-row1 [(mw/panel test-uni1 0) (mw/panel test-uni2 0)]
        test-row2 [(mw/panel test-uni1 192) (mw/panel test-uni2 192)]]
    (mw/arrangement [test-row1 test-row2])))


(deftest get-from-arrangement
  (let [test-arrangement (create-test-arrangement
                          (atom (vec (range 0 512)))
                          (atom (vec (range 513 1024))))]
    (doseq [[location value] [[[0 0 0] 0]
                              [[0 0 1] 1]
                              [[0 7 2] 23]
                              [[1 0 0] 24]
                              [[7 7 2] 191]
                              [[0 8 0] 192]
                              [[7 15 2] 383]
                              [[8 0 0] 513]
                              [[15 15 2] 896]]]
      (is (= value (mw/get-value test-arrangement location))))))


(deftest set-in-arrangement
  (let [test-state1 (atom (vec (replicate 512 0)))
        test-state2 (atom (vec (replicate 512 0)))
        test-arrangement (create-test-arrangement test-state1 test-state2)]

    (mw/set-value test-arrangement [0 7 2] 255)
    (mw/set-value test-arrangement [7 15 2] 255)
    (mw/set-value test-arrangement [8 0 0] 255)
    (mw/set-value test-arrangement [15 15 2] 255)

    (is (= 255 (nth @test-state1 23)))
    (is (= 255 (nth @test-state1 383)))
    (is (= 255 (nth @test-state2 0)))
    (is (= 255 (nth @test-state2 383)))))


;; (deftest dummy-panel)


(deftest get-arrangement-matrix
  (let [test-arrangement (create-test-arrangement
                          (atom (vec (range 0 512)))
                          (atom (vec (range 513 1024))))
        matrix (mw/get-arrangement-matrix test-arrangement)]
    (doseq [[location value] [[[0 0 0] 0]
                              [[0 0 1] 1]
                              [[0 7 2] 23]
                              [[1 0 0] 24]
                              [[7 7 2] 191]
                              [[0 8 0] 192]
                              [[7 15 2] 383]
                              [[8 0 0] 513]
                              [[15 15 2] 896]]]
      (is (= value (get-in matrix location))))))


;; set values for whole arrangement
;; provide dummy panel to allow for spaces in grid


;; (def ta (create-test-arrangement
;;          (atom (vec (range 0 512)))
;;          (atom (vec (range 513 1024)))))
;;
;;
;; (mw/monitor [[(mw/panel {:state (atom (vec (replicate 512 0)))} 0)
;;               (mw/panel {:state (atom (vec (replicate 512 0)))} 0)]])
;;
;;
;;
