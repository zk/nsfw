(ns nsfw.test.env
  (:use [nsfw.env] :reload)
  (:use [clojure.test])
  (:refer-clojure :exclude (int str)))


(deftest test-env
  (is (= (env :user) (System/getenv "USER")))

  ;; not found
  (is (not (env :foobarbazbat)))

  ;; default
  (is (= "foo" (env :foobarbazbat "foo"))))
