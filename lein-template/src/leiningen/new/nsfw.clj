(ns leiningen.new.nsfw
  (:use [leiningen.new.templates :only [renderer name-to-path ->files slurp-resource]]))

(def render (renderer "nsfw"))

(defn nsfw
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             ["Procfile" (render "Procfile" data)]
             ["bin/build" (render "build" data) :executable true]
             ["bin/dev" (render "dev" data) :executable true]
             ["project.clj" (render "project.clj" data)]
             [".gitignore" (render ".gitignore" data)]
             ["src/clj/{{sanitized}}/run.clj" (render "run.clj" data)]
             ["src/clj/{{sanitized}}/entry.clj" (render "entry.clj" data)]
             ["src/cljs/{{sanitized}}/app.cljs" (render "app.cljs" data)]
             ["src/scss/app.scss" (render "app.scss" data)]
             ["src/scss/nsfw.scss" (render "nsfw.scss" data)]
             ["README.md" (render "README.md" data)]

             ["src/scss/font-awesome.scss" (slurp-resource "font-awesome.scss")]
             ["resources/public/font/fontawesome-webfont.eot"
              (slurp-resource "fontawesome-webfont.eot")]
             ["resources/public/font/fontawesome-webfont.svg"
              (slurp-resource "fontawesome-webfont.svg")]
             ["resources/public/font/fontawesome-webfont.ttf"
              (slurp-resource "fontawesome-webfont.ttf")]
             ["resources/public/font/fontawesome-webfont.woff"
              (slurp-resource "fontawesome-webfont.woff")]
             ["resources/public/font/FontAwesome.otf"
              (slurp-resource "FontAwesome.otf")]
)))
