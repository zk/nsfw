(ns nsfw.http-test
  (:use [nsfw.http] :reload)
  (:use [clojure.test]
        [plumbing.core]))

(defn run-handler [gmap & request]
  (let [handler (graph gmap)]
    (handler (or request {}))))

(deftest test-graph
  (is (= "foo"
         (run-handler {:resp (fnk [] "foo")}))))

(deftest test-html
  (= "text/html"
     (-> (html "foo")
         :headers
         (get "Content-Type"))))

(deftest test-content-type
  (is (= {:media-type "application/json"
          :params {:charset "utf-8"
                   :foo "bar"}}
         (content-type
           {:headers
            {"Content-Type"
             "application/json; charset=utf-8; foo=bar"}}))))
