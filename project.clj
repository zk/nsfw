(defproject nsfw "0.11.56"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [clj-stacktrace "0.2.8"]
                 [cheshire "5.5.0"]
                 [hiccup "1.0.5"]
                 [congomongo "0.5.0"]
                 [ring "1.4.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.220"]
                 [com.cognitect/transit-clj "0.8.275"]
                 [aleph "0.4.1-beta2"]
                 [byte-transforms "0.1.4"]
                 [slingshot "0.12.2"]
                 [clj-http "1.1.2"]
                 [prismatic/dommy "1.1.0"]
                 [joda-time/joda-time "2.8.1"]
                 [oauth-clj "0.1.13"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [markdown-clj "0.9.67"]
                 [prismatic/plumbing "0.4.4"]
                 [clout "2.1.2"]
                 [org.pegdown/pegdown "1.4.1"]
                 [com.draines/postal "1.11.3"]
                 [bidi "1.23.1" :exclusions [org.clojure/clojure]]
                 [hashobject/hashids "0.2.0"]
                 [org.clojure/java.classpath "0.2.2"]
                 [garden "1.2.5" :exclusions [org.clojure/clojure]]
                 [reagent "0.6.0"]
                 [cljs-http "0.1.37"]
                 [camel-snake-kebab "0.3.2"]
                 [clj-jwt "0.1.1"]
                 [clj-mailgun "0.2.0"]
                 [clj-aws-s3 "0.3.10"]
                 [com.rpl/specter "0.13.2"]
                 [cljsjs/fastclick "1.0.6-0"]
                 [cljsjs/hammer "2.0.4-5"]]
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
