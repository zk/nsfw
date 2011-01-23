(ns nsfw.forms
  (:use [hiccup core [page-helpers :only (doctype)]]
        [nsfw util csrf]))

(defn parse-opts-body [[o & b]]
  [(if (map? o) o nil)
   (if (map? o) b (concat [o] b))])

(defn form [& args]
  (let [[opts body] (parse-opts-body args)]
    (html [:form
           opts
           body])))

(defn group [& [o & b]]
  (let [opts (if (map? o) o nil)
        body (if opts b (concat [o] b))]
    [:div (merge {:class "form-section"} (dissoc opts :title))
     (when (:title opts) [:h6 {:class "section-title"} (:title opts)])
     body
     [:div {:class "clear"}]]))

(defn input-row [type & opts]
  (let [opts (apply hash-map opts)
        input-opts (dissoc opts :name :label)
        type (name type)]
    [:div {:class "row"}
     [:label {:for (:name opts)} (:label opts)]
     [:div {:class "input"}
      [:input (merge {:type type :name (:name opts) :class (str type "-input")}
                     input-opts)]]
     [:div {:class "clear"}]]))

(defn text-row [& opts]
  (apply input-row :text opts))

(defn password-row [& opts]
  (apply input-row :password opts))

(defn submit-row [& opts]
  (let [opts (apply hash-map opts)]
    [:div {:class "row submit"}
     [:div {:class "input"}
      [:input (merge opts {:type "submit" :value (:label opts)})]]]))
