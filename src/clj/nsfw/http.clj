(ns nsfw.http
  "Request / response utilities"
  (:use [plumbing.core])
  (:require [plumbing.graph :as pg]))

(defn -graph [gmap strategy]
  (let [g (strategy gmap)]
    (fn [r]
      (:resp (g r)))))

(defn graph [gmap]
  (-graph gmap pg/lazy-compile))

(defn graph-eager [gmap]
  (-graph gmap pg/eager-compile))

(defn html [body]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body body})

(defn clj [body]
  {:headers {"Content-Type" "text/clj; charset=utf-8"}
   :body (pr-str body)})

(def header-for {:html {"Content-Type" "text/html; charset=utf-8"}
                 :js {"Content-Type" "text/javascript; charset=utf-8"}})

(defn headers [& opts]
  (apply merge (map header-for opts)))

(def html-header (headers :html))

(defn redirect [loc & opts]
  (merge
   {:headers {"Location" loc}
    :status 301}
   (apply hash-map opts)))