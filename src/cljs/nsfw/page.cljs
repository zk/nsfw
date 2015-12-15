(ns nsfw.page
  (:require [nsfw.util :as util]
            [nsfw.ops :as ops]
            [bidi.bidi :as bidi]))

(defn start-app [handlers]
  (let [entry-key (try
                    (:js-entry (util/page-data :env))
                    (catch js/Error e
                      nil))]
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

(defn fq-url [& parts]
  (let [loc (.-location js/window)]
    (apply str
      (.-protocol loc)
      "//"
      (.-host loc)
      parts)))

(defn dispatch-route [routes on-path]
  (let [{:keys [route-params handler] :as match}
        (bidi/match-route routes (pathname))]
    (when handler
      (on-path handler route-params))))

(defn path-for [routes handler]
  (bidi/path-for routes handler))

(defn push-route [routes handler]
  (push-path (path-for routes handler)))

(defn link [{:keys [title on-click class]}]
  ^{:key title}
  [:a {:href "#"
       :class class
       :on-click (fn [e]
                   (.preventDefault e)
                   (on-click e)
                   e)}
   title])

(defn nav [{:keys [!view-key bus]} children]
  [:ul.nav
   (->> children
        (map (fn [{:keys [title view-key]}]
               ^{:key view-key}
               [:li
                (link {:title title
                       :class (str "nav-link"
                                (when (= @!view-key view-key) " active"))
                       :on-click
                       (fn [e]
                         (ops/send bus ::nav {:view-key view-key}))})]))
        doall)])

(defn nav-handlers [{:keys [views routes]}]
  {::nav (fn [{:keys [!app view-key]}]
           (let [{:keys [route]} (get views view-key)]
             (when route
               (push-route routes view-key)
               (.scrollTo js/window 0 0))
             (swap! !app assoc-in [:view-key] view-key)))})
