(ns nsfw.ops
  "Provides message-based dispatching and context sharing. This helps
  with decoupling disparate parts of an app while sharing a common
  context (e.g. app state, windows, connections) between those parts."
  (:require [nsfw.util :as util]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put!
                     alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defprotocol Dispatcher
  (send [this op] [this op data])
  (bind! [this kw->f])
  (unbind! [this kws])
  (set-ctx! [this ctx])
  (stop [this])
  (set-debug! [this id f])
  (clear-debug! [this id]))

(defn bus [context handlers]
  (let [!handlers (atom handlers)
        !ctx (atom context)
        ch (chan)
        !debug-fns (atom {})
        bus (reify
              Dispatcher
              (send [_ op]
                (put! ch {::op op}))
              (send [_ op data]
                (put! ch {::op op ::data data}))
              (bind! [_ kw->f]
                (swap! !handlers merge kw->f))
              (unbind! [_ kws]
                (swap! !handlers
                  #(apply dissoc % kws)))
              (set-ctx! [_ ctx]
                (reset! !ctx ctx))
              (stop [_]
                (close! ch))
              (set-debug! [_ id f]
                (swap! !debug-fns assoc id f))
              (clear-debug! [_ id]
                (swap! !debug-fns dissoc id)))]
    (go-loop []
      (when-let [msg (<! ch)]
        (let [op (or (::op msg) (:op msg))]
          (if-let [f (get @!handlers op)]
            (do
              (println "[nsfw.ops] Dispatching" op)
              (f (merge {:bus bus}
                        @!ctx
                        (::data msg))))
            (println "[nsfw.ops] No handler for op" msg))
          (when-not (empty? @!debug-fns)
            (doseq [f (vals @!debug-fns)]
              (f op))))
        (recur))
      (println "[nsfw.ops] Stopping dispatcher"))
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
