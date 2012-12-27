(ns nsfw.bind
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]))

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
                          (reader/read-string ))))
             (error req)))
         (catch js/Object e
           (.error js/console (.-stack e))
           (throw e))))
     method
     data
     (clj->js headers))))

(defn push-updates [atom path & [opts]]
  (bind atom
        (fn [id old new]
          (ajax (merge
                 {:path path
                  :data new
                  :method "POST"}
                 opts)))))

(defn bind [atom function]
  (add-watch
   atom
   (gensym)
   (fn [key identity old-value new-value]
     (function identity old-value new-value))))

(defn change [atom key f]
  (add-watch
   atom
   (gensym)
   (fn [k atom old new]
     (let [ov (get old key)
           nv (get new key)]
       (when (not= ov nv)
         (f atom old new))))))

(defn update [el atom f]
  (bind atom (fn [id old new] (f el new)))
  el)

(defn render [el atom f]
  (bind atom (fn [id old new]
               (-> el
                   dom/empty
                   (dom/append (f new)))))
  (dom/append el (f @atom))
  el)
