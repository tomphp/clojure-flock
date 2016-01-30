(ns cljgl.core-test
  (:require [clojure.test :refer :all]
            [cljgl.core :refer :all]
            [cljgl.vector :refer [->Vector2d]]
            [cljgl.point :refer [->Point2d]]))

(deftest flocking-test
  (let [position (->Point2d 100 100)
        velocity (->Vector2d 1 1)
        bird {:position position, :velocity velocity}]

    (testing "bounce has no effect when bird is on screen"
      (is (= bird (bounce {:width 200, :height 200}, bird))))
    
    (testing "bounce points the velocity up if the bird goes off the bottom of the screen"
      (is (= (->Vector2d 1 -1) (:velocity (bounce {:width 200, :height 50} bird)))))
    
    (testing "bounce points the velocity left if the bird goes off the right of the screen"
      (is (= (->Vector2d -1 1) (:velocity (bounce {:width 50, :height 200} bird))))))

  (let [velocity (->Vector2d -1 -1)
        position-bird (fn [x y] {:position (->Point2d x y) :velocity velocity})
        context {:width 100, :height 100}]

    (testing "bounce points the velocity down if the bird goes off the top of the screen"
      (is (= (->Vector2d -1 1) (:velocity (bounce context (position-bird 50 -1))))))
    
    (testing "bounce points the velocity right if the bird goes off the left of the screen"
      (is (= (->Vector2d 1 -1) (:velocity (bounce context (position-bird -1 50)))))))

    )
