(ns nsfw.console
  (:require [clojure.string :as str]
            [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]))

(defn name-js->clj [name]
  (-> name
      (str/replace #"_GT_" ">")
      (str/replace #"_BANG_" "!")
      (str/replace #"_" "-")))

(defn name-clj->js [name]
  (-> name
      (str/replace #">" "_GT_")
      (str/replace #"!" "_BANG_")
      (str/replace #"-" "_")))

(defn starts-with? [s match]
  (= 0 (.indexOf s match)))

(defn render-type [data]
  [:span.data-type
   (cond
    (fn? data) "f"
    (map? data) "m"
    (starts-with? (pr-str data) "#<Atom:") "a"
    (or (starts-with? (pr-str data) "#<[object HTML")
        (starts-with? (pr-str data) "#<[object NodeList]>")) "d"
    :else "?")])

(defn render-attr [i o k]
  [:tr {:class (if (= 0 (mod i 2)) "even" "odd")}
   [:td.data-type (render-type (aget o k))]
   [:td (name-js->clj k)]
   [:td (take 100 (pr-str (aget o k)))]])

(defn render-attrs [o]
  (let [keys (Object/keys o)]
    (map-indexed #(render-attr %1 o %2) keys)))

(defn js-val [var]
  (reduce (fn [o p] (aget o p)) js/window (str/split var #"\.")))

(defn render-ns [name]
  [:table
   (->> (js-val (name-clj->js name))
        render-attrs)])

(declare render-result)

(defn render-coll [data]
  [:ul (map #(vector :li (render-result %)) data)])

(defn render-result [data]
  (cond
   (fn? data) [:div [:h5 "fn"] (pr-str data)]
   (coll? data) (render-coll data)
   (= (type data) js/NodeList) [:div data]
   :else [:div (pr-str data)]))

(defn on-debug-open [e $el $res !vis]
  (if @!vis
    (do
      (reset! !vis false)
      (dom/style $el {:display "none"}))
    (do
      (reset! !vis true)
      (dom/style $el {:display "block"}))))

(defn panel [ns]
  (let [ns-parts (str/split ns #"\.")
        $res (dom/$ [:div.results
                     [:div
                      [:h4 ns]
                      (render-ns ns)]])
        $input (dom/$ [:input.search {:autofocus "autofocus"}])
        $el (dom/$ [:div.debug
                    $input
                    [:div.complete]
                    $res])
        doc js/document
        !vis (atom false)]
    (dom/keydown doc (fn [{:keys [key-code meta-key ctrl-key] :as e}]
                       (when (and (= 32 key-code)
                                  meta-key)
                         (on-debug-open e $el $res !vis))
                       (when (= 27 key-code)
                         (on-debug-open e $el $res !vis))
                       (when (and (= 71 key-code)
                                  ctrl-key)
                         (dom/val $input "")
                         (-> $res
                             dom/empty
                             (dom/append [:div
                                          [:h4 ns]
                                          (render-ns ns)])))))
    (dom/on-enter $input (fn [e $el]
                           (let [val (dom/val $input)
                                 name (->> (concat ns-parts
                                                   [val])
                                           (map name-clj->js)
                                           (remove #(empty? %))
                                           (interpose ".")
                                           (apply str))
                                 data (js-val (name-clj->js name))]
                             (if (empty? val)
                               (-> $res
                                   dom/empty
                                   (dom/append [:div
                                                [:h4 ns]
                                                (render-ns ns)]))
                               (-> $res
                                   dom/empty
                                   (dom/append [:div [:h4 (->> ns-parts
                                                               (interpose ".")
                                                               (apply str))]
                                                (render-result data)]))))))
    $el))

(defn on-cmd-space [el f]
  (dom/keydown
   js/document
   (fn [{:keys [key-code meta-key ctrl-key] :as e}]
     (when (and (= 32 key-code)
                meta-key)
       (f e)))))

(defn panel []
  (let [el (dom/$ [:div.console-panel "COMMAND PANEL"])
        toggle (util/toggle
                #(dom/style el {:display "block"})
                #(dom/style el {:display "none"}))]
    (on-cmd-space
     el
     toggle)
    el))
