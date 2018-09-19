(ns nsfw.devbus.gui
  (:require [nsfw.fuse :as fuse]
            [nsfw.util :as nu]
            [nsfw.devbus :as devbus]
            [nsfw.components :as nc]
            [nsfw.page :as page]
            [nsfw.s3 :as s3]
            [dommy.core :as dommy]
            [cljs.reader :as reader]
            [taoensso.timbre :as log :include-macros true]
            [reagent.core :as r]
            [datascript.transit :as dt]
            [mount.core
             :as mount
             :refer-macros [defstate]]
            [cljs.core.async
             :refer [put! chan]
             :refer-macros [go]]))


(def ENV (when js/ENV (js->clj js/ENV :keywordize-keys true)))

(def AWS_CREDS
  {:access-id (:aws-access-id ENV)
   :secret-key (:aws-secret-key ENV)
   :region "us-west-2"})



(def DEBUG_APP_STATES
  (->> ["app-state-zk-1537217263557.transit.json"
        "app-state-zk-1537004440662.transit.json"
        "app-state-zk-1536536501989.transit.json"]
       (map (fn [key-frag]
              {:s3-key (str "debug-app-state/" key-frag)
               :s3-bucket "nalopastures"
               :s3-region "us-west-2"}))))

(def !state (r/atom nil))
(defonce !db (atom nil))

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

(defn $devbus []
  (r/create-class
    {:component-did-mount
     (fn []
       (let [db (devbus/debug-client
                  "ws://localhost:44100/devbus"
                  {:on-state-items
                   (fn [state-items]
                     (swap! !state
                       update
                       :state-items
                       #(distinct-by
                          :key
                          (concat state-items %))))
                   :on-test-states
                   (fn [test-states]
                     (swap! !state
                       assoc
                       :test-states
                       test-states))
                   :on-error (fn [e]
                               (prn "ERR" e))})]

         (reset! !db db)))
     :component-will-unmount
     (fn []
       (devbus/stop-client @!db))
     :reagent-render
     (fn [] [:span])}))


(defn $exact-timeago [{:keys [ts]}]
  (let [!ts (atom ts)
        !v (r/atom (nu/exact-timeago ts))]

    (r/create-class
      {:component-did-update
       (page/cdu-diff
         (fn [_ [{:keys [ts]}]]
           (reset! !ts ts)))

       :reagent-render
       (fn []
         [:span
          [nc/$poller
           {:active? true
            :on-poll (fn []
                       (reset! !v (nu/exact-timeago @!ts)))}]
          @!v])})))

(defn $editable [_ text]
  (let [!ext (r/atom text)
        !int (r/atom text)]
    (r/create-class
      {:component-did-update
       (page/cdu-diff
         (fn [[_ old-text] [_ new-text]]
           (when (not= old-text new-text)
             (reset! !ext new-text))))
       :reagent-render
       (fn []
         [:div
          [:div.flex-right
           [:a
            {:href "#"
             :disabled (when (= @!ext @!int) "disabled")
             :on-click (fn [e]
                         (.preventDefault e)
                         nil)}
            "update"]]
          [:textarea
           {:style {:width "100%"
                    :height 500
                    :padding 10}
            :on-change (fn [e]
                         (reset! !int (.. e -target -value)))
            :value (or @!int @!ext)}]])})))

(defn filter-level [n value]
  #_(if (<= 0 n)
      value
      (cond
        (map? value) (filter-map (dec n) value))))

(defn $searchable [_ _]
  (let [!ui (r/atom nil)]
    (fn [_ value]
      [:div
       (->> [0 1 2 3]
            (map (fn [level]
                   [:a {:href "#"
                        :style {:margin-right 5}
                        :on-click (fn [e]
                                    (.preventDefault e)
                                    (swap! !ui assoc :level level)
                                    nil)}
                    (pr-str level)])))
       [:pre (nu/pp-str (filter-level (:level @!ui) value))]])))

