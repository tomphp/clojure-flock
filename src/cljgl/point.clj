(ns cljgl.point
  (:require [cljgl.math :as m]))

(defprotocol Point
  (distance [this other]))

(defrecord Point2d [x y]
  Point
  (distance [{ax :x, ay :y} {bx :x, by :y}]
    (let [delta-x (Math/abs (- bx ax))
          delta-y (Math/abs (- by ay))]
      (m/hyp-length delta-x delta-y))))
