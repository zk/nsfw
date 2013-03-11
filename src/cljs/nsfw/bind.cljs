(ns nsfw.bind
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.util :as util]
            [cljs.reader :as reader]
            [goog.net.XhrIo]))

(defn val-or-call [o & args]
  (if (fn? o)
    (apply o args)
    o))

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
     (f identity old-value new-value)))
  atom)

(defn change [atom f]
  (bind
   atom
   (fn [id old new]
     (when-not (= old new)
       (f id old new)))))

(defn server-push-when [!a opts should-push?]
  (bind
   !a
   (fn [id old new]
     (when (should-push? old new)
       (ajax (merge {:path "/"
                     :method "PATCH"
                     :success #()
                     :error #()
                     :data (pr-str new)}
                    opts))))))

(defn server-push [!a opts]
  (server-push-when !a opts (constantly true)))

(defn server-keys [!a opts & keys]
  (server-push-when
   !a opts
   (fn [old new]
     (not= (select-keys old keys)
           (select-keys new keys)))))

(defn server [!a opts & keys]
  ;; Track state of last server response to prevent shipping the same
  ;; thing twice
  (let [last-projection (atom (select-keys @!a keys))]
    (bind
     !a
     (fn [id old new]
       (when (and (not= (select-keys old keys)
                        (select-keys new keys))
                  (not= (select-keys new keys)
                        @last-projection))
         (ajax {:path (val-or-call (:path opts) new)
                :method (or (:method opts)
                            "POST")
                :data (select-keys new keys)
                :success
                (fn [data]
                  (let [server-projection (select-keys data keys)
                        local-projection (select-keys @!a keys)]
                    (reset! last-projection server-projection)
                    (when (and data
                               (not= server-projection local-projection))
                      (util/log (swap! !a merge server-projection)))))}))))))

(defn update [el atom f]
  (bind atom (fn [id old new] (f new old el)))
  el)

(defn append-or-text [el res]
  (cond
   (string? res) (dom/text el res)
   :else         (dom/append el res)))

(defn render [el atom f]
  (let [el (dom/wrap-content el)]
    (change atom
            (fn [id old new]
              (-> el
                  dom/empty
                  (append-or-text (f new old el)))))
    (append-or-text el (f @atom @atom el))
    el))

(defn render-struct [new old struct]
  (let [contents-or-fns (if (map? (second struct))
                          (drop 2 struct)
                          (drop 1 struct))
        contents (map (fn [content-or-fn]
                        (cond
                         (string? content-or-fn) content-or-fn
                         (coll? content-or-fn) (render-struct new old content-or-fn)
                         (ifn? content-or-fn) (content-or-fn new old)
                         :else content-or-fn))
                      contents-or-fns)
        opts (when (map? (second struct))
               (reduce (fn [m [k v]]
                         (assoc m k (cond
                                     (string? v) v
                                     (coll? v) v
                                     (ifn? v) (v new old)
                                     :else v)))
                       {}
                       (second struct)))]
    (dom/$ (vec (if opts
                  (concat [(first struct) opts] contents)
                  (concat [(first struct)] contents))))))

(defn render2 [!state struct & [after-update]]
  (let [!el (atom (render-struct @!state @!state struct))]
    (change !state (fn [id old new]
                     (let [new-el (render-struct new old struct)]
                       (dom/replace @!el new-el)
                       (reset! !el new-el))
                     (when after-update
                       (after-update new old @!el))))
    @!el))

(defn text [el atom f]
  (update el atom (fn [new old el]
                    (-> el
                        dom/empty
                        (dom/text (f new old el))))))

(defn map-difference [m1 m2]
  (loop [m (transient {})
         ks (concat (keys m1) (keys m2))]
    (if-let [k (first ks)]
      (let [e1 (find m1 k)
            e2 (find m2 k)]
        (cond (and e1 e2 (not= (e1 1) (e2 1))) (recur (assoc! m k (e1 1)) (next ks))
              (not e1) (recur (assoc! m k (e2 1)) (next ks))
              (not e2) (recur (assoc! m k (e1 1)) (next ks))
              :else    (recur m (next ks))))
      (persistent! m))))