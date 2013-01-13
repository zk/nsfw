(ns nsfw.refresher
  (:require [clojure.java.shell :as sh]
            [watchtower.core :as wt]))

(defn refresh-safari [url-str-match]
  (->> ["osascript"
        "tell application \"Safari\""
        "set windowList to every window"
        "repeat with aWindow in windowList"
        "set tabList to every tab of aWindow"
        "repeat with atab in tabList"
        (format "if (URL of atab contains \"%s\") then" url-str-match)
        "tell atab to do javascript \"window.location.reload()\""
        "end if"
        "end repeat"
        "end repeat"
        "end tell"]
       (interpose "-e")
       (apply sh/sh)))

(defn refresh-safari-on-js-change []
  (wt/watcher ["resources/public" "src/clj"]
              (wt/rate 100)
              (wt/on-change (fn [& args]
                              (println (->> args
                                            (interpose ", ")
                                            (apply str))
                                       "changed, refreshing safari")
                              (refresh-safari "localhost")))))

(defn -main [& dirs]
  (refresh-safari-on-js-change))
