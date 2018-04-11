(ns nsfw.popover
  (:require [cljfmt.core :as cf]))

(comment
  (println (cf/reformat-string
             "[pop/$wrap
{:content \"hello world\"
:enable-mouse-over? true}
             [:h1.text-center \"Hey ZK\"]]")))

(def css)
