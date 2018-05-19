(ns nsfw.devbus
  (:require [nsfw.util :as nu]))

(defn ws [url
          {:keys [on-open
                  on-close
                  on-message
                  on-error]}]
  (let [s (js/WebSocket. url)]
    (set! (.-onopen s) on-open)
    (set! (.-onclose s) on-close)
    (set! (.-onmessage s) on-message)
    (set! (.-onerror s) on-error)
    s))

(defn close [s]
  (when s
    (.close s)))

(defn handlers-client [url handlers
                       & [{:keys [on-open
                                  on-error
                                  on-close]
                           :or {on-open (fn [_])
                                on-error (fn [_])
                                on-close (fn [_])}}]]
  (ws
    url
    {:on-open
     (fn [o]
       (on-open (.-target o)))

     :on-error
     (fn [o] (on-error (.-target o)))

     :on-message
     (fn [o]
       (let [res (try
                   (nu/from-transit (.-data o))
                   (catch js/Error e nil))]
         #_(prn "DEBUG" (.-data o))
         (when res
           #_(nu/pp-str res)
           (let [[key & args] res
                 handler (get handlers key)]
             (when handler
               (apply handler (.-target o) args))))))

     :on-close
     (fn [o] (on-close (.-target o)))}))

(deftype ObjectHandler []
  Object
  (tag [this v] "jsobj")
  (rep [this v] (str v))
  (stringRep [this v] (str v)))

(defn send [s data]
  (when s
    (try
      (.send s (nu/to-transit data))
      (catch js/Error e
        #_(.error js/console (str "Error serializing"
                                  (pr-str data)))
        nil))))

(defn send-state [db {:keys [ref] :as state-item}]
  (send db
    [(merge
       (select-keys
         state-item
         [:title :section])
       (when ref
         {:value (deref ref)}))]))

(defn client [url
              {:keys [mobile-states
                      on-mobile-states]
               :as opts}]
  (let [db
        (handlers-client
          url
          {:request-mobile-states
           (fn [db]
             (send
               db
               [:full-mobile-states
                (->> mobile-states
                     (map (fn [{:keys [ref] :as item}]
                            (merge
                              (select-keys
                                item
                                [:title :section :value])
                              (when ref
                                {:value (deref ref)})))))]))
           :full-mobile-states
           (fn [db states]
             (when on-mobile-states
               (on-mobile-states states)))
           :heartbeat (fn [_]
                        (prn "devbus heartbeat"))}
          (merge
            (when mobile-states
              {:on-open
               (fn [ws]
                 (send
                   ws
                   [:full-mobile-states
                    (->> mobile-states
                         (map (fn [{:keys [ref] :as item}]
                                (merge
                                  (select-keys
                                    item
                                    [:title :section :value])
                                  (when ref
                                    {:value (deref ref)})))))]))
               :on-close
               (fn [ws]
                 (doseq [{:keys [ref]} mobile-states]
                   (when ref
                     (remove-watch ref :devbus))))})
            opts))]
    (doseq [{:keys [ref] :as item} mobile-states]
      (when ref
        (add-watch
          ref
          :devbus
          (fn [_ _ _ state]
            (send db
              [:full-mobile-states
               [(merge
                  (select-keys
                    item
                    [:title :section :value])
                  {:value state})]])))))
    db))

(def !state (atom nil))

(defn init [url]
  (when (:db @!state)
    (close (:db @!state)))
  (let [db (handlers-client
             url
             {:request-mobile-states
              (fn [db]
                (send
                  db
                  [:full-mobile-states
                   (->> @!state
                        :ref-items
                        (map (fn [{:keys [ref] :as item}]
                               (merge
                                 (select-keys
                                   item
                                   [:title :section :value])
                                 (when ref
                                   {:value (deref ref)})))))]))
              :heartbeat
              (fn [db]
                (prn "got heartbeat"))}
             {:on-open
              (fn [db]
                (send db
                  [:full-mobile-states
                   (->> @!state
                        :ref-items
                        (map (fn [{:keys [ref] :as item}]
                               (merge
                                 (select-keys
                                   item
                                   [:title :section :value])
                                 (when ref
                                   {:value (deref ref)})))))]))
              :on-close (fn [db]
                          (prn "devbus client closed"))})]
    (swap! !state assoc :db db)))

(defn shutdown []
  (prn "devbus shutdown")
  (when-let [db (:db @!state)]
    (prn "shutdown db")
    (close db))
  (doseq [{:keys [ref] :as item} (:ref-items @!state)]
    (when ref
      (remove-watch
        ref
        :devbus)))
  (reset! !state nil))

(defn add-items [ref-items]
  #_(prn "add items" ref-items)
  (swap! !state update :ref-items
    #(set
       (concat
         %
         ref-items)))

  (when-let [db (:db @!state)]
    #_(prn "db there")
    (send db
      [:full-mobile-states
       (->> @!state
            :ref-items
            (map (fn [{:keys [ref] :as item}]
                   (merge
                     (select-keys
                       item
                       [:title :section :value])
                     (when ref
                       {:value (deref ref)})))))]))
  (doseq [{:keys [ref] :as item} (:ref-items @!state)]
    (when ref
      (add-watch ref :devbus
        (fn [_ _ _ state]
          #_(prn (:title item) "changed" state (:db @!state))
          (when-let [db (:db @!state)]
            (send db
              [:full-mobile-states
               [(merge
                  (select-keys
                    item
                    [:title :section :value])
                  {:value state})]])))))))
