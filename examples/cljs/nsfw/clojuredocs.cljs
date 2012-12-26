(ns nsfw.clojuredocs
  (:use [nsfw.util :only [log timeout interval]]
        [nsfw.dom :only [$]])
  (:require [cljs.reader :as reader]
            [nsfw.dom :as dom]
            [clojure.string :as str]))

(defn match-vars [vars text]
  (let [pattern (re-pattern text)]
    (filter #(re-find pattern (:name %))
            vars)))

(defn ellipsis [s n]
  (if (> (count s) n)
    (str (->> s (take n) (apply str) str/trim)
         "...")
    s))


(defn filter-input [atom vars]
  (-> ($ [:input.filter {:placeholder "filter here!"
                         :autofocus "autofocus"}])
      (dom/val-changed (fn [el val]
                         (when-not (empty? val)
                           (reset! atom (->> val
                                             (match-vars vars)
                                             (take 20))))))))

;; State
(def header
  ($ [:div.header
      [:h1 "ClojureDocs"]]))

(defn fmt-arglists [arglists]
  (->> arglists
       (map pr-str)
       (interpose " ")
       (apply str)))

(defn var-overview [{:keys [doc name added line file arglists ns]}]
  ($ [:div {:class (str "var-overview"
                        (when arglists " fn"))}
      [:div.code
       [:span.sym name]
       [:span.ns ns]]
      [:div.doc-preview (ellipsis doc 33)]
      (when arglists
        [:div.arglists (fmt-arglists arglists)])]))

(defn bind-render [el atom rendering-fn]
  (dom/bind atom
            (fn [ident old new]
              (-> el dom/empty
                  (dom/append (rendering-fn new)))))
  el)


(defn sidebar [vars-atom vars]
  ($ [:div.sidebar
      [:div.filter-wrapper
       (filter-input vars-atom vars)]
      (-> ($ [:div.var-overviews
              (map var-overview vars)])
          (bind-render vars-atom (fn [val] (map var-overview val))))]))

(defn filter-vars [vars-atom vars]
  ($ [:div.filter-wrapper
      (filter-input vars-atom vars)]))

(defn vars-overview [vars-atom vars]
  (-> ($ [:div.var-overviews
          (map var-overview vars)])
      (bind-render vars-atom (fn [val] (map var-overview val)))))

(defn sidebar [vars-atom vars]
  ($ [:div.sidebar
      (filter-vars vars-atom vars)
      (vars-overview vars-atom vars)]))

(defn render-var [{:keys [doc name arglists ns]}]
  ($ [:div.var
      [:h2.name name]
      [:div.doc doc]
      (when arglists
        [:div.arglists
         (->> arglists
              (map #(cons name %))
              (map #(vector :div (pr-str %)))
              (map $))])]))

(defn widget [state vis]
  (-> (vis @state)
      (bind-render state vis)))

(defn content [vars-atom]
  (widget vars-atom
          #($ [:div.content (map render-var %)])))

(defn main []
  (let [body ($ "body")
        vars (reader/read-string (.-functions js/window)) ; imported from page
        selected-vars (atom vars)] ; intially all vars are selected
    (-> body
        (dom/append header)
        (dom/append (sidebar selected-vars vars))
        (dom/append (content selected-vars)))))
