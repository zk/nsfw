(defproject nsfw "0.12.15"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.946"]
                 [clj-stacktrace "0.2.8"]
                 [cheshire "5.5.0"]
                 [hiccup "2.0.0-alpha1"]
                 [congomongo "0.5.1"]
                 [ring "1.6.0"]
                 [org.clojure/core.async "0.4.474"]
                 [com.cognitect/transit-clj "0.8.303"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [aleph "0.4.4"]
                 [byte-transforms "0.1.4"]
                 [slingshot "0.12.2"]
                 [clj-http "3.8.0"]
                 [prismatic/dommy "1.1.0"]
                 [joda-time/joda-time "2.9.9"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [prismatic/plumbing "0.5.5"]
                 [org.pegdown/pegdown "1.4.1"]
                 [bidi "2.1.3"]
                 [hashobject/hashids "0.2.0"]
                 [org.clojure/java.classpath "0.2.2"]
                 [garden "1.3.5"]
                 [reagent "0.7.0"]
                 [cljs-http "0.1.45"]
                 [camel-snake-kebab "0.4.0"]
                 [clj-jwt "0.1.1"]
                 [clj-aws-s3 "0.3.10"]
                 [cljsjs/aws-sdk-js "2.94.0-0"]
                 [com.rpl/specter "1.1.0"]
                 [cljsjs/fastclick "1.0.6-0"]
                 [cljsjs/hammer "2.0.4-5"]
                 [cljsjs/google-maps "3.18-1"]
                 #_[cljsjs/mapbox-gl "0.42.2-0"]
                 [reaver "0.1.2"]
                 [hickory "0.7.1"]
                 [org.apache.commons/commons-compress "1.16.1"]
                 [cljsjs/showdown "1.4.2-0"]
                 [hikari-cp "1.8.3"]
                 [org.postgresql/postgresql "42.1.4"]
                 [honeysql "0.9.1"]
                 [nilenso/honeysql-postgres "0.2.3"]
                 [migratus "1.0.1"]
                 [cljsjs/youtube "1.1-0"]

                 [com.googlecode.libphonenumber/libphonenumber "8.9.6"]
                 [cljsjs/libphonenumber "8.4.1-1"]

]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :repl-options {:init (load-file "reup.clj")}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :jar-name "nsfw.jar"
  :cljsbuild
  {:builds {:dev
            {:source-paths ["src/cljs" "src/cljc"]
             :compiler {:output-to "target/cljs/nsfw.js"
                        :output-dir "target/cljs"
                        :optimizations :none
                        :source-map true
                        :main "rx.entry"
                        :asset-path "/cljs"
                        :recompile-dependents false
                        }}}})
