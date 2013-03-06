(ns leiningen.new.nsfw
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer "nsfw"))

(defn nsfw
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             ["Procfile" (render "Procfile" data)]
             ["bin/build" (render "build" data)]
             ["project.clj" (render "project.clj" data)]
             [".gitignore" (render ".gitignore" data)]
             ["src/clj/{{sanitized}}/run.clj" (render "run.clj" data)]
             ["src/clj/{{sanitized}}/entry.clj" (render "entry.clj" data)]
             ["src/cljs/{{sanitized}}/app.cljs" (render "app.cljs" data)]
             ["src/scss/app.scss" (render "app.scss" data)]
             ["src/scss/nsfw.scss" (render "nsfw.scss" data)]
             ["README.md" (render "README.md" data)])))
