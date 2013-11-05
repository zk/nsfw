(defproject nsfw "0.6.29"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-stacktrace "0.2.5"]
                 [hiccup "1.0.2"]
                 [congomongo "0.3.3"]
                 [ring "1.2.0"]
                 [aleph "0.3.0-rc2"]
                 [net.cgrand/moustache "1.1.0"]
                 [slingshot "0.10.2"]
                 [clj-http "0.5.8"]
                 [prismatic/dommy "0.0.1"]
                 [joda-time/joda-time "2.1"]
                 [org.clojars.franks42/cljs-uuid-utils "0.1.3"]
                 [oauth-clj "0.1.1"]
                 [org.clojure/google-closure-library "0.0-2029-2"]
                 [org.clojure/google-closure-library-third-party "0.0-2029-2"]
                 [watchtower "0.1.1"]
                 [ring-reload-modified "0.1.1"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.pegdown/pegdown "1.2.1"]
                 [prismatic/plumbing "0.1.0"]
                 [clout "1.1.0"]
                 [org.pegdown/pegdown "1.4.1"]
                 [com.draines/postal "1.10.2"]]
  :source-paths ["src/clj" "examples/clj"]
  :test-paths ["test/clj"]
  :jar-name "nsfw.jar"
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs" "test/cljs"]
                :compiler {:output-to "resources/test.js"
                           :optimizations :whitespace}
                :jar true}]})
