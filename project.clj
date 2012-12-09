(defproject nsfw "0.4.2"
  :description "No Such Framework -- Experimental"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-stacktrace "0.2.5"]
                 [hiccup "1.0.2"]
                 [congomongo "0.3.3"]
                 [ring "1.1.6"]
                 [net.cgrand/moustache "1.1.0"]
                 [slingshot "0.10.2"]
                 [clj-http "0.5.8"]
                 [crate "0.2.1"]]
                 [domina "1.0.0"]
                 [crate "0.2.1"]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :hooks [leiningen.cljsbuild]
  :jar-name "nsfw.jar"
  :cljsbuild {:builds [{:source-path "src/cljs"
                        :jar true}]})
