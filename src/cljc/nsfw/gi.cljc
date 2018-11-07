(ns nsfw.gi
  #?
  (:clj
   (:require
    [nsfw.util :as nu]
    [clj-http.client :as http]
    [clojure.core.async :as async
     :refer [go <! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
   :cljs
   (:require
    [nsfw.util :as nu]
    [cljs-http.client :as http]
    [cljs.core.async :as async
     :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]
     :refer-macros [go go-loop]])))

;; single endpoint with auth

(defn tunnel [{:keys [uri
                      include-response?
                      timeout
                      transit-opts
                      with-credentials?]
               :or {timeout 20000}
               :as req}
              payload]
  (when-not uri
    #? (:clj
        (throw (Exception. "tunnel: missing {:uri ...}"))
        :cljs
        (throw (js/Error "tunnel: missing {:uri ...}"))))
  (go
    (let [res #?(:clj
                 (http/post
                   uri
                   {:content-type :transit+json
                    :form-params (nu/to-transit payload transit-opts)})
                 :cljs
                 (<! (http/post
                       uri
                       {:transit-params payload
                        :headers {"Content-Type" "application/transit+json"}
                        :timeout timeout
                        :transit-opts transit-opts
                        :with-credentials? with-credentials?})))
          {:keys [success
                  body
                  headers
                  error-code
                  error-text
                  status] :as resp}
          res]
      (cond
        (= :timeout error-code)
        [nil
         {:timeout? true
          :error error-text}
         req]

        (not= 200 status)
        [nil
         {:error "Tunnel response status not 200"
          :response res}
         req]

        (= -1
           (.indexOf
             (or (get headers "Content-Type")
                 (get headers "content-type"))
             "application/transit+json"))
        [nil
         {:error (str "tunnel response content-type not application/transit+json: " (pr-str resp))}
         req]

        #? (:cljs (not success))
        #? (:cljs [nil
                   {:error "Couldn't contact server"}
                   req])

        :else [body nil req]))))

(defn <send-commands [tun-opts commands]
  (go
    (<! (tunnel tun-opts commands))))

(defn <send-command [tun-opts command]
  (go
    (let [response (<! (tunnel tun-opts [command]))]
      (if (second response)
        response
        (ffirst response)))))

(def <scom <send-command)

(defn handle-commands [spec {:keys [body]}]
  (let [commands body]
    (->> commands
         (map (fn [command]
                (let [handler (get spec (first command))]
                  (if handler
                    (try
                      (apply handler (rest command))
                      #? (:clj
                          (catch clojure.lang.ArityException e
                            (println "GI2 Exception Caught")
                            (prn e)
                            [nil {:error (str
                                           "Wrong number of arguments for "
                                           (first command))}]))
                      #? (:clj
                          (catch Exception e
                            (println "GI2 Exception Caught")
                            (prn e)
                            [nil {:error (str
                                           "Exception when handling "
                                           (first command))
                                  :exception (pr-str e)}])
                          :cljs
                          (catch js/Error e
                            [nil {:error (str
                                           "Exception when handling "
                                           (first command))
                                  :exception (pr-str e)}])))
                    [nil {:error (str "No handler found for " (first command))}])))))))

(defn format-response [request results]
  (let [body (->> results
                  (map (fn [res]
                         (if (map? res)
                           (:body res)
                           res)))
                  vec)
        session (->> results
                     (map (fn [res]
                            (when (map? res)
                              (:session res))))
                     (remove nil?)
                     (reduce merge))]
    (merge
      {:body body}
      (when-not (empty? session)

        {:session
         (merge
           (:session request)
           session)}))))

(defn handle-request [spec {:keys [body] :as request}]
  (format-response
   request
   (handle-commands spec request)))

#_(go
    (nu/pp (<! (<send-commands
                 {:uri "http://localhost:5000/api/fash/gi2"}
                 [[:test "foo"]]))))

#_(go
    (nu/spy "GI2 command"
      (<! (<send-command
            {:uri "http://localhost:5000/api/fash/gi2"}
            [:test2 "foo" "bar"]))))
