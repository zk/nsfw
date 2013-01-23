(ns nsfw.refresher
  (:require [clojure.java.shell :as sh]
            [watchtower.core :as wt]))

(defn refresh-safari []
  (->> ["osascript"
        "tell application \"Safari\" to tell its first document"
        "set its URL to (get its URL)"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defn refresh-chrome
  "Refreshes first tab of chrome"
  []
  (->> ["osascript"
        "tell application \"Google Chrome\" to tell the active tab of its first window"
        "reload"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defn refresh-on-change [refresh-fn]
  (wt/watcher ["resources/public" "src/clj"]
              (wt/rate 100)
              (wt/on-change (fn [& args]
                              (println (->> args
                                            (interpose ", ")
                                            (apply str))
                                       "changed, refreshing browser")
                              (refresh-fn)))))

(defn -main [& [browser]]
  (condp = browser
    "safari" (refresh-on-change refresh-safari)
    (refresh-on-change refresh-chrome) ; default to chrome
    ))