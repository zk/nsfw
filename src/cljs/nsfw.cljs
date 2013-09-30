(ns nsfw
  (:require [nsfw.pubsub :as ps]
            [nsfw.util :as util]
            [nsfw.dom :as $]
            [cljs.reader :as rdr]))

(def !buses (atom {}))

(defn lookup-bus [bus-name]
  (get @!buses bus-name))

(defn init-buses [& names]
  (swap! !buses
         merge
         (->> (interleave names (repeatedly ps/mk-bus))
              identity
              (apply hash-map))))

(def default-bus (ps/mk-bus))

(defn pub [msg]
  (ps/pub default-bus msg))

(defn sub
  ([msg f]
     (ps/sub default-bus msg f))
  ([f]
     (ps/sub default-bus f)))

(def !comps (atom []))

(defn add-comp [selector init]
  (swap! !comps concat [{:selector selector :init init}]))

(defn init-comps [$el comps]
  (doseq [{:keys [selector init]} comps]
    (doseq [$match ($/query $el selector)]
      (let [data-str ($/attr $match :data-nsfw)
            data (when data-str (rdr/read-string data-str))]
        (init $match data default-bus)))))

(defn init-page-comps []
  (init-comps (first ($/query :body)) @!comps))

(defn append [parent el-or-wd]
  (let [$el (if (map? el-or-wd)
              (:$el el-or-wd)
              el-or-wd)
        $el ($/node $el)]
    (init-comps ($/node [:div $el]) @!comps)
    ($/append parent $el)))