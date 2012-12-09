(ns nsfw.test.html
  (:use [nsfw.html] :reload)
  (:use [clojure.test]))

(deftest test-href
  (is (= [:a {:href "http://google.com" :rel "nofollow"} "Google!"]
         (href "http://google.com" "Google!" :rel "nofollow")))

  ;; nesting
  (is (= [:a {:href "foo"} [:span "bar"]]
         (href "foo" [:span "bar"]))))

(deftest test-script
  (is (= [:script {:type "text/javascript" :src "js-path"}]
         (script "js-path"))))

(deftest test-stylesheet
  (is (= [:link {:rel "stylesheet" :href "css-path"}]
         (stylesheet "css-path"))))

(deftest test-css-rule
  (is (= "h1{foo:bar;}"
         (css-rule [:h1 {:foo "bar"}])))

  (is (= "h1{foo:bar;}"
         (css-rule [:h1 {:foo :bar}])))

  (is (= "h1{foo:bar;baz:bap;}"
         (css-rule [:h1 {:foo "bar" :baz "bap"}])))

  (is (= "h1.foo{bar:baz;}"
         (css-rule [:h1.foo {:bar "baz"}])))

  (is (= "h1.foo bar{baz:bap;}"
         (css-rule [:h1.foo :bar {:baz "bap"}]))))

(deftest test-css
  (is (= "h1{foo:bar;}h2{baz:bap;}"
         (css [:h1 {:foo :bar}]
              [:h2 {:baz :bap}]))))
