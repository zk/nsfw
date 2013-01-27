(ns nsfw.bind
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.util :as util]
            [cljs.reader :as reader]
            [goog.net.XhrIo]))

(defn ajax [opts]
  (let [{:keys [path method data headers success error]}
        (merge
         {:path "/"
          :method "GET"
          :data {}
          :headers {"content-type" "application/clojure"}
          :success (fn [])
          :error (fn [])}
         opts)]
    (goog.net.XhrIo/send
     path
     (fn [e]
       (try
         (let [req (.-target e)]
           (if (.isSuccess req)
             ;; maybe pull js->clj
             (success (let [resp (.getResponseText req)]
                        (when-not (empty? resp)
                          (reader/read-string resp))))
             (error req)))
         (catch js/Object e
           (.error js/console (.-stack e))
           (throw e))))
     method
     data
     (clj->js headers))))

(defn bind [atom f]
  (add-watch
   atom
   (gensym)
   (fn [key identity old-value new-value]
     (f identity old-value new-value))))

(defn change [atom f]
  (bind
   atom
   (fn [id old new]
     (when-not (= old new)
       (f id old new)))))

(defn push-updates [atom path & [opts]]
  (bind atom
        (fn [id old new]
          (ajax (merge
                 {:path path
                  :data new
                  :method "POST"}
                 opts)))))

(defn server [a path success error & [opts]]
  (let [last-response (atom @a)]
    (change
     a
     (fn [id old new]
       (when (not= @last-response new)
         (ajax (merge
                {:path path
                 :method "POST"
                 :data new
                 :success (fn [data]
                            (when-let [res (success data)]
                              (reset! last-response res)
                              (when (not= res new)
                                (reset! a res))))
                 :error (fn [e] (error old new e))}
                opts)))))))

#_(defn server [a path serialize]
  (let [last-response (atom (serialize @a))]
    (change
     a
     (fn [id old new]
       (let [serialized (serialize new)]
         (when (not= @last-response serialized)
           (ajax (merge
                  {:path path
                   :method "POST"
                   :data serialized
                   :success (fn [data]
                              (when data
                                (reset! last-response data)
                                (when (not= res serialized)
                                  (swap! a #(merge % data)))))
                   :error (fn [e] (error old new e))}
                  opts))))))))

(defn update [el atom f]
  (bind atom (fn [id old new] (f new old el)))
  el)

(defn append-or-text [el res]
  (cond
   (string? res) (dom/text el res)
   :else         (dom/append el res)))

(defn render [el atom f]
  (let [el (dom/wrap-content el)]
    (bind-change atom (fn [id old new]
                        (-> el
                            dom/empty
                            (append-or-text (f new old el)))))
    (append-or-text el (f @atom @atom el))
    el))

(defn text [el atom f]
  (update el atom (fn [new old el]
                    (-> el
                        dom/empty
                        (dom/text (f new old el))))))