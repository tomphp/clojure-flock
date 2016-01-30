(ns cljgl.math)

(defn difference [a b] (Math/abs (- a b)))

(defn square [number] (* number number))

(defn hyp-length [side-a side-b]
  (Math/sqrt (+ (square side-a) (square side-b))))
