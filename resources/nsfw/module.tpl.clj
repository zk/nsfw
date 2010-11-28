(ns <(str module-ns)>
  (:use [net.cgrand.moustache :only (app)]
        [hiccup core [page-helpers :only (doctype)]]
        [nsfw util render]))

(defn index [req]
  (render :text "<(str module-ns)>/index"))

(def routes
  (app [""] index))