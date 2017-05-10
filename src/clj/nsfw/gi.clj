(ns nsfw.gi
  (:require [nsfw.http :as http]
            [nsfw.gqi :as gqi]))

(defn handle-gi-command [[command-key
                          command-payload
                          :as command]
                         mutate-interface
                         query-interface]
  (merge
    (cond
      (not command-key)
      {:gi/success? false
       :gi/error (str
                   "Command key missing: "
                   (pr-str command))}

      (not command-payload)
      {:gi/success? false
       :gi/error (str
                   "Missing payload for command: "
                   command-key)}

      (and (= :gi/mutate command-key)
           (not (map? command-payload)))
      {:gi/success? false
       :gi/error (str "Mutate payload should be a map, got: "
                      command-payload)}

      (= :gi/mutate command-key)
      (let [id (:gi/id command-payload)
            auth (:gi/auth command-payload)]
        (merge
          (->> command-payload
               (remove (fn [[k v]]
                         (= "gi" (namespace k))))
               (map (fn [[k v]]
                      [k (mutate-interface
                           {:type k
                            :obj v}
                           auth)]))
               (into {}))
          (when id
            {:gi/id id})))

      (and (= :gi/query command-key)
           (:type command-payload)
           (:result-type command-payload)
           (:query command-payload))
      {:gi/success? false
       :gi/error (str "Queries are keyed at the top level, don't send a qi query without wrapping in a map: " command-payload)}

      (= :gi/query command-key)
      (merge
        (let [auth (:gi/auth command-payload)
              res (-> command-payload
                      ((fn [m]
                         (->> m
                              (remove #(= "gi" (namespace (first %))))
                              (into {}))))
                      (query-interface auth))]
          (if (:success? res)
            (:result res)
            {:gi/success? false
             :gi/error (str "Error querying: " (:errors res))}))
        (when-let [id (:gi/id command-payload)]
          {:gi/id id}))

      :else {:gi/success? false
             :gi/error (str "Unknown command key: " (pr-str command-key))})))

(defn gi [mutate-interface
          query-interface
          commands
          auth]
  {:body {:gi/results
          (->> commands
               (map #(handle-gi-command %
                       mutate-interface
                       query-interface)))}})

(defn handler [specs]
  (let [mutate (gqi/mk-mutate-engine (:mutate specs))
        query (gqi/mk-query-engine (:query specs))]
    (-> (fn [{:keys [headers body :gi/auth] :as r}]
          (cond
            (not
              (or (.contains
                    (get headers "Content-Type")
                    "application/transit+json")
                  (.contains
                    (get headers "content-type")
                    "application/transit+json")))
            {:tun/error (str "Received content-type not application/transit+json: " (pr-str (or (get headers "Content-Type")
                                                                                                (get headers "content-type"))))}
            :else (gi
                    mutate
                    query
                    body
                    auth)))
        http/wrap-transit-request
        http/wrap-transit-response)))
