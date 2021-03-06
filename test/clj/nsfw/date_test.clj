(ns nsfw.date-test
  (:use [nsfw.date] :reload)
  (:use [clojure.test])
  (:refer-clojure :exclude [= > < >= <= pr]))

(deftest test-comparators
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
