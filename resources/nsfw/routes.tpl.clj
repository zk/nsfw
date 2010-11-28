(ns <(str project-name)>.routes
  (:use [net.cgrand.moustache :only (app)]
        [hiccup core]
        [nsfw render]))

(def routes
  (app [""] (fn [r] (render :text "hello from nsfw."))))