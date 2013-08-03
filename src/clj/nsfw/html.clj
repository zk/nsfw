(ns nsfw.html
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as hiccup-page])
  (:import [org.pegdown PegDownProcessor]
           [org.pegdown Parser]))

(defn href
  "ex. (href \"http://google.com\" \"Google!\" :rel \"nofollow\")"
  [href content & opts]
  (let [opts (apply hash-map opts)]
    [:a (merge opts {:href href})
     content]))

(defn script [path]
  [:script {:type "text/javascript" :src path}])

(defn stylesheet [path]
  [:link {:rel "stylesheet" :href path}])

(defn css-rule [rule]
  (let [sels (reverse (rest (reverse rule)))
        props (last rule)]
    (str (apply str (interpose " " (map name sels)))
         "{" (apply str (map #(str (name (key %))
                                   ":"
                                   (if (keyword? (val %))
                                     (name (val %))
                                     (val %))
                                   ";") props)) "}")))

(defn css
  "ex.
    (css [:h1 {:height :20px}] [:h2 {:height :10px}])
    => h1{height:20px;}h2{height:10px}"
  [& rules]
  (apply str (map css-rule rules)))

(defn embed-css
  "Quick and dirty dsl for inline css rules, similar to hiccup.

   ex. `(css [:h1 {:color \"blue\"}] [:div.content p {:text-indent \"1em\"}])`
   => `h1 {color: blue;} div.content p {text-indent: 1em;}`"
  [& rules]
  [:style {:type "text/css"}
   (apply str (map css-rule rules))])

(defn html5 [& body]
  (hiccup-page/html5 body))

(defn html [body]
  (hiccup/html body))

(defn markdown [s]
  (let [pd (PegDownProcessor.)]
      (.markdownToHtml pd s)))