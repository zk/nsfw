(defproject devguide "0.0.1"
  :description "NSFW Devguide"
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [nsfw "0.10.2"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]
  :repl-options {:init (load-file "reup.clj")}
  :cljsbuild
  {:builds {:dev
            {:source-paths ["src/cljs" "checkouts/nsfw/src/cljs"]
             :figwheel {:on-jsload "devguide.entry/reload-hook"}
             :compiler {:output-to "resources/public/cljs/app.js"
                        :output-dir "resources/public/cljs"
                        :optimizations :none
                        :source-map true
                        :main "devguide.entry"
                        :asset-path "/cljs"}}
            :prod
            {:source-paths ["src/cljs"]
             :compiler {:output-to "resources/public/cljs/app.js"
                        :optimizations :advanced
                        :main "devguide.entry"
                        :pretty-print false
                        :externs ["externs/dropzone.js"]}}}}
  :figwheel {:http-server-root "resources/public"
             :server-port 3448
             :css-dirs ["resources/public/css"]
             :repl false})
