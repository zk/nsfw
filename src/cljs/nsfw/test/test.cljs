(ns nsfw.test.test
  (:use [nsfw.util :only [log]])
  (:require [nsfw.test.dom :as dom]))

(defn main [] (dom/run))
