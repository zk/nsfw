(ns nsfw.render
  (:require [org.danlarkin.json :as json]
            [ring.util.response :as resp]))

(defn render-dispatch [& args]
  (if (keyword? (first args))
    (first args)
    :template))

(defmulti render render-dispatch)

(defmethod render :template [template & args]
           {:status 200
            :headers {"Content-Type" "text/html;charset=utf-8"}
            :body (apply template args)})

(defmethod render :text [_ thing & args]
           (merge {:status 200
                   :headers {"Content-Type" "text/html;charset=utf-8"}
                   :body (str thing)}
                  (apply hash-map args)))

(defmethod render :html [_ text & args]
           (merge {:status 200
                   :headers {"Content-Type" "text/html;charset=utf-8"}
                   :body text}
                  (apply hash-map args)))

(defmethod render :json [_ map & args]
           (merge {:status 200
                   :headers {"Content-Type" "application/json; charset=utf-8"}
                   :body (json/encode map)}
                  (apply hash-map args)))

(defmethod render :resource [_ resource-name & args]
           (merge (resp/resource-response (str "public/" resource-name))
                  (apply hash-map args)))

(defn json-header [resp]
  (resp/header resp "Content-Type" "application/json;charset=utf-8"))

