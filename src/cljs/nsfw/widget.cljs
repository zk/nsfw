(ns nsfw.widget
  (:require [nsfw.dom :as $]
            [nsfw.bind :as bind]
            [nsfw.util :as util]
            [nsfw.pubsub :as ps]
            [clojure.string :as str]))

(defn html
  ([m struct]
     (let [struct (if (fn? struct)
                    (struct m)
                    struct)]
       (assoc m :init
              (fn [opts]
                ($/node struct))))))

(defn html [m f]
  (assoc m :init (fn [state ctx]
                   ($/node (f state ctx)))))

(defn el
  [m q]
  (assoc m :init (fn [opts]
                   (first ($/query q)))))

(defn event
  ([m sel-ev transform]
     (event m :pub sel-ev transform))
  ([m type sel-ev transform]
     (let [events (:events m)
           [sel ev] ($/parse-sel-ev sel-ev)
           events (concat events [{:selector sel
                                   :event ev
                                   :transform transform
                                   :type type}])]
       (assoc m :events events))))

(defn event-pub [m sel-ev transform]
  (event m :pub sel-ev transform))

(defn event-state [m sel-ev transform]
  (event m :state sel-ev transform))

(defn handle
  ([m msg handler]
     (let [handlers (:msg-handlers m)]
       (assoc m :msg-handlers (concat handlers [{:msg-type msg
                                                 :action handler}]))))
  ([m handler] (handle m nil handler)))

(defn state
  [m atom]
  (assoc m :!state atom))

(defn bind
  ([m query-fn handler]
     (let [bindings (:data-bindings m)]
       (assoc m :data-bindings (concat bindings [{:query-fn query-fn
                                                  :handler handler}]))))
  ([m handler]
     (bind m identity handler)))

(defn spy [m]
  (-> m
      (handle (fn [msg] (util/lpr msg)))
      #_(bind (fn [state] (util/lpr state)))))

(defn bus [m b]
  (assoc m :bus b))

(defn build [{:keys [html
                     init
                     events
                     msg-handlers
                     data-bindings
                     !state
                     initial-data
                     bus] :as opts}]
  ;; gen html
  (let [$root (init initial-data opts)
        opts (-> opts
                 (assoc :$el $root)
                 (dissoc :initial-data))
        bus (or bus (ps/mk-bus))]

    ;; add subs
    (doseq [{:keys [msg-type action]}
            (->> msg-handlers (filter identity))]
      (ps/sub bus msg-type
              (fn [msg]
                (if msg-type
                  (apply action (concat (clojure.core/drop 1 msg) [opts]))
                  (action msg opts)))))

    ;; bind events
    (doseq [{:keys [selector event transform type]} events]
      (doseq [$el (if selector
                    ($/query $root selector)
                    [$root])]
        (when (nil? $el)
          (throw (format "nsfw.widget/build: Element binding event [%s %s] to is nil."
                         selector event)))
        ($/listen $el event (fn [e]
                              (when-let [res (transform e $el opts)]
                                (condp = type
                                  :pub (ps/pub bus res)
                                  :state (swap! !state merge res)
                                  nil))))))

    (doseq [{:keys [query-fn handler]} data-bindings]
      ($/on-change
       !state
       (fn [id old new]
         (let [qold (query-fn old)
               qnew (query-fn new)]
           (when (not= qold qnew)
             (handler qnew qold opts)))))
      #_(handler (query-fn @!state) nil opts))
    (:$el opts)))

(defn node [m $el]
  (assoc m :init (fn [& _] $el)))