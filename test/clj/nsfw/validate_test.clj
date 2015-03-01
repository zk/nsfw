(ns nsfw.validate-test
  (:require [clojure.test :refer :all]
            [nsfw.validate :as v]))


(deftest test-key
  (is ((v/key :foo v/not-empty? "error") {})))

(deftest test-run
  (is (v/run
        {:asdf "bar"}
        [(fn [pl]
           (if (empty? (:foo pl))
             {:foo [":foo can't be empty"]}))
         (fn [pl]
           (when (empty? (:bar pl))
             {:bar [":bar can't be empty"]}))
         (fn [pl]
           (if (empty? (:foo pl))
             {:foo [":foo some other thing"]}))
         (fn [pl]
           (if (empty? (:foo pl))
             {:message "Here's a general message"}))
         (v/key :foo v/not-empty? "Key message not empty")])))
