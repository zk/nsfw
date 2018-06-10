(ns nsfw.markdown
  #? (:clj
      (:require [clojure.string :as str]))
  #? (:cljs
      (:require [clojure.string :as str]
                [cljsjs.showdown]))
  #? (:clj
      (:import [org.pegdown PegDownProcessor Extensions])))

#? (:cljs (def converter (js/showdown.Converter.)))

#? (:clj
    (defn render-html
      "Render HTML from markdown"
      [s]
      (let [pgp (PegDownProcessor. Extensions/ALL)]
        (when s
          (.markdownToHtml pgp (str/replace s #"!\[\]" "![ ]")))))
    :cljs
    (defn render-html
      [s]
      (when s
        (.makeHtml converter s))))
