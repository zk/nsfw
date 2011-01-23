(ns nsfw.csrf
  (:use [nsfw.util]))


;; Used in the middleware to bind the current csrf token for
;; use through (current) by actions and templates.
(def *csrf-token* nil)

(defn gen-token []
  (sha1-str (str (java.util.UUID/randomUUID))))

(defn insert-token [resp]
  (let [ses (get resp :session {})]
    (assoc resp
      :session (assoc ses
                 :csrf-token (gen-token)))))

(defn pull [req]
  (-> req
      (:session)
      (:csrf-token)))

(defn current []
  *csrf-token*)

(defn wrap-bind-csrf [handler]
  (fn [req]
    (binding [*csrf-token* (pull req)]
      (handler req))))

