(ns party-up.universe
  (:require [clojure.core.async :as async]))


(defn ^:private close-port [_universe]
  (let [port (:port _universe)]
    (when (and (not (nil? @port))
               (.portOpened @port))
      (.closePort @port))
    _universe))


(defn ^:private open-port [_universe]
  (let [baud-rate 57600
        data-bits 8
        stop-bits 1
        parity jssc.SerialPort/PARITY_NONE
        port (jssc.SerialPort. (:port-path _universe))]
    (doto port
          .openPort
          (.setParams baud-rate data-bits stop-bits parity))
    (reset! (:port _universe) port)
    _universe))


(defn write [_universe]
  (let [port @(:port _universe)
        state @(:state _universe)
        header [126 6 1 2 0]
        footer [231]
        msg (concat header state footer)]
    (.writeIntArray port (int-array msg))
    _universe))


;; TODO rename?
(defn set-state [_universe address-values]
  (swap! (:state _universe) #(apply assoc % (flatten address-values)))
  _universe)


;; rate info https://en.wikipedia.org/wiki/DMX512#Timing
(defn ^:private start-update-loop [_universe]
  (async/go-loop []
    (when (= :started @(:status _universe))
      (let [min-break-ms 92]
        (write _universe)
        (async/<! (async/timeout min-break-ms))
        (recur))))
  _universe)


(defn ^:private set-status [_universe status]
  (reset! (:status _universe) status)
  _universe)


(defn restart [_universe]
  (-> _universe
      (set-status :stopped)
      close-port
      open-port
      (set-status :started)
      start-update-loop))


(defn stop [_universe]
  (-> _universe
      (set-status :stopped)
      close-port))


(defn blackout [_universe]
  (set-state! _universe (map vector (range 512) (replicate 512 0))))


(defrecord ^:private Universe [port port-path queue state])


(defn universe [port-path]
  (let [_universe (map->Universe {:port (atom nil)
                                  :port-path port-path
                                  :state (atom (into [] (replicate 512 0)))
                                  :status (atom :stopped)})]
    (restart _universe)))