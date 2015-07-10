(ns nsfw.util-test
  (:use nsfw.util :reload)
  (:use clojure.test))

(deftest test-to-json
  (is (= "\"foo\"" (to-json "foo")))
  (is (= "10" (to-json 10)))
  (is (= "true" (to-json true)))
  (is (= "null" (to-json nil)))
  (is (= {:foo "bar"} (from-json (to-json {:foo "bar"})))))

(deftest test-from-json
  (is (= "foo" (from-json "\"foo\"")))
  (is (= 10 (from-json "10")))
  (is (= true (from-json "true")))
  (is (= nil (from-json "null"))))

(deftest test-url-encode
  (is (= "http%3A%2F%2Fgoogle.com" (url-encode "http://google.com"))))

(deftest test-transit
  (is (= (from-transit
           (to-transit
             {:nil nil
              :bool [true false]
              :string "hello world"
              :char \c
              :sym 'foo
              :keyword :foo
              :int 10
              :float 1.5
              :list '()
              :vec []
              :map {:foo "bar"}
              :set #{1 2 3}})))))
