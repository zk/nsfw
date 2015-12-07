(ns nsfw.admin)

(defn admin [r]
  {:body "admin"})

(defn admin-login [r]
  {:body "admin login"})

(defn routes [{:keys [admin?]}]
  (merge
    {"" admin}
    {"/login" admin-login}))
