(ns {{name}}.app
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]))

(def $body (dom/$ "body"))

(dom/append $body [:h1 "Hello NSFW"])