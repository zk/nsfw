(ns nsfw.ops
  "Provides message-based dispatching and context sharing. This helps
  with decoupling disparate parts of an app while sharing a common
  context (e.g. app state, windows, connections) between those parts."
  (:require [nsfw.util :as util]))

(defprotocol Dispatcher
  (send [this op] [this op data])
  (bind! [this kw->f])
  (unbind! [this kws])
  (set-ctx! [this ctx])
  (set-debug! [this id f])
  (clear-debug! [this id]))

(defn bus [context handlers]
  (let [!handlers (atom handlers)
        !ctx (atom context)
        !debug-fns (atom {})
        bus (reify
              Dispatcher
              (send [this op]
                (send this op nil))
              (send [_ op data]
                (when-let [msg {::op op ::data data}]
                  (let [op (or (::op msg) (:op msg))]
                    (if-let [f (get @!handlers op)]
                      (do
                        #_(println "[nsfw.ops] Dispatching" op)
                        (f (merge {:bus bus}
                                  @!ctx
                                  (::data msg))))
                      (println "[nsfw.ops] No handler for op" msg))
                    (when-not (empty? @!debug-fns)
                      (doseq [f (vals @!debug-fns)]
                        (f op))))))
              (bind! [_ kw->f]
                (swap! !handlers merge kw->f))
              (unbind! [_ kws]
                (swap! !handlers
                  #(apply dissoc % kws)))
              (set-ctx! [_ ctx]
                (reset! !ctx ctx))
              (set-debug! [_ id f]
                (swap! !debug-fns assoc id f))
              (clear-debug! [_ id]
                (swap! !debug-fns dissoc id)))]
    bus))

(defn data [op]
  (::data op))

(defn op [{:keys [op op-id data on-ack on-error auth]}]
  {::op op
   ::data data
   ::op-id (or op-id (util/uuid))
   ::on-ack on-ack
   ::auth auth
   ::on-error on-error})
