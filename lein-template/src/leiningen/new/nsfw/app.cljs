(ns {{name}}.app
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]))

(def $body (dom/$ "body"))

(dom/append
 $body
 [:div
  [:h1 "Hello NSFW"]
  [:p
   "This file can be found at " [:code "src/cljs/<project name>/app.cljs"] ". "
   "Any changes to this file (and any other cljs files) will automatically be "
   "reloaded in your browser."]])
