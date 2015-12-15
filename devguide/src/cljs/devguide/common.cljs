(ns devguide.common)

(defn fn-doc [{:keys [name ns]} body]
  [:div.function-doc
   [:div.header.flex-row
    [:h3 [:code name]]
    [:div [:code ns]]]
   [:div.content
    body]])

(defn $options [opts]
  [:div.sec4
   [:h5 "Options"]
   [:div.options-table
    (->> opts
         (map (fn [[k v]]
                ^{:key k}
                [:div.row
                 [:div.col-xs-4.key.col-md-3
                  (str k)]
                 [:div.col-xs-8.val.col-md-9
                  v]])))]])
