(ns nsfw.html
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as hiccup-page])
  (:refer-clojure :exclude [meta]))

(defn href [href content & opts]
  (let [opts (apply hash-map opts)]
    (hiccup/html [:a (merge opts {:href href})
                  content])))

(defn image-url [name]
  (str "/images/" name))

(defn image [name & opts]
  (let [opts (apply hash-map opts)
        opts (merge {:src (image-url name)} opts)]
    (hiccup/html [:img opts])))

(defn meta [he content]
  (hiccup/html [:meta {:http-equiv he :content content}]))

(defn script [path]
  [:script {:type "text/javascript" :src path}])

(defn stylesheet [path]
  [:link {:rel "stylesheet" :href path}])

(defn html5 [& body]
  (hiccup-page/html5 body))

(defn html [body]
  (hiccup/html body))
