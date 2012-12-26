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
                         (reset! atom
                                 (->> (if (empty? val)
                                        vars
                                        (match-vars vars val))
                                      (take 20)))))))

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

(defn bind-update [el atom f]
  (dom/bind atom (fn [id old new] (f el new)))
  el)

(defn filter-vars [vars-atom vars]
  ($ [:div.filter-wrapper
      (filter-input vars-atom vars)]))

(defn vars-overview [vars-atom vars]
  (-> ($ [:div.var-overviews
          (map var-overview vars)])
      (bind-update vars-atom (fn [el new]
                               (-> el
                                   dom/empty
                                   (dom/append (map var-overview new)))))))

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

(defn content [vars-atom]
  (-> ($ [:div.content (map render-var @vars-atom)])
      (bind-update vars-atom
                   (fn [el new]
                     (-> el
                         dom/empty
                         (dom/append (map render-var new)))))))

(defn results-count [vars-atom]
  (-> ($ [:span.results-count (count @vars-atom)])
      (bind-update vars-atom (fn [el new]
                               (-> el dom/empty
                                   (dom/text (count new)))))
      (dom/style {:position :absolute
                  :top :0px
                  :right :0px
                  :padding :10px})))

(defn main []
  (let [body ($ "body")
        vars (reader/read-string (.-functions js/window)) ; imported from page
        selected-vars (atom (take 20 vars))] ; intially all vars are selected
    (-> body
        (dom/append ($ [:div.header
                        [:h1 "ClojureDocs"]
                        (results-count selected-vars)]))
        (dom/append ($ [:div.sidebar
                        (filter-vars selected-vars vars)
                        (vars-overview selected-vars vars)]))
        (dom/append (content selected-vars)))))
