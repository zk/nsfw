(ns nsfw.user-prefs)

(def *user-prefs* {})

(defn current [] *user-prefs*)

(defn wrap-bind [handler defaults]
  (fn [req]
    (binding [*user-prefs* (merge defaults (:user-prefs (:session req)))]
      (handler req))))

(defn update [resp req new-prefs]
  (let [sess (get req :session {})
        old-prefs (current)]
    (assoc resp
      :session
      (assoc sess :user-prefs (merge old-prefs new-prefs)))))

