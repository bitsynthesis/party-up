(ns party-up.core
  (:require [clojure.core.async :as async]))


;; TODO can this be private?
(defn open-port [port-path]
  (let [baud-rate 57600
        data-bits 8
        stop-bits 1
        parity jssc.SerialPort/PARITY_NONE]
    (doto (jssc.SerialPort. port-path)
          .openPort
          (.setParams baud-rate data-bits stop-bits parity))))


;; TODO can this be private?
(defn close-port [port]
  (.closePort port))


(defn- int-to-signed-byte [i]
  (byte (if (< i 128) i (- i 256))))


;; TODO can this be private?
;; TODO use writeIntArray instead, then no byte fuckery, maybe
;; https://github.com/scream3r/java-simple-serial-connector
;; /blob/2.8.0/src/java/jssc/SerialPort.java
(defn write-bytes [port b-array]
  (.writeBytes port b-array))


(defn write-universe [_universe]
  (let [header [126 6 1 2 0]
        footer [231]]
    (->> (concat header @(:state _universe) footer)
         (map int-to-signed-byte)
         byte-array
         (write-bytes @(:port _universe)))))


(defn set-state [_universe address value]
  (async/put! @(:channel _universe) [address value]))


(defrecord Universe [channel port port-path state])


;; TODO channel is confusing here, async vs dmx
(defn universe [port-path]
  (let [state (atom (into [] (replicate 512 0)))
        _universe (map->Universe {:channel (atom (async/chan))
                                  :port (atom nil)
                                  :port-path port-path
                                  :state state})]

    (add-watch state :write-universe (fn [& _] (write-universe _universe)))
    _universe))


;; whenever the state is swapped, write-universe is triggered
(defn swap-state [_universe address-values]
  (swap! (:state _universe) #(apply assoc % (flatten address-values))))


;; TODO rate limiting https://en.wikipedia.org/wiki/DMX512#Timing
;; and minimum rate for that matter... should probably not park
;; which may simplify things anyway
(defn start-update-loop [_universe]
  (async/go-loop []
    ;; park awaiting items on channel
    (let [channel @(:channel _universe)
          channel-dump (async/into [(async/<! channel)] channel)]

      ;; create a new channel for the universe and close the old
      (reset! (:channel _universe) (async/chan))
      (async/close! channel)

      ;; consume the old channel dump
      (let [min-break-ms 92
            address-values (async/<! channel-dump)]
        (swap-state _universe address-values)
        (async/<! (async/timeout min-break-ms))
        (recur)))))


;; TODO add ! to appropriate fn names?
(defn start-universe [_universe]
  (let [port (:port _universe)]
    (when @port (close-port @port))
    (reset! port (open-port (:port-path _universe)))
    (start-update-loop _universe)))


(defn stop-universe [_universe]
  (let [port (:port _universe)]
    (when @port (close-port @port) (reset! port nil))
    (async/close! @(:channel _universe))))


(defn blackout-universe [_universe]
  (reset! (:state _universe) (into [] (replicate 512 0))))
