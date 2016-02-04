(ns nsfw.hammer
  (:require [clojure.string :as str]
            [reagent.core :as rea]
            [com.hammerjs]
            [nsfw.page :as page]))


(defn bind-touch-event [el evt handler]
  (let [ht (js/Hammer. el #js {})]
    (.set (.get ht "pan") #js {:direction js/Hammer.DIRECTION_HORIZONTAL})
    (.set (.get ht "swipe") #js {:direction js/Hammer.DIRECTION_HORIZONTAL})
    (.on ht evt handler)
    (fn []
      (.off ht evt handler)
      (.destroy ht))))

(defn on-touch-events [el evt-handlers]
  (let [stop-fns (->> evt-handlers
                      (map (fn [[k v]]
                             (let [event-name (-> k name
                                                  (str/replace #"^on-" ""))]
                               (bind-touch-event
                                 el
                                 event-name
                                 v))))
                      doall)]
    (fn []
      (doseq [f stop-fns]
        (f)))))


(def with-touch
  (with-meta
    (fn [events component]
      component)
    {:component-did-mount (fn [this]
                            (let [events (-> this
                                             rea/argv
                                             second)
                                  el (rea/dom-node this)
                                  stop-events-fn (on-touch-events el events)]
                              (rea/set-state this {:stop-events-fn stop-events-fn})))
     :component-will-unmount (fn [this]
                               (when-let [f (:stop-events-fn (rea/state this))]
                                 (f)))}))
