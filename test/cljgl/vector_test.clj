(ns cljgl.vector-test
  (:require [clojure.test :refer :all]
            [cljgl.vector :refer :all]))

(deftest Vector2d-test
  (testing "a vector2d has an x and y component"
    (let [v (->Vector2d 12 88)]
      (is (= 12 (:x v)))
      (is (= 88 (:y v)))))

  (testing "2 Vector2ds can be added"
    (let [v1 (->Vector2d 5 11)
          v2 (->Vector2d 2 4)]
      (is (= (->Vector2d 7 15) (add v1 v2)))))
  
  (testing "it calculates the length of a Vector2d"
    (is (= 5.0 (length (->Vector2d 3 4)))))
  
  (testing "it returns a normalised Vector2d"
    (is (= 1.0 (-> (->Vector2d 3 4) normalise length))))
  (testing "it scales a Vector2d"
    (is (= (->Vector2d 7 14) (scale (->Vector2d 1 2) 7)))))
