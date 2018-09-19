(ns nsfw.fuse
  (:require [cljsjs.fuse]))

(defn create-index [data opts]
  (js/Fuse.
    (clj->js data)
    (clj->js opts)))

(defn search [index query]
  (js->clj (.search index query) :keywordize-keys true))

(defn test-fuse []
  (let [index (create-index
                [{:id "foo"
                  :title "The old man and the sea"
                  :author "Hemingway"}
                 {:id "bar"
                  :title "1984"
                  :author "George Oldwell"}]
                {:threshold 0.3
                 :location 0
                 :distance 100,
                 :maxPatternLength 32,
                 :minMatchCharLength 1
                 :keys ["title" "author"]})]
    (search index "th")))
