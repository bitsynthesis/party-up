(ns party-up.core)


(defrecord Universe [port port-path state])


(defn universe [port-path]
  (map->Universe {:port (atom nil)
                  :port-path port-path
                  :state (atom (into [] (replicate 512 0)))}))


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


(defn start-universe [_universe]
  (let [port (:port _universe)]
    (when @port (close-port @port))
    (reset! port (open-port (:port-path _universe)))
    _universe))


(defn stop-universe [_universe]
  (let [port (:port _universe)]
    (when @port (close-port @port) (reset! port nil))
    _universe))


(defn- int-to-signed-byte [i]
  (byte (if (< i 128) i (- i 256))))


;; TODO can this be private?
(defn write-bytes [port b-array]
  (.writeBytes port b-array))


(defn update-universe [_universe]
  (let [header [126 6 1 2 0]
        footer [231]]
    (->> (concat header @(:state _universe) footer)
         (map int-to-signed-byte)
         byte-array
         (write-bytes @(:port _universe)))))


(defn blackout-universe [_universe]
  (reset! (:state _universe) (into [] (replicate 512 0)))
  (update-universe _universe))


(defrecord Device [starting-address universe])


(defn device [_universe starting-address]
  (map->Device {:starting-address starting-address
                :universe _universe}))


(defn set-state [_universe address value]
  (swap! (:state _universe) assoc address value))


(defn device-function [device channel]
  (let [address (+ (:starting-address device) channel)]
    (fn [value]
      (set-state (:universe device) address value)
      ;; TODO this should be happening automatically all the time, according to
      ;; standard dmx behavior...
      (update-universe (:universe device)))))