(defn parse-filter-text [v]
  (when (and v (not (empty? v)))
    (try
      (reader/read-string
        (str "[" v "]"))
      (catch js/Error e
        (log/debug "Couldn't parse:" v)
        nil))))

(defn gen-filter-function [form]
  (if (and form (sequential? form))
    (condp = (first form)
      'rmk (fn [v]
             (->> form
                  rest
                  (reduce (fn [v form-el]
                            (if (sequential? form-el)
                              (update-in
                                v
                                (butlast form-el)
                                (fn [m]
                                  (dissoc m (last form-el))))
                              (dissoc v form-el)))
                    v)))
      identity)
    identity))

(defn $pp-pre [value]
  [:pre
   {:style {:color "#ddd"}}
   (nu/pp-str value)])

(defn apply-filter [value filter-text]
  (if filter-text
    (let [filter-fn (gen-filter-function (parse-filter-text filter-text))]
      (filter-fn value))
    value))

(defn $clj-data-view [_ _]
  (let [!ui (r/atom nil)]
    (fn [opts
         clj-obj]
      (let [{:keys [filter-text]} @!ui]
        [:div
         {:key key
          :id key}
         [:div
          {:style {:display 'flex
                   :align-items 'left
                   :justify-content 'space-between
                   :background-color "#555"
                   :margin-bottom 10
                   :padding 10}}
          [:div
           {:style {:flex 1}}
           [:input {:style {:width "100%"}
                    :type "text"
                    :value filter-text
                    :on-change
                    (fn [e]
                      (swap! !ui assoc
                        :filter-text
                        (.. e -target -value)))}]]]
         [$pp-pre (apply-filter clj-obj filter-text)]
         #_[$searchable
            {}
            value]]))))

