(ns party-up.core-test
  (:require [bond.james :as bond :refer [with-stub]]
            [clojure.test :refer [deftest is]]
            [party-up.core :as core]))


(def test-port :test-port)
(def test-port-path "/dev/ttyFAKE")
(def test-state (into [] (replicate 512 200)))


(defn create-test-universe
  ([] (create-test-universe {}))
  ([args]
   (merge (core/universe test-port-path) args)))


(deftest start-universe
  (with-stub [core/open-port core/close-port]
    (core/start-universe (create-test-universe))
    (is (= 1 (-> core/open-port bond/calls count)))
    (is (= 0 (-> core/close-port bond/calls count)))
    (is (= [test-port-path] (-> core/open-port bond/calls first :args)))))


(deftest stop-existing-before-starting-universe
  (with-stub [core/open-port core/close-port]
    (core/start-universe (create-test-universe {:port (atom test-port)}))
    (is (= 1 (-> core/open-port bond/calls count)))
    (is (= 1 (-> core/close-port bond/calls count)))
    (is (= [test-port-path] (-> core/open-port bond/calls first :args)))
    (is (= [test-port] (-> core/close-port bond/calls first :args)))))


(deftest stop-universe
  (let [test-universe (create-test-universe {:port (atom test-port)})]
    (with-stub [core/close-port]
      (core/stop-universe test-universe)
      (is (= 1 (-> core/close-port bond/calls count)))
      (is (= [test-port] (-> core/close-port bond/calls first :args)))
      (is (= nil @(:port test-universe))))))


(deftest int-to-signed-byte
  (is (= 0 (#'core/int-to-signed-byte 0)))
  (is (= 1 (#'core/int-to-signed-byte 1)))
  (is (= 127 (#'core/int-to-signed-byte 127)))
  (is (= -128 (#'core/int-to-signed-byte 128)))
  (is (= -2 (#'core/int-to-signed-byte 254)))
  (is (= -1 (#'core/int-to-signed-byte 255))))


;; TODO trim down to less than 512 values for test clarity
(deftest update-universe
  (with-stub [core/write-bytes]
    (core/update-universe (create-test-universe {:port (atom test-port)
                                                 :state (atom test-state)}))

    (is (= 1 (-> core/write-bytes bond/calls count)))
    (is (= test-port (-> core/write-bytes bond/calls first :args first)))
    ;; assert output bytes are correct. c8 is 200
    (is (= (apply str (concat ["7e06010200"] (replicate 512 "c8") ["e7"]))
           (->> core/write-bytes
                bond/calls
                first
                :args
                second
                (new java.math.BigInteger)
                (format "%x"))))))


(deftest blackout-universe
  (with-stub [core/write-bytes]
    (core/blackout-universe (create-test-universe {:port (atom test-port)
                                                   :state (atom test-state)}))

    (is (= 1 (-> core/write-bytes bond/calls count)))
    (is (= test-port (-> core/write-bytes bond/calls first :args first)))
    ;; assert output bytes are correct. c8 is 200
    (is (= (apply str (concat ["7e06010200"] (replicate 512 "00") ["e7"]))
           (->> core/write-bytes
                bond/calls
                first
                :args
                second
                (new java.math.BigInteger)
                (format "%x"))))))


;; (deftest device-function)
