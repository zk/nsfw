(defproject {{name}} "0.1.0"
  :description "A Clojure web app using NSFW."
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [nsfw "0.5.14"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :cljsbuild {:builds
              {:dev  {:source-paths ["src/cljs"]
                      :compiler {:output-to "resources/public/js/app.js"
                                 :optimizations :whitespace
                                 :pretty-print true}
                      :jar true}

               :prod {:source-paths ["src/cljs"]
                      :compiler {:output-to "resources/public/js/app.js"
                                 :optimizations :advanced
                                 :pretty-print false}
                      :jar true}}})