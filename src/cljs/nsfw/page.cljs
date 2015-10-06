(ns nsfw.page
  (:require [nsfw.util :as util]))

(defn start-app [handlers]
  (let [entry-key (:js-entry (util/page-data :env))]
    (if-let [handler (get handlers entry-key)]
      (handler
        (util/page-data :env))
      (do
        (println "Couldn't find handler for js-entry" entry-key)
        (fn [])))))

(defn stop-app [app]
  (app))

(defn reloader [gen-handlers]
  (let [!app (atom (start-app (gen-handlers)))]
    (fn []
      (when @!app
        (@!app))
      (reset! !app (start-app (gen-handlers))))))

(defn push-path [& parts]
  (let [new-path (apply str parts)
        cur-path (.-pathname js/window.location)]
    (when-not (= new-path cur-path)
      (.pushState js/window.history nil nil new-path))))

(defn pathname []
  (.. js/window -location -pathname))

(defn href []
  (.. js/window -location -href))
