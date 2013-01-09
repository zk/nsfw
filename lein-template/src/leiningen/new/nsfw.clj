(ns leiningen.new.nsfw
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer "nsfw"))

(defn nsfw
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["src/clj/{{sanitized}}/server.clj" (render "server.clj" data)]
             ["src/cljs/{{sanitized}}/app.cljs" (render "app.cljs" data)]
             ["resources/public/js/app.js" ""]
             ["resources/public/css/app.css" (render "app.css" data)]
             ["resources/public/css/bootstrap-responsive.min.css" (render "bootstrap-responsive.min.css" data)]
             ["resources/public/css/bootstrap.min.css" (render "bootstrap.min.css" data)])))
