(ns cljgl.point-test
  (:require [clojure.test :refer :all]
            [cljgl.point :refer :all]))

(deftest Point2d-test
  (testing "a point has an x and y component"
    (let [point (->Point2d 12 88)]
      (is (= 12 (:x point)))
      (is (= 88 (:y point)))))
  
  (testing "getting the distance between 2 points horizontally"
    (let [a (->Point2d 0 0)
          b (->Point2d 1 0)]
      (is (= 1.0 (distance a b)))
      (is (= 1.0 (distance b a)))))  

  (testing "getting the distance between 2 points horizontally"
    (let [a (->Point2d 0 0)
          b (->Point2d 0 2)]
      (is (= 2.0 (distance a b)))
      (is (= 2.0 (distance b a)))))   

  (testing "getting the distance between 2 points horizontally"
    (let [a (->Point2d 0 0)
          b (->Point2d 3 4)]
      (is (= 5.0 (distance a b)))
      (is (= 5.0 (distance b a))))))    
