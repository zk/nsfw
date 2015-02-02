(ns nsfw.routing
  (:require [clout.core :refer (route-matches)]))

(defn routes-handler
  "route path params are overidden by query params in :params map"
  [rs]
  (fn [req]
    (let [matched-route (->> rs
                             (some (fn [{:keys [method path] :as route}]
                                     (when (or (= nil method)
                                               (= :any method)
                                               (= method (:request-method req)))
                                       (when-let [route-params (route-matches path req)]
                                         (assoc route :params route-params))))))]
      (when-let [{:keys [handler]} matched-route]
        (handler (assoc req :params (merge (:params matched-route)
                                           (:params req))))))))

(defn sugar->routes [sugar]
  (->> sugar
       (partition 3)
       (mapcat (fn [[method path handler-or-subroutes]]
                 (if (sequential? handler-or-subroutes)
                   (->> (sugar->routes handler-or-subroutes)
                        (filter
                          #(or (= :any method)
                               (= :any (:method %))
                               (= method (:method %))))
                        (map (fn [subroute]
                               (assoc subroute :path (str path (:path subroute))))))
                   [{:method method
                     :path path
                     :handler handler-or-subroutes}])))))



(comment (sugar->routes [:any "/foo" [:any "/bar" (fn [r])
                                      :get "/baz" (fn [r])]])

         (sugar->routes [:get "/foo" [:any "/bar" (fn [r])
                                      :get "/baz" (fn [r])]])

         (sugar->routes [:get "/foo" [:post "/bar" (fn [r])
                                      :get "/baz" (fn [r])]])

         (sugar->routes [:any "/foo/bar" (fn [r])])

         ((routes-handler
            [{:method :any
              :path "/foo/bar/*"
              :handler (fn [req] (println req) "OK")}])
          {:uri "/foo/bar/baz" :request-method :post}))

(defn router [rs]
  (->> rs
       sugar->routes
       routes-handler))

(defn mount [root-path sub-routes]
  [:any root-path sub-routes])
