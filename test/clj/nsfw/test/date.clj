(ns nsfw.test.date
  (:use [nsfw.date] :reload)
  (:use [clojure.test])
  (:refer-clojure :exclude [= > < >= <=]))

(deftest test-comparators
  (is      (= (from :now) (from :now)))
  (is (not (= (from :now) (from :yesterday))))

  (is      (> (from :now) (from :yesterday)))
  (is (not (> (from :now) (from :tomorrow))))

  (is      (< (from :now) (from :tomorrow)))
  (is (not (< (from :now) (from :yesterday)))))

(deftest test-in-range
  (is ((in-range (from :yesterday)
                 (from :tomorrow)) (from :now)))
  
  (is (not ((in-range (from :now)
                      (from :tomorrow)) (from :yesterday))))
  
  (is (not ((in-range (from :yesterday)
                      (from :now)) (from :tomorrow)))))
