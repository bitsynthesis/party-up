(ns party-up.devices)


(defprotocol Strobe
  (strobe [device speed]))


(defprotocol Movement
  (pan [device degree])
  (pan-fine [device degree])
  (tilt [device degree])
  (tilt-fine [device degree])
  (pan-tilt-speed [device speed]))


(defprotocol Color
  (preset [device color])
  (red [device value])
  (green [device value])
  (blue [device value]))


(defprotocol Brightness
  (brightness [device level]))


(defmacro defdevice [_name & extensions]
  (let [values ['universe 'starting-address]]
    `(do
       (defrecord ~_name ~(vec values))
       (extend ~_name ~@extensions))))
