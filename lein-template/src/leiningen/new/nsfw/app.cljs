(ns {{name}}.app
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]))

(def $body (dom/$ "body"))

(def $page
  [:div
   [:h1 "Hello {{name}}."]
   [:p
    "This file can be found at " [:code "src/cljs/{{sanitized}}/app.cljs"] ". "
    "Any changes to this file (and any other cljs files) will automatically be "
    "reloaded in your browser."]])

(defn ^:export entry []
  (dom/append $body $page))