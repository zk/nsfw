(ns devguide.markdown
  (:require [nsfw.util :as util]
            [nsfw.page :as page]))

(defn $render [!app bus]
  [:div.dg-section
   [:h1 "Markdown Handling"]
   [:p "Markdown can be a nice alternative to embedding your content in Clojure source."]
   [:pre "asdf asdf asdf"]])

(def views
  {::markdown {:render $render
               :route "/markdown"
               :nav-title "Markdown"}})
