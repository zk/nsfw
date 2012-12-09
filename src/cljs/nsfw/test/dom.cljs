(ns nsfw.test.dom
  (:use [nsfw.util :only [log]]
        [nsfw.dom :only [$]])
  (:require [crate.core :as crate]
            [nsfw.dom :as d]))

(def $t (-> ($ [:table])
            (d/style {:width :500px
                      :margin-left :auto
                      :margin-right :auto})))

(defn test [name el]
  (-> $t
      (d/append [:tr
                 (-> ($ [:td.label name])
                     (d/style {:width :100px}))
                 [:td el]])))

(test "append"
      (-> ($ [:h1])
          (d/append [:span "baz"])))

(test "style"
      (-> ($ [:h1 "foo"])
          (d/style {:color :blue
                    :font-family :sans-serif})))

(defn run []
  (log "hi")
  (-> ($ "body")
      (d/append $t)))
