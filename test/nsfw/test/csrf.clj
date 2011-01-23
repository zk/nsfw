(ns nsfw.test.csrf
  (:use [nsfw.csrf] :reload)
  (:use [clojure.test]))


(deftest test-insert-token
  (is (:csrf-token (:session (insert-token {})))))

(deftest test-pull
  (let [req (insert-token {})
        token (:csrf-token (:session req))]
    (is (= token (pull req)))))

(deftest test-wrap-bind-csrf
  (is ((wrap-bind-csrf (fn [req] (current))) {:session {:csrf-token (gen-token)}})))