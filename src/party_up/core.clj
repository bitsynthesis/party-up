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
  (let [state (atom (into [] (replicate 512 0)))]
    (map->Universe {:port (atom nil)
                    :port-path port-path
                    :queue (atom (async/chan))
                    :state state})))


(defn queue-update [_universe address value]
  (async/put! @(:queue _universe) [address value]))


(defn set-state! [_universe address-values]
  (swap! (:state _universe) #(apply assoc % (flatten address-values))))


;; TODO rate limiting https://en.wikipedia.org/wiki/DMX512#Timing
;; and minimum rate for that matter... should probably not park
;; which may simplify things anyway
(defn start-update-loop [_universe]
  (async/go-loop []
    ;; park awaiting items on queue
    (let [queue @(:queue _universe)
          queue-dump (async/into [(async/<! queue)] queue)]

      ;; create a new queue for the universe and close the old
      (reset! (:queue _universe) (async/chan))
      (async/close! queue)

      ;; consume the old queue dump
      (let [min-break-ms 92
            address-values (async/<! queue-dump)]
        (set-state! _universe address-values)
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
    (when @port (close-port @port) (reset! port nil))
    (async/close! @(:queue _universe))))


(defn blackout-universe [_universe]
  (reset! (:state _universe) (into [] (replicate 512 0))))
