(ns nsfw.templating-test
  (:use nsfw.templating :reload)
  (:use clojure.test)
  (:require [clojure.zip :as zip]))

(deftest test-has-html?
  (are [res template]  (= res (has-html? template))
       true  [:html [:div.foo "bar"]]
       true  [:div.bar [:html [:head [:title "foobar"]]]]
       false [:div [:div [:div]]]))

(deftest test-is-css-tpl?
  (are [res template] (= res (is-css-tpl? template))
       true  [:link {:rel "stylesheet"}]
       true  [:style "p { color: blue; }"]
       false [:foo [:link {:rel "stylesheet"}]]))

(deftest test-is-js-tpl?
  (are [res template] (= res (is-js-tpl? template))
       true  [:script {:type "text/javascript"}]
       false [:script]))

(deftest test-collect-css
  (let [res (collect-css [:html
                          [:head
                           [:link {:rel "stylesheet" :href "foo.css"}]
                           [:link {:rel "stylesheet" :href "bar.css"}]]])]
    (is (= [[:link {:rel "stylesheet" :href "foo.css"}]
            [:link {:rel "stylesheet" :href "bar.css"}]]
           (:css-coll res)))))
