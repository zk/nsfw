(defproject nsfw "0.8.13"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2665"]
                 [clj-stacktrace "0.2.5"]
                 [cheshire "5.2.0"]
                 [hiccup "1.0.2"]
                 [congomongo "0.3.3"]
                 [ring "1.3.2"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [com.cognitect/transit-cljs "0.8.192"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [aleph "0.4.0-beta3"]
                 [byte-transforms "0.1.3"]
                 [slingshot "0.10.2"]
                 [clj-http "0.5.8"]
                 [prismatic/dommy "1.0.0"]
                 [joda-time/joda-time "2.1"]
                 [oauth-clj "0.1.1"]
                 [watchtower "0.1.1"]
                 [org.clojure/tools.namespace "0.2.7"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [markdown-clj "0.9.62"]
                 [prismatic/plumbing "0.1.0"]
                 [clout "1.1.0"]
                 [org.pegdown/pegdown "1.4.1"]
                 [com.draines/postal "1.10.2"]
                 [om "0.8.0-rc1"]
                 [sablono "0.3.1"]
                 [bidi "1.15.0" :exclusions [org.clojure/clojure]]
                 [hashobject/hashids "0.2.0"]
                 [org.clojure/java.classpath "0.2.2"]
                 [prismatic/schema "0.3.7" :exclusions [potemkin]]]
  :repl-options {:init (load-file "reup.clj")}
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :jar-name "nsfw.jar"
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :compiler {:output-to "resources/test.js"
                           :optimizations :whitespace}
                :jar true}]})
