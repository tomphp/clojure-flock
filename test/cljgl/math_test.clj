(ns cljgl.math-test
  (:require [clojure.test :refer :all]
            [cljgl.math :refer :all]))

(deftest math-test
  (testing "it calcuates the different between 2 numbers"
    (is (= 2 (difference 2 4)))
    (is (= 5 (difference 10 5))))

  (testing "it squares 2 numbers"
    (is (= 4 (square 2)))
    (is (= 9 (square 3)))) 

  (testing "it calculates the hypotenuse from the other 2 sides"
    (is (= 5.0 (hyp-length 3 4)))))
