(ns nsfw.devbus
  (:require [nsfw.util :as nu]
            [taoensso.timbre :as log :include-macros true]
            [cljs.test
             :refer-macros [deftest is
                            testing run-tests
                            async]]
            [cljs.core.async
             :refer [<! chan put! timeout close!]
             :refer-macros [go go-loop]]))

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

(defn close-ws [s]
  (when s
    (.close s)))

(defn <ws [url]
  (let [ch (chan)
        socket (ws
                 url
                 {:on-open (fn [o]
                             (put! ch {:event-type :open
                                       :event o}))
                  :on-error (fn [o]
                              (put! ch {:event-type :error
                                        :event o})
                              (close! ch))
                  :on-message (fn [o]
                                (put! ch {:event-type :message
                                          :event o}))
                  :on-close (fn [o]
                              (put! ch {:event-type :close
                                        :event o})
                              (close! ch))})]

    {:ch ch
     :socket socket}))

(defn pws [url {:keys [on-open
                       on-message
                       on-close
                       on-error]
                :or {on-open (fn [_])
                     on-message (fn [_])
                     on-close (fn [_])
                     on-error (fn [_])}}]
  (let [!run? (atom true)
        !socket (atom nil)
        out {:close (fn []
                      (reset! !run? false)
                      (when @!socket
                        (.close @!socket)))
             :send (fn [data]
                     (.send @!socket data))
             :!socket !socket}]
    (go-loop []
      (let [{:keys [ch socket]} (<ws url)]
        (reset! !socket socket)
        (loop []
          (let [{:keys [event-type event] :as message} (<! ch)]
            (condp = event-type
              :open (on-open event out)
              :message (on-message event out)
              :close (on-close event out)
              :error (on-error event out))
            (when (get #{:open :message} event-type)
              (recur))))


        (when @!socket
          (log/debug "Closing socket")
          (.close @!socket))
        (log/info "Connection lost")
        (when @!run?
          (<! (timeout 1000))
          (log/info "Reconnecting")
          (recur))
        (log/info "Stopping pws")))
    out))

(defn close-pws [{:keys [close] :as pws}]
  (when pws
    (close)))

(defn pws-open? [{:keys [!socket]}]
  (when-let [s @!socket]
    (let [state (.-readyState s)]
      (= (.-OPEN s) state))))

(deftest test-pws-close
  (let [p (pws "ws://localhost:44100/devbus"
            {:on-close (fn [] (prn "close"))
             :on-error (fn [] (prn "error"))})]
    (async done
      (go
        (<! (timeout 500))
        (close-pws p)
        (is (not (pws-open? p)))
        (done)))))

(defn stop-client [conn]
  (close-pws conn))

(defn handlers-client [url handlers
                       & [{:keys [on-open
                                  on-error
                                  on-close]
                           :or {on-open (fn [_])
                                on-error (fn [_])
                                on-close (fn [_])}}]]
  (pws
    url
    {:on-open
     (fn [o pws-client]
       (on-open (.-target o) pws-client))

     :on-error
     (fn [o]
       (on-error (.-target o)))

     :on-message
     (fn [o pws-client]
       (let [res (try
                   (nu/from-transit (.-data o))
                   (catch js/Error e
                     (log/debug "Error decoding message" (.-data o))
                     nil))]
         (when res
           (let [[key & args] res
                 handler (get handlers key)]
             (when handler
               (handler pws-client args))))))

     :on-close
     (fn [o]
       (prn "oc")
       (on-close (.-target o)))}))

(deftype ObjectHandler []
  Object
  (tag [this v] "jsobj")
  (rep [this v] (str v))
  (stringRep [this v] (str v)))

(defn send [{sendpws :send :as pws} data]
  (when-not sendpws
    (nu/throw-str (str "send requires pws object, got " pws)))
  (when sendpws
    (try
      (sendpws (nu/to-transit data))
      (catch js/Error e
        (log/info "Error encoding" data)
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

(defn app-client [url]
  (when (:devbus-conn @!state)
    (stop-client (:devbus-conn @!state)))
  (let [devbus-conn
        (handlers-client
          url
          {:request-state-items
           (fn [devbus-conn]
             (send
               devbus-conn
               [:state-items
                (->> @!state
                     :state-items
                     realize-state-items)]))
           :request-test-states
           (fn [devbus-conn]
             (send
               devbus-conn
               [:test-states (-> @!state :test-states)]))

           :load-test-state
           (fn [_ test-state]
             (when-let [on-receive (:on-receive @!state)]
               (on-receive test-state)))
           :heartbeat
           (fn [devbus-conn]
             (prn "got heartbeat"))}
          {:on-open
           (fn [ws devbus-conn]
             (log/debug "Startup broadcast for :state-items, :test-states")
             (send devbus-conn
               [:state-items
                (->> @!state
                     :state-items
                     realize-state-items)])
             (send devbus-conn
               [:test-states
                (->> @!state
                     :test-states)]))})]
    (swap! !state assoc :devbus-conn devbus-conn)))

(defn debug-client [url
                    {:keys [on-state-items
                            on-test-states
                            on-open]
                     :as opts}]
  (handlers-client
    url
    {:state-items
     (fn [_ [state-items]]
       (on-state-items state-items))
     :test-states
     (fn [_ [test-states]]
       (on-test-states test-states))
     :heartbeat (fn [_]
                  (prn "heartbeat"))}
    (merge
      opts
      {:on-open (fn [db pws]
                  (log/debug "Connected, broadcasting"
                    (->> [:request-state-items
                          :request-test-states]
                         (interpose ", ")
                         (apply str)))
                  (send pws [:request-state-items])
                  (send pws [:request-test-states])
                  (when on-open
                    (on-open db pws)))})))

(defn shutdown []
  (log/info "Devbus shutdown")
  (when-let [devbus-conn (:devbus-conn @!state)]
    (log/info "Shutting down devbus-conn")
    (stop-client devbus-conn))
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
                        (str (namespace key) "." (name key))
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

    (doseq [{:keys [ref key] :as item} (->> state-items
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

(defn atom? [o]
  (satisfies? IAtom o))

(defn watch [k v]
  (try
    (track
      (merge
        {:key k}
        (if (atom? v)
          {:ref v}
          {:value v})))
    (catch js/Error e
      (println "Couldn't watch" k "-" e))))

(defn hook-test-states
  [test-states
   on-receive]
  (swap! !state
    assoc
    :test-states test-states
    :on-receive on-receive)
  (when-let [conn (:devbus-conn @!state)]
    (send conn [:test-states test-states])))
