(ns cljgl.random
  (:require [cljgl.point :as p :refer [->Point2d]]
            [cljgl.vector :as v :refer [->Vector2d]]))

(defn normalised-vector []
  (let [rand-fn #(- (rand) 0.5)]
    (v/normalise (->Vector2d (rand-fn) (rand-fn)))))

(defn colour []
  (let [rand-fn #(+ (* (rand) (- 1 %1)) %1)] [(rand-fn 0.2) (rand-fn 0.5) (rand-fn 0.8)]))

(defn bird
  [{width :width, height :height}]
  {:position (->Point2d (* (rand) width) (* (rand) height))
   :velocity (normalised-vector)
   :colour (colour)})
