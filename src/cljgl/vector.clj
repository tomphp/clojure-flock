(ns cljgl.vector
  (:require [cljgl.math :as m]))

(defprotocol Vector
  (add [this other])
  (length [this])
  (normalise [this]))

(defrecord Vector2d [x y]
  Vector
  (add [{ax :x, ay :y} {bx :x, by :y}]
    (->Vector2d (+ ax bx) (+ ay by)))
  (length [{x :x, y :y}] (m/hyp-length x y))
  (normalise [{x :x, y :y, :as v}]
   (let [len (length v)]
     (->Vector2d (/ x len) (/ y len)))))

