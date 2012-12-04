(ns nsfw.templating
  "Dependency management for web templates."
  (:require [clojure.zip :as zip]))

(defn has-tag?
  "DFS for tag."
  [tag template]
  (loop [ptr (zip/vector-zip template)]
    (cond
     (zip/end? ptr) false
     (= tag (zip/node ptr)) true
     :else (recur (zip/next ptr)))))

(def has-html? (partial has-tag? :html))

(defn is-stylesheet-link? [node]
  (and (coll? node)
       (= :link (first node))
       (map? (second node))
       (= "stylesheet" (get (second node) :rel))))

(defn is-style-tag? [node]
  (and (coll? node)
       (= :style (first node))))

(defn is-css-tpl? [tpl]
  (or (is-stylesheet-link? tpl)
      (is-style-tag? tpl)))

(defn is-css-ptr? [ptr]
  (is-css-tpl? (zip/node ptr)))

(defn is-js-tpl? [node]
  (and (coll? node)
       (= :script (first node))
       (map? (second node))
       (= "text/javascript" (get (second node) :type))))

(defn is-js-ptr? [ptr]
  (is-js-tpl? (zip/node ptr)))


(defn collect-tags [tag-pred tpl]
  (loop [ptr (zip/vector-zip tpl)
         css-coll []]
    (if (zip/end? ptr)
      {:template (zip/root ptr) :css-coll css-coll}
      (recur (if (tag-pred ptr)
               (zip/next (zip/remove ptr))
               (zip/next ptr))
             (if (tag-pred ptr)
               (conj css-coll (zip/node ptr))
               css-coll)))))

(def collect-css (partial collect-tags is-css-ptr?))

(def collect-js (partial collect-tags is-js-ptr?))

