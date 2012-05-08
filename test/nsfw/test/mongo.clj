(ns nsfw.test.mongo
  (:use [nsfw.mongo :as mon] :reload)
  (:use [clojure.test]))

(deftest test-parse-username
  (is (= "foo" (parse-username (java.net.URI. "http://foo:bar@zaarly.com"))))
  (is (= nil   (parse-username (java.net.URI. "http://zaarly.com")))))

(deftest test-parse-password
  (is (= "bar" (parse-password (java.net.URI. "http://foo:bar@zaarly.com"))))
  (is (= nil   (parse-password (java.net.URI. "http://zaarly.com")))))