(defn $state-item [_ _ ]
  (let [!ui (r/atom nil)]
    (fn [opts
         {:keys [key updated-at value]}]
      (let [{:keys [filter-text]} @!ui]
        [:div
         {:key key
          :id key}
         [:div
          {:style {:display 'flex
                   :align-items 'center
                   :justify-content 'space-between
                   :background-color "#555"
                   :margin-bottom 10
                   :padding 10}}
          [:div {:style {:display 'flex
                         :align-items 'center
                         :justify-content 'flex-start}}
           [:h3 {:style {:margin 0}}
            key]
           [:div {:style {:width 10}}]
           [:div
            [$exact-timeago {:ts updated-at}]]]
          [:div
           [:input {:type "text"
                    :value filter-text
                    :on-change
                    (fn [e]
                      (swap! !ui assoc
                        :filter-text
                        (.. e -target -value)))}]]]
         [$pp-pre (apply-filter value filter-text)]
         #_[$searchable
            {}
            value]]))))

(defn $nav-header [& title-parts]
  [:div {:style {:padding 10
                 :background-color "black"}}

   [:h1 {:style {:font-size 16
                 :font-weight 'normal
                 :margin 0}}
    (apply str title-parts)]])


(defn load-debug-app-state [{:keys [s3-key s3-bucket]}]
  (go
    (let [[res err] (<! (s3/<call AWS_CREDS
                          "getObject"
                          {:Bucket s3-bucket
                           :Key s3-key}))]
      (when res
        (let [state (-> res
                        :Body
                        str)]
          (devbus/send
            @!db
            [:load-test-state
             {:section "Debug App State"
              :title s3-key
              :state (nu/from-transit state)}]))))))

(defn $collapable [_ & _]
  (let [!open? (r/atom nil)]
    (fn [{:keys [header]} & children]
      [:div.collapsable
       [:div.collapsable-header
        {:style {:cursor 'pointer}
         :on-click (fn [e]
                     (.preventDefault e)
                     (swap! !open? not)
                     nil)}
        header]
       (when @!open?
         (vec
           (concat
             [:div.collapsable-body]
             children)))])))

(defn $root []
  [:div.mag-left
   [$devbus]
   [:div.mag-sidebar
    {:style {:position 'fixed
             :top 0
             :left 0
             :bottom 0
             :width 200
             :background-color "#333"
             :color 'white
             :overflow-y 'scroll}}


    [page/$interpose-children
     {:separator [:div {:style {:height 1}}]}
     [$collapable
      {:header
       [$nav-header "Mem" " (" (count (:state-items @!state)) ")"]}
      [:div.pad-sm
       (->> @!state
            :state-items
            (sort-by :key)
            (map (fn [{:keys [key value]}]
                   [:a {:key key
                        :href (str "#" key)
                        :style {:padding 10
                                :display 'block
                                :color 'white}}
                    [:h3
                     {:style {:margin 0
                              :font-size 16}}
                     key]])))]]


     (let [sections (->> @!state
                         :test-states
                         (group-by :section))]
       [$collapable
        {:header
         [$nav-header "Test States" " (" (count sections) ")"]}
        [:div
         (->> sections
              (map (fn [[section-title test-states]]
                     [:div
                      {:key section-title}
                      [$collapable
                       {:header [:div
                                 {:style {:padding 10}}
                                 [:h4 {:style {:font-size 12
                                               :font-weight "500"
                                               :text-transform 'uppercase
                                               :letter-spacing 1
                                               :margin 0
                                               :color "#ccc"}}
                                  section-title]]}
                       [:div
                        (->> test-states
                             (map (fn [{:keys [title] :as test-state}]
                                    [:a {:key (str section-title "-" title)
                                         :href "#"
                                         :style {:padding 10
                                                 :display 'block
                                                 :padding-left 25
                                                 :color 'white}
                                         :on-click (fn [e]
                                                     (.preventDefault e)
                                                     (swap! !state
                                                       assoc
                                                       :current-test-state
                                                       test-state)
                                                     (devbus/send
                                                       @!db
                                                       [:load-test-state test-state])
                                                     nil)}
                                     [:h3
                                      {:style {:margin 0
                                               :font-size 16}}
                                      title]])))]]])))]])

     [$collapable
      {:header [$nav-header (str "Debug App States" " (" (count DEBUG_APP_STATES) ")")]}
      [:div.pad-sm
       (->> DEBUG_APP_STATES
            (map (fn [{:keys [s3-key] :as das}]
                   [:a {:key s3-key
                        :href "#"
                        :style {:padding 10
                                :display 'block
                                :color 'white}
                        :on-click (fn []
                                    (load-debug-app-state das))}
                    [:h3
                     {:style {:margin 0
                              :font-size 16}}
                     s3-key]])))]]]]
   [:div.mag-content
    {:style {:position 'fixed
             :left 200
             :top 0
             :right 0
             :bottom 0
             :overflow-y 'scroll
             :background-color "#111"
             :color 'white}}
    [:div.container
     [:div.row
      [:div.col-sm-12
       [:div
        [:br]
        (when-let [current-test-state (:current-test-state @!state)]
          [$clj-data-view {} current-test-state])
        (->> @!state
             :state-items
             (filter #(= (:sel-state-item-key @!state)
                         (:key %)))
             (map (fn [{:keys [key value updated-at] :as si}]
                    [$state-item {:key key} si]))
             doall)]]]]]])

(defn init []
  (let [on-hashchange (fn []
                        (swap! !state
                          assoc
                          :sel-state-item-key
                          (page/location-hash)))]
    (dommy/listen!
      js/window
      :hashchange
      on-hashchange)

    (r/render
      [$root]
      (.getElementById js/document "entry"))

    (fn []
      (dommy/unlisten!
        js/window
        :hashchange
        on-hashchange))))

(def reload-hook (fn []))

(defn shutdown [f]
  (when @f (@f)))

(defstate app
  :start (init)
  :stop (shutdown app))

(mount/start)
