(ns nsfw.gi
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! take! alts! timeout pipe mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn tunnel [{:keys [uri
                      include-response?
                      timeout]
               :or {timeout 5000}}
              payload]
  (when-not uri
    (throw (js/Error "tunnel: missing {:uri ...}")))
  (go
    (let [res (<! (http/post
                    uri
                    {:transit-params payload
                     :headers {"Content-Type" "application/transit+json"}
                     :timeout timeout}))
          {:keys [success
                  body
                  headers
                  error-code
                  error-text
                  status] :as resp}
          res]
      (cond

        (= :timeout error-code)
        {:tun/success? false
         :tun/timeout? true
         :tun/error error-text}

        (not= 200 status)
        {:tun/success? false
         :tun/error "Tunnel response status not 200"
         :tun/response res}

        (= -1
           (.indexOf
             (or (get headers "Content-Type")
                 (get headers "content-type"))
             "application/transit+json"))
        {:tun/success? false
         :tun/error (str "tunnel response content-type not application/transit+json: " (pr-str resp))}



        (not (map? body))
        {:tun/success? false
         :tun/error (str "tunnel response body is not a map: " (pr-str body))}

        :else (merge
                body
                {:tun/success? success}
                (when-not success
                  {:tun/error "Error contacting server"})
                (when include-response?
                  {:tun/response resp}))))))

(defn keys-with-ns [ns m]
  (->> m
       keys
       (filter #(= ns (namespace %)))))

(defn select-keys-with-ns [ns m]
  (when-not (or (map? m) (nil? m))
    (throw (js/Error. (str "Second arg to select-keys-with-ns should be a map: " m))))
  (->> m
       (filter #(= ns (namespace (first %))))
       (into {})))


(defn <mutate [tun-opts pl & [auth]]
  (go
    (let [{:keys [:gi/results] :as res}
          (<! (tunnel
                tun-opts
                [[:gi/mutate
                  (merge
                    pl
                    (when auth
                      {:gi/auth auth}))]]))]
      (merge
        (->> res
             (select-keys-with-ns "tun"))
        (first results)))))

(defn <mutate-one [tun-opts type obj & [auth]]
  (go
    (let [m (<! (<mutate tun-opts {type obj} auth))]
      (merge
        (get m type)
        (select-keys-with-ns "tun" m)
        (select-keys-with-ns "gi" m)))))

(defn <vmutate-one [tun-opts type obj & [auth]]
  (go
    (let [m (<! (<mutate-one tun-opts type obj auth))
          _ (prn "vmutone res" m)
          success? (if (vector? m)
                     (not (second m))
                     (and (or (:gi/success? m)
                              (:tun/success? m))
                          (:success? m)))
          _ (prn "success?" success?)]
      (if (vector? m)
        m
        (let [res (apply
                    dissoc
                    m
                    (concat
                      (keys-with-ns "tun" m)
                      (keys-with-ns "gi" m)))]
          (if success?
            [res nil]
            [nil (merge
                   (select-keys-with-ns "tun" m)
                   (select-keys-with-ns "gi" m))]))))))

(defn <query [tun-opts pl & [auth]]
  (when-not pl
    (throw (js/Error. "gi/query payload missing")))
  (go
    (let [res (<! (tunnel
                    tun-opts
                    [[:gi/query
                      (merge
                        pl
                        (when auth
                          {:gi/auth auth}))]]))
          m (-> res :gi/results first)]
      (merge
        m
        (select-keys-with-ns "tun" res)
        (select-keys-with-ns "gi" m)))))

(defn <query-obj [tun-opts type q & [auth]]
  (go
    (let [res (<! (<query
                    tun-opts
                    {:obj {:type type
                           :result :obj
                           :query q}}
                    auth))]
      res)))

(defn <vquery-obj [tun-opts type q & [auth]]
  (go
    (let [m (<! (<query-obj tun-opts type q auth))
          ;;_ (prn "vquery obj res" m)
          success? (if (vector? m)
                     (not (second m))
                     (and (or (:gi/success? m)
                              (:tun/success? m))
                          #_(:success? m)))
          _ (prn "success?" success?)]
      (if (vector? m)
        m
        (let [m (:obj m)
              res (apply
                    dissoc
                    m
                    (concat
                      (keys-with-ns "tun" m)
                      (keys-with-ns "gi" m)))]
          (if success?
            [res nil]
            [nil (merge
                   (select-keys-with-ns "tun" m)
                   (select-keys-with-ns "gi" m))]))))))

(defn <query-list [tun-opts type q & [auth]]
  (go
    (let [res (<! (<query
                    tun-opts
                    {:list {:type type
                            :result :list
                            :query q}}
                    auth))]
      res)))

(defn <vquery-list [tun-opts type obj & [auth]]
  (go
    (let [m (<! (<query-list tun-opts type obj auth))
          ;;_ (prn "vquery res" m)
          success? (if (vector? m)
                     (not (second m))
                     (and (or (:gi/success? m)
                              (:tun/success? m))
                          (:success? m)))
          _ (prn "success?" success?)]
      (if (vector? m)
        m
        (let [res (apply
                    dissoc
                    m
                    (concat
                      (keys-with-ns "tun" m)
                      (keys-with-ns "gi" m)))]
          (if success?
            [res nil]
            [nil (merge
                   (select-keys-with-ns "tun" m)
                   (select-keys-with-ns "gi" m))]))))))

(defn <query-count [tun-opts type q & [auth]]
  (go
    (let [res (<! (<query
                    tun-opts
                    {:count {:type type
                             :result :count
                             :query q}}
                    auth))]
      (:count res))))
