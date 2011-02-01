(ns nsfw.test.csrf
  (:use [nsfw.csrf] :reload)
  (:use [clojure.test]))

(def req-without-token {:uri "/foo"})
(def req-with-token {:uri "/foo" :session {:csrf-token (gen-token)}})

(deftest test-insert-token
  (is (:csrf-token (:session (insert-token {} (gen-token))))))

(deftest test-pull
  (let [token (:csrf-token (:session req-with-token))]
    (is (= token (pull req-with-token)))))

(deftest test-wrap-bind-csrf
  (is ((wrap-bind-csrf
        (fn [req] {:session {:csrf-token (current)}})) req-with-token))
  (is (:csrf-token (:session ((wrap-bind-csrf (fn [req] {})) req-without-token)))))



