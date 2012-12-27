(ns nsfw.clojuredocs
  (:require [nsfw.server :as server]
            [nsfw.webapp :as webapp]
            [nsfw.html :as html]))

(defn functions []
  (->> 'clojure.core
       find-ns
       ns-publics
       (map second)
       (map meta)
       (map #(-> %
                 (assoc :ns (symbol (str (:ns %))))
                 (dissoc :inline :inline-arities)))
       (sort-by :name)))

(def fns (->> (functions)
              (map #(select-keys % [:name :ns :arglists :doc]))))

(server/start :entry
              (webapp/routes
               [""] (webapp/cs
                     :examples
                     :entry 'nsfw.clojuredocs
                     :data {:functions fns})))