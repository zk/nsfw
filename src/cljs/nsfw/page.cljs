(ns nsfw.page
  (:require [nsfw.util :as util]
            [nsfw.ops :as ops]
            [reagent.core :as rea]
            [bidi.bidi :as bidi]
            [dommy.core :as dommy]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

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

(defn views->routes [views]
  ["" (->> views
           (mapcat (fn [[k {:keys [route routes]}]]
                     (->> (concat [route] routes)
                          (remove nil?)
                          (map (fn [route]
                                 {route k})))))
           (#(do (prn %) %))
           (reduce merge))])

(defn views->handlers [views]
  (->> views
       (map second)
       (map :handlers)
       (apply merge)))

(defn path-for [routes handler & [params]]
  (apply
    bidi/path-for
    routes
    handler
    (mapcat identity params)))

(defn push-route [routes handler & [params]]
  (push-path (path-for routes handler params)))

(defn link [{:keys [title on-click class]}]
  ^{:key title}
  [:a {:href "#"
       :class class
       :on-click (fn [e]
                   (.preventDefault e)
                   (on-click e)
                   e)}
   title])

(defn $nav [{:keys [!view-key bus]} children]
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

(defn nav-to-key [bus key & [route-params]]
  (ops/send bus ::nav {:view-key key
                       :route-params route-params}))

(defn scroll-top []
  (.scrollTo js/window 0 0))

(defn nav-handlers [{:keys [views routes]}]
  (let [routes (or routes
                   (views->routes views))]
    {::nav (fn [{:keys [!app routes view-key route-params]}]
             (let [{:keys [<state state] :as view} (get views view-key)]
               (push-route routes view-key route-params)
               (.scrollTo js/window 0 0)
               (if state
                 (swap! !app
                   #(-> %
                        (assoc-in [:view-key] view-key)
                        (assoc-in [:state] (state @!app route-params))))
                 (swap! !app
                   #(-> %
                        (assoc-in [:view-key] view-key))))
               (when <state
                 (go
                   (let [state (<! (<state @!app route-params))]
                     (swap! !app
                       #(-> %
                            (assoc-in [:view-key] view-key)
                            (assoc-in [:state] state))))))))}))

(defn dispatch-route [routes on-path]
  (let [{:keys [route-params handler] :as match}
        (bidi/match-route routes (pathname))]
    (when handler
      (on-path handler route-params))))

(defn dispatch-view [views routes !app bus]
  (dispatch-route routes
    (fn [handler route-params]
      (nav-to-key bus handler route-params))))

(defn render-view [views !app bus]
  (let [render (:render (get views (:view-key @!app)))]
    (when render
      [render (rea/cursor !app [:state]) bus])))

(defn start-popstate-handler [on-pop]
  (aset js/window "onpopstate" on-pop)
  (fn []
    (aset js/window "onpopstate" nil)))

(defn stop-popstate-handler [f]
  (f))

(defn render-key [!state path views]
  (let [view-key (get-in @!state path)
        $view (get views view-key)]
    (if $view
      $view
      (fn []))))

(defn viewport []
  {:width (or (.. js/document -documentElement -clientWidth)
              (.-innerWidth js/window)
              0)
   :height (or (.. js/document -documentElement -clientHeight)
               (.-innerHeight js/window)
               0)})

(defn aspect-ratio []
  (let [{:keys [width height]}
        (viewport)]
    (/ width height)))

(defn on-resize [f]
  (dommy/listen! js/window :resize f)
  (fn []
    (dommy/unlisten! js/window :resize f)))

(defn on-scroll [f]
  (dommy/listen! js/window :scroll f)
  (fn []
    (dommy/unlisten! js/window :scroll f)))

(defn throttle [f delta]
  (let [last (atom nil)
        to (atom nil)]
    (fn [& args]
      (cond
        (not @last) (do
                      (reset! last (util/now))
                      (reset! to nil)
                      (apply f args))
        (> @last 0) (let [now (util/now)]
                      (if (> (- now @last) delta)
                        (do
                          (reset! last now)
                          (apply f args))
                        (do
                          (js/clearTimeout @to)
                          (reset! to
                            (js/setTimeout
                              (fn []
                                (reset! last (+ delta @last))
                                (apply f args))
                              (- delta (- now @last)))))))))))

(defn debounce [f delay]
  (let [last (atom nil)
        to (atom nil)]
    (fn [& args]
      (when @to
        (js/clearTimeout @to))
      (reset! to
        (js/setTimeout
          (fn []
            (reset! to nil)
            (apply f args))
          delay)))))

(defn scroll-source [bus {throttle-ms :throttle
                          debounce-ms :debounce
                          op-key :op-key}]
  (let [f (fn [e]
            (ops/send bus (or op-key ::scroll)
              #_(.. e -target -pageYOffset)
              (.. js/window -scrollY)))
        f (if throttle-ms
            (throttle f throttle-ms)
            f)
        f (if debounce-ms
            (debounce f debounce-ms)
            f)]
    (on-scroll f)))

(defn high-density-screen? []
  (and (.-matchMedia js/window)
       (or
         (.-matches
           (.matchMedia js/window
             "only screen and (min-resolution: 124dpi), only screen and (min-resolution: 1.3dppx), only screen and (min-resolution: 48.8dpcm)"))
         (.-matches
           (.matchMedia js/window
             "only screen and (-webkit-min-device-pixel-ratio: 1.3), only screen and (-o-min-device-pixel-ratio: 2.6/2), only screen and (min--moz-device-pixel-ratio: 1.3), only screen and (min-device-pixel-ratio: 1.3)")))))

(defn attach-fastclick [& [$el]]
  (let [$el (or $el (aget js/document "body"))]
    (.attach js/FastClick $el)))
