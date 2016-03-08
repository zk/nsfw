(ns nsfw.gqi
  (:require [nsfw.util :as util]
            [somnium.congomongo :as mon]))

(mon/mongo! :db "dailyink")

(defn resolve-handler [specs type result-type]
  (let [spec (get specs type)
        spec (if (map? spec)
               spec
               {:list spec})
        flist (:list spec)
        fobj (or (:obj spec)
                 (comp first flist))
        fcount (or (:count spec)
                   (comp count flist))]
    (condp = result-type
      :list flist
      :obj fobj
      :count fcount
      nil)))


(defn replace-with-value [sym context]
  (let [ns (.getNamespace sym)
        name (.getName sym)]
    (if (= ns "$")
      (get context (keyword name))
      sym)))

#_(defn populate-query [query context]
    (let [m (->> query
                 (partition 2)
                 (map vec)
                 (into {}))
          where (:where m)
          where' (->> where
                      (map (fn [[k v]]
                             (if (symbol? v)
                               [k (replace-with-value v context)]
                               [k v])))
                      (into {}))]
      (->> (assoc m :where where')
           (mapcat identity)
           vec)))

;; FIX: This assumes a [:where {...}] right now, handle var
;; replacement for all data types
(defn populate-query [query context]
  (if (sequential? query)
    (let [m (->> query
                 (partition 2)
                 (map vec)
                 (into {}))
          where (:where m)
          where' (->> where
                      (map (fn [[k v]]
                             (if (symbol? v)
                               [k (replace-with-value v context)]
                               [k v])))
                      (into {}))]
      (->> (assoc m :where where')
           (mapcat identity)
           vec))
    query))

(declare do-query)

(defn apply-subs [spec subs auth query-result]
  (cond
    (map? query-result)
    (merge
      query-result
      (->> subs
           (map (fn [[k v]]
                  [k (:result (do-query spec v auth query-result))]))
           (into {})))

    (coll? query-result)
    (->> query-result
         (map (fn [context]
                (merge
                  context
                  (->> subs
                       (map (fn [[k v]]
                              [k (:result (do-query spec v auth context))]))
                       (into {}))))))
    :else query-result))

(defn do-query [spec {:keys [type result query subs]} auth context]
  (let [handler (resolve-handler spec type result)]
    (if handler
      (let [query (populate-query query context)
            query-result (handler query auth)
            query-result (apply-subs spec subs auth query-result)]
        {:success? true :result query-result})
      {:success? false :error (str "No handler for type " type ", " result)})))

;; :query ->

(defn mk-query-engine [spec]
  (fn [query auth]
    (let [res (->> query
                   (map (fn [[k v]]
                          [k (do-query spec v auth nil)]))
                   (into {})
                   doall)
          errors (->> res
                      (map (fn [[k v]]
                             (when-not (:success? v)
                               [k (:error v)])))
                      (remove nil?)
                      (into {}))
          result (->> res
                      (map (fn [[k v]]
                             [k (:result v)]))
                      (into {}))]
      (if-not (empty? errors)
        {:success? false
         :errors errors}
        {:success? true
         :result result}))))

(defn ensure-id [obj]
  (assoc obj
    :_id (or (:_id obj)
             (str (org.bson.types.ObjectId.)))))

(defn update-timestamps [{:keys [updated-at created-at] :as obj}]
  (let [ts (util/now)]
    (-> obj
        (assoc :updated-at (or updated-at ts))
        (assoc :created-at (or created-at ts)))))

(defn mk-mutate-engine [spec]
  (fn [{:keys [type obj]} auth]
    (let [handler (get spec type)]
      (if handler
        (handler obj auth)
        {:success? false
         :error (str "No handler for type " type)}))))
