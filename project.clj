(defproject nsfw "0.11.82"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [clj-stacktrace "0.2.8"]
                 [cheshire "5.5.0"]
                 [hiccup "2.0.0-alpha1"]
                 [congomongo "0.5.0"]
                 [ring "1.4.0"]
                 [org.clojure/core.async "0.3.443"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [aleph "0.4.3"]

                 [byte-transforms "0.1.4"]
                 [slingshot "0.12.2"]
                 [clj-http "3.6.0"]
                 [prismatic/dommy "1.1.0"]
                 [joda-time/joda-time "2.8.1"]
                 [oauth-clj "0.1.13"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [markdown-clj "0.9.67"]
                 [prismatic/plumbing "0.5.5"]
                 [clout "2.1.2"]
                 [org.pegdown/pegdown "1.4.1"]
                 [com.draines/postal "1.11.3"]
                 [bidi "1.23.1" :exclusions [org.clojure/clojure]]
                 [hashobject/hashids "0.2.0"]
                 [org.clojure/java.classpath "0.2.2"]
                 [garden "1.2.5" :exclusions [org.clojure/clojure]]
                 [reagent "0.7.0"]
                 [cljs-http "0.1.37"]
                 [camel-snake-kebab "0.3.2"]
                 [clj-jwt "0.1.1"]
                 [clj-mailgun "0.2.0"]
                 [clj-aws-s3 "0.3.10"]
                 [com.rpl/specter "1.0.0"]
                 [cljsjs/fastclick "1.0.6-0"]
                 [cljsjs/hammer "2.0.4-5"]
                 [cljsjs/google-maps "3.18-1"]

                 [reaver "0.1.2"]
                 [hickory "0.7.1"]
                 [org.apache.commons/commons-compress "1.14"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :repl-options {:init (load-file "reup.clj")}
  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj"]
  :jar-name "nsfw.jar"
  :cljsbuild
  {:builds {:dev
            {:source-paths ["src/cljs"]
             :compiler {:output-to "target/cljs/nsfw.js"
                        :output-dir "target/cljs"
                        :optimizations :none
                        :source-map true
                        :main "rx.entry"
                        :asset-path "/cljs"
                        :recompile-dependents false
                        }}}})
