(ns nsfw.test.util
  (:use nsfw.util :reload)
  (:use clojure.test))

(deftest test-json-encode
  (is (= "\"foo\"" (json-encode "foo")))
  (is (= "10" (json-encode 10)))
  (is (= "true" (json-encode true)))
  (is (= "null" (json-encode nil)))
  (is (= {:foo "bar"} (json-encode (json-decode {:foo "bar"})))))

(deftest test-json-decode
  (is (= "foo" (json-decode "\"foo\"")))
  (is (= 10 (json-decode "10")))
  (is (= true (json-decode "true")))
  (is (= nil (json-decode "null"))))
