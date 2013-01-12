(defproject nsfw "0.4.3"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-stacktrace "0.2.5"]
                 [hiccup "1.0.2"]
                 [congomongo "0.3.3"]
                 [ring "1.1.6"]
                 [net.cgrand/moustache "1.1.0"]
                 [slingshot "0.10.2"]
                 [clj-http "0.5.8"]
                 [domina "1.0.0"]
                 [crate "0.2.1"]
                 [joda-time/joda-time "2.1"]
                 [org.clojars.franks42/cljs-uuid-utils "0.1.3"]
                 [oauth-clj "0.1.1"]
                 [org.clojure/google-closure-library "0.0-2029"]
                 [org.clojure/google-closure-library-third-party "0.0-2029"]
                 [watchtower "0.1.1"]]
  :source-paths ["src/clj" "examples/clj"]
  :test-paths ["test/clj"]
  :hooks [leiningen.cljsbuild]
  :jar-name "nsfw.jar"
  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "resources/test-js/test.js"
                           :optimizations :whitespace}
                :jar true}
               {:source-path "examples/cljs"
                :compiler {:output-to "resources/public/js/examples.js"
                           :optimizations :whitespace}}]})
