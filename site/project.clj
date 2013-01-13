(defproject nsfw-site "0.1.0"
  :description "A Clojure web app using NSFW."
  :min-lein-version "2"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [nsfw "0.4.3"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :plugins [[lein-cljsbuild "0.2.10"]]
  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/app.js"
                           :optimizations :whitespace}
                :jar true}]})