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
    (.close s)
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
     (fn [o]
       (prn "ERR")
       (on-error (.-target o)))

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
     (fn [o]
       (prn "WS ON CLOSE")
       (on-close (.-target o)))}))

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

(defn send-state [devbus-conn {:keys [ref] :as state-item}]
  (send devbus-conn
    [(merge
       (select-keys
         state-item
         [:title :section])
       (when ref
         {:value (deref ref)}))]))

(def !state (atom nil))

(defn realize-state-item
  [{:keys [ref] :as si}]
  (if ref
    (-> si
        (dissoc :ref)
        (assoc :value (deref ref))
        (assoc :updated-at (nu/now)))
    si))

(defn realize-state-items [sis]
  (->> sis
       (map realize-state-item)
       vec))

(def app-client-handlers
  {:request-state-items
   (fn [devbus-conn]
     (send
       devbus-conn
       [:state-items
        (->> @!state
             :state-items
             realize-state-items)]))
   :heartbeat
   (fn [devbus-conn]
     (prn "got heartbeat"))})

(defn app-client [url]
  (when (:devbus-conn @!state)
    (close (:devbus-conn @!state)))
  (let [devbus-conn
        (handlers-client
          url
          app-client-handlers
          {:on-open
           (fn [devbus-conn]
             (send devbus-conn
               [:state-items
                (->> @!state
                     :state-items
                     realize-state-item)]))
           :on-close (fn [devbus-conn]
                       (prn "devbus client closed"))})]
    (swap! !state assoc :devbus-conn devbus-conn)))


(defn debug-client [url
                    on-state-items]
  (handlers-client
    url
    {:state-items
     (fn [_ state-items]
       (on-state-items state-items))
     :heartbeat (fn [_]
                  (prn "heartbeat"))}
    {:on-open (fn [db]
                (send
                  db
                  [:request-state-items]))}))

(defn shutdown []
  (prn "devbus shutdown")
  (when-let [devbus-conn (:devbus-conn @!state)]
    (prn "shutdown devbus-conn")
    (close devbus-conn))
  (doseq [{:keys [ref] :as item} (:state-items @!state)]
    (when ref
      (remove-watch
        ref
        :devbus)))
  (reset! !state nil))

(defn distinct-by
  ([f coll]
   (let [step (fn step [xs seen]
                (lazy-seq
                  ((fn [[x :as xs] seen]
                     (when-let [s (seq xs)]
                       (let [v (f x)]
                         (if (contains? seen v)
                           (recur (rest s) seen)
                           (cons x (step (rest s) (conj seen v)))))))
                   xs seen)))]
     (step coll #{}))))

(defn process-state-items [sis]
  (->> sis
       (map (fn [{:keys [title section ref value key]}]
              (merge
                {:key (if key
                        (name key)
                        (str section "." title))}
                (when ref
                  {:ref ref})
                (when value
                  {:value value}))))))

(defn trackmult [state-items]
  (let [state-items (process-state-items state-items)
        new-state-items (set
                          (distinct-by
                            :key
                            (concat
                              state-items
                              (:state-items @!state))))]

    (swap! !state assoc :state-items new-state-items)

    (when-let [devbus-conn (:devbus-conn @!state)]
      (send devbus-conn
        [:state-items
         (->> state-items
              realize-state-items)]))

    (doseq [{:keys [ref] :as item} (->> state-items
                                        (filter :ref))]
      (when ref
        (add-watch ref :devbus
          (fn [_ _ _ state]
            (when-let [devbus-conn (:devbus-conn @!state)]
              (send devbus-conn
                [:state-items
                 [(realize-state-item item)]]))))))))

(defn track [si]
  (trackmult [si]))
