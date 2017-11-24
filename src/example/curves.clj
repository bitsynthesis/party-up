(ns example.curves
  (:require [party-up.curves :refer :all]))


(def ramp (bezier [0 255]))

(def oops (combine ramp (invert ramp)))
(def updown (combine ramp (flip ramp)))

(def updown4 (duplicate 4 (combine ramp (invert ramp))))

;; (view [ramp updown])

(view updown4)



(defn append [curve-fn modifier-fn]
  (combine curve-fn (modifier-fn curve-fn)))


(def and-flip (comp combine (juxt identity flip)))

(def zig (with (comp flip invert) ramp))


(def swoop (bezier [0 75 100 100 25 0]))
(def tail (poly [255 100 100 100 0]))

(view (as-> swoop |
            (append (comp duplicate invert) |)
            (replicate 3 |)
            (combine | tail)))


(def my-player (player 120 swoop :loop))

;; bpm
;; beats in curve
;; loop?

;; bpm
;; curves
;;   beats-in-curves
;; loop?

;; curves need to be composable with variable proportion of x

(start my-player)
(play my-player zig)
(stop my-player)


(def my-track (track 120))
(my-track 8 swoop
          2 tail)

;; split swoop into 8 functions representing each beat
;;     ABCDEFGH | A B C D E F G H

;; split tail into 2 functions representing each beat
;;     IJ | I J

;; combine all
;;     ABCDEFGHIJ


;; TODO *****
;; work like combine and take mixed vars of collections and
;; single functions (flatten)
;; assume every single function is 1 beat
(play (bpm 120)
      (track (beats 8 swoop)
             (beats 3 zig)
             tail)
      (track (beats 4 (point 123))
             tail))


;; option to stretch all tracks to be size of first track
;;     combine each track
;;     play back at time for bpm in ms * count of 1st track
;; option to play all tracks out of sync looping (forever ?)
;;     find the minimum common multiple
;;     replicate each track to that number of beats
;;     combine each track
;;     split all tracks into beats per multiple
;;     replicate and / or take the number desired for playback
;;         (if infinite, use multiple)
;;     play back via first option
;;         combine each track
;;         play back at time for bpm in ms * count of 1st track



;; TODO remember we need to map these values to functions at some point

;; stretch / squeeze to specific number of beats
;;     (comp (partial beats number) combine)
;; truncate to smallest number of beats
;;     ... (apply min (map count tracks))
;; stretch to largest number of beats
;;     ... (apply max (map count tracks))
;; fill to largest number of beats
;;     ... (apply max (map count tracks))
;; loop until sync
;;     something something denominators


(def ramp (poly [0 100]))


;; TODO add to view, if coll then combine, or something like that?
(defn view-tracks [tracks]
  ;; TODO add beat marks
  (view (map combine tracks)))


(defn stretch
  ([tracks] (stretch (apply max (map count tracks)) tracks))
  ;; TODO need to flatten and handle single tracks
  ([length tracks]
   (->> tracks
        (map combine)
        (map (partial beats length)))))

(view-tracks (stretch [(track (beats 3 ramp) (flip ramp))
                       (track (flip ramp))]))

(view-tracks (stretch 2 [(track (beats 3 ramp) (flip ramp))
                         (track (flip ramp))]))

(count (first (stretch [(track (beats 3 ramp) (flip ramp))
                        (track (flip ramp))])))

(defn truncate [tracks]
  (let [length (apply min (map count tracks))]
    (map (partial take length) tracks)))

(view-tracks (truncate [(track (beats 3 ramp) (flip ramp))
                       (track (flip ramp))]))

(defn greatest-common-denominator
  [a b]
  (if (zero? b) a (recur b, (mod a b))))


(defn least-common-multiple
  [a b]
  (/ (* a b) (greatest-common-denominator a b)))


(defn unsync [tracks]
  (let [multiple (reduce least-common-multiple (map count tracks))]
    (for [track tracks]
         (let [number (/ multiple (count track))]
           (beats multiple (duplicate number (combine track)))))))


(view-tracks (unsync [(track (beats 3 ramp) (flip ramp))
                      (track (flip ramp))]))

(defn fill [tracks]
  (let [length (apply max (map count tracks))]
    (for [track tracks]
         (let [number (- length (count track))]
           (concat track (replicate number (point ((last track) 1))))))))

(view-tracks (fill [(track (beats 3 ramp) (flip ramp))
                    (track (flip ramp))]))


;; ABCD | AB
;; EF   | EF

;; ABCD | ABC
;; EFGH | EFG

;; ABCD | ABCD
;; EF   | EFFF
;; -----------
;; AB   | ABBB
;; CDEF | CDEF
(play (bpm 120)
      (track (beats 8 swoop)
             (beats 3 zig)
             tail)
      (track (beats 4 (point 123))
             tail))

;; ABCD | ABCD
;; EF   | EEFF
(play (bpm 120)
      (stretch
       (track (beats 8 swoop)
              (beats 3 zig)
              tail)
       (track (beats 4 (point 123))
              tail)))

;; ABCD | ABCDABCDABCD
;; EFG  | EFGEFGEFGEFG
(play (bpm 120)
      (unsync
       (track (beats 8 swoop)
              (beats 3 zig)
              tail)
       (track (beats 4 (point 123))
              tail)))




;; FIXME is combine really a consistent api?

;; FIXME must be easy as pie to play a one-shot or loop with fixed time and single fn


;; FIXME then we need a way to replace currently playing tracks


;; FIXME and also a way to play multiple tracks?


(play-track my-track 120)

;; get time in ms
;;     (bpm-to-ms 120 (count my-track) | 5000

;; play track
;;     (play 5000 4 my-sequencer)

;; (also flip my-curve)

;; (dupe my-curve)




;; dupe fn
;; A | A A
;;
;; dupe [fn fn]
;; A | A A
;; B | B B
;;
;; combine [fn fn]
;; A | A B
;; B |
;;
;; flip fn
;; AB | BA
;;
;; flip [fn fn]
;; AB | BA
;; CD | DC
;;
;; invert fn
;; M | W
;;
;; invert [fn fn]
;; M | W
;; A | V
