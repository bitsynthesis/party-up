(ns party-up.core-test
  (:require [clojure.test :refer [deftest is]]
            [party-up.core :as core]))


;; (deftest start-universe
;;   (let [test-port-path "/dev/ttyFAKE"
;;         test-universe (core.universe test-port-path)]
;;     (with-redefs [core/open-port ])
;;     (core/start-universe)
;;     )
;;   )


(deftest int-to-signed-byte
  (is (= 0 (#'core/int-to-signed-byte 0)))
  (is (= 1 (#'core/int-to-signed-byte 1)))
  (is (= 127 (#'core/int-to-signed-byte 127)))
  (is (= -128 (#'core/int-to-signed-byte 128)))
  (is (= -2 (#'core/int-to-signed-byte 254)))
  (is (= -1 (#'core/int-to-signed-byte 255))))


;; (deftest stop-universe)
;;
;;
;; (deftest update-universe)
;;
;;
;; (deftest blackout-universe)
;;
;;
;; (deftest device-function)
