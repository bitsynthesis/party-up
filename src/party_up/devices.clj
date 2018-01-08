(ns party-up.devices
  (:require [party-up.universe :as uni]))


(defrecord Device [address universe])


(defprotocol Brightness
  (brightness [device level]))


(defprotocol Color
  (color [device color])
  (color-speed [device speed])
  (red [device value])
  (green [device value])
  (blue [device value]))


(defprotocol Movement
  (pan [device degree])
  (pan-fine [device degree])
  (tilt [device degree])
  (tilt-fine [device degree])
  (pan-tilt-speed [device speed]))


;; TODO
(defprotocol Panel
  (get-matrix [device])
  (set-matrix [device matrix]))


(defprotocol Strobe
  (strobe [device speed]))


(defmacro defdevice
  "Define a record with the given extensions. The device record requires the
   attributes :universe, a universe record, and :address, the DMX
   address mapped to the first channel of the device."
  [_name & extensions]
  (let [values ['universe 'address]]
    `(do
       (defrecord ~_name ~(vec values))
       (extend ~_name ~@extensions))))


(defn channel-handler [device number]
  (let [address (+ number (:address device))]
    (fn [value]
      (uni/set-state (:universe device) [address value]))))


(defn channel
  ([number] (channel number identity))
  ([number modifier]
   (fn [device value]
     ((channel-handler device number) (modifier value)))))


(defn disabled [& _])
