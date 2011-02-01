(ns nsfw.csrf
  "Middleware and utilities to prevent cross-site request forgery attacks.

  Usage:

  Add the wrap-bind-csrf middleware to your request chain.  `wrap-bind-csrf`
  will bind *csrf-token* to a new token, and then add that token to `{:session {:csrf-token}}`
  on the next response.

  From that point forward the csrf-token can be accessed through `current`."
  (:use [nsfw.util]))

;; Used in the middleware to bind the current csrf token for
;; use through (current) by actions and templates.
(def *csrf-token* nil)

(defn gen-token []
  (sha1-str (str (java.util.UUID/randomUUID))))

(defn insert-token [resp token]
  (let [ses (get resp :session {})]
    (assoc resp
      :session (assoc ses
                 :csrf-token token))))

(defn pull [req]
  (-> req
      (:session)
      (:csrf-token)))

(defn current []
  *csrf-token*)

(defn wrap-bind-csrf [handler]
  (fn [req]
    (if-let [token (current)]
      (binding [*csrf-token* token]
        (handler req))
      (binding [*csrf-token* (gen-token)]
        (insert-token (handler req) *csrf-token*)))))
