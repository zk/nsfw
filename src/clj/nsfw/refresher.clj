(ns nsfw.refresher
  (:require [clojure.java.shell :as sh]
            [watchtower.core :as wt]))

(defn refresh-safari-on-js-change []
  (wt/watcher "resources/public"
              (wt/rate 100)
              (wt/on-change (fn [& args]
                              (println
                               (->> args
                                    (interpose ", ")
                                    (apply str))
                               "changed, reloading")
                              (sh/sh "./bin/reload-safari")))))

(defn -main [& args]
  (refresh-safari-on-js-change))
