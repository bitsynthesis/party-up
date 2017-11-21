(ns party-up.core
  (:require [clojure.core.async :as async]))


(defn ^:private open-port [port-path]
  (let [baud-rate 57600
        data-bits 8
        stop-bits 1
        parity jssc.SerialPort/PARITY_NONE]
    (doto (jssc.SerialPort. port-path)
          .openPort
          (.setParams baud-rate data-bits stop-bits parity))))


(defn ^:private close-port [port]
  (.closePort port))


(defn ^:private int-to-signed-byte [i]
  (byte (if (< i 128) i (- i 256))))


;; TODO use writeIntArray instead, then no byte fuckery, maybe
;; https://github.com/scream3r/java-simple-serial-connector
;; /blob/2.8.0/src/java/jssc/SerialPort.java
(defn ^:private write-bytes [port b-array]
  (.writeBytes port b-array))


(defn write-universe [_universe]
  (let [header [126 6 1 2 0]
        footer [231]]
    (->> (concat header @(:state _universe) footer)
         (map int-to-signed-byte)
         byte-array
         (write-bytes @(:port _universe)))))


(defrecord Universe [port port-path queue state])


(defn universe [port-path]
  (map->Universe {:port (atom nil)
                  :port-path port-path
                  :state (atom (into [] (replicate 512 0)))
                  :status (atom :stopped)}))


(defn set-state! [_universe address-values]
  (swap! (:state _universe) #(apply assoc % (flatten address-values))))


;; rate info https://en.wikipedia.org/wiki/DMX512#Timing
(defn start-update-loop [_universe]
  (reset! (:status _universe) :started)
  (async/go-loop []
    (when (= :started @(:status _universe))
      (let [min-break-ms 92]
        (write-universe _universe)
        (async/<! (async/timeout min-break-ms))
        (recur)))))


(defn start-universe [_universe]
  (let [port (:port _universe)]
    (when @port (close-port @port))
    (reset! port (open-port (:port-path _universe)))
    (start-update-loop _universe)))


(defn stop-universe [_universe]
  (let [port (:port _universe)]
    (reset! (:status _universe) :stopped)
    (when @port (close-port @port) (reset! port nil))))


(defn blackout-universe [_universe]
  (reset! (:state _universe) (into [] (replicate 512 0))))
