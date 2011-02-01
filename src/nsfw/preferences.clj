(ns nsfw.user-prefs)

(def *user-prefs* {})

(defn current [] *user-prefs*)

(defn wrap-bind-user-prefs [handler defaults]
  (fn [req]
    (binding [*user-prefs* (merge defaults (:user-prefs (:session req)))]
      (handler req))))