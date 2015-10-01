(ns nsfw.page
  (:require [nsfw.util :as util]))

(defn start-app [handlers]
  (let [entry-key (:js-entry (util/page-data :env))]
    (if-let [handler (get handlers entry-key)]
      (handler (util/page-data :env))
      (do
        (println "Couldn't find handler for js-entry" entry-key)
        (fn [])))))

(defn stop-app [app]
  (app))

(defn reloader [handlers]
  (let [!app (atom (start-app handlers))]
    (fn []
      (when @!app
        (@!app))
      (reset! !app (start-app handlers)))))

(defn push-path [& parts]
  (let [new-path (apply str parts)
        cur-path (.-pathname js/window.location)]
    (when-not (= new-path cur-path)
      (.pushState js/window.history nil nil new-path))))
