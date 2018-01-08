(ns party-up.universe
  (:require [clojure.core.async :as async]))


;; TODO universe should be plural or devices should be singular
;; there should also be a reflective structure for universe
;; devices as fixtures. perhaps "fixture" is a better term than
;; device. perhaps "interface" for dmxking.


(defn ^:private close-port [_universe]
  (let [port (:port _universe)]
    ;; TODO this is broken
    ;; (when (and (not (nil? @port))
    ;;            (.portOpened @port))
    ;;   (.closePort @port))
    (try (.closePort @port) (catch Exception e))
    _universe))


;; TODO extract dmxking specific settings
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


;; TODO extract dmxking specific settings
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


;; TODO this rate limiting may not be necessary
;; rate info https://en.wikipedia.org/wiki/DMX512#Timing
(defn ^:private start-update-loop [_universe]
  (async/go-loop []
    (when (= :started @(:status _universe))
      (let [min-break-ms 92]
        (write _universe)
        ;; (async/<! (async/timeout min-break-ms))
        (recur))))
  _universe)


(defn ^:private set-status [_universe status]
  (reset! (:status _universe) status)
  _universe)


(defn start [_universe]
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
  (set-state _universe (map vector (range 512) (replicate 512 0))))


(defrecord ^:private Universe [port port-path state])


(defn universe [port-path]
  (map->Universe {:port (atom nil)
                  :port-path port-path
                  :state (atom (vec (replicate 512 0)))
                  :status (atom :stopped)}))
