(ns nsfw.mongo-test
  (:use [nsfw.mongo :as mon] :reload)
  (:use [clojure.test]))

(deftest test-parse-username
  (is (= "foo" (parse-username (java.net.URI. "http://foo:bar@zaarly.com"))))
  (is (= nil   (parse-username (java.net.URI. "http://zaarly.com")))))

(deftest test-parse-password
  (is (= "bar" (parse-password (java.net.URI. "http://foo:bar@zaarly.com"))))
  (is (= nil   (parse-password (java.net.URI. "http://zaarly.com")))))

(deftest test-parse-mongo-url
  (is (= {:username "user"
          :password "pass"
          :host "host"
          :port 123
          :db "db"}
         (parse-mongo-url "mongodb://user:pass@host:123/db"))))
