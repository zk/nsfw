(ns nsfw.mapbox
  (:require [reagent.core :as rea]
            [nsfw.util :as util]
            [clojure.string :as str]
            [clojure.set :as set]
            #_[cljsjs.mapbox-gl]))

(defn camel-keyword [k]
  (let [parts (-> k
                  name
                  (str/split "-"))]
    (if (> (count parts) 1)
      (keyword
        (str
          (str/lower-case (first parts))
          (->> parts
               rest
               (map str/capitalize)
               (apply str))))
      k)))

(defn kebob->camel
  "Turn a map's kebob-cased keywords into camelCased keywords.
  ex: {:foo-bar 0} -> {:foo-bar 0}"
  [m]
  (if (map? m)
    (->> m
         (map (fn [[k v]]
                [(camel-keyword k)
                 (cond
                   (map? v) (kebob->camel v)
                   (or (vector? v) (list? v)) (map kebob->camel v)
                   :else v)]))
         (into {}))
    m))


(defn map-render [{:keys [style]}]
  [:div.rx-map
   {:style (merge
             {:width "100%" :height "100%"}
             style)}])

(defn pad-coll [n coll val]
  (take n (concat coll (repeat val))))

(defn unwrap-children [children]
  (->> children
       (mapcat
         (fn [c]
           (if (or (list? (first c))
                   (vector? (first c)))
             c
             [c])))
       (remove empty?)
       (remove nil?)))



(defmulti remove-obj :key)
(defmulti add-obj :key)
(defmulti update-obj :key)

(defmethod remove-obj :default
  [{:keys [key]}]
  (println "No remove-obj for" key))

(defmethod add-obj :default
  [{:keys [key]}]
  (println "No add-obj for key:" key))

(defmethod update-obj :default
  [{:keys [key]}]
  (println "No update-obj for" key))

(def EVENT_KEYS [:on-dragend
                 :on-drag
                 :on-click
                 :on-mousedown
                 :on-mouseup
                 :on-center-changed
                 :on-bounds-changed
                 :on-zoom-changed])

(defn attach-event-listeners [obj props & [process-fn]]
  (let [events (select-keys props EVENT_KEYS)
        props (apply dissoc props EVENT_KEYS)]
    (doseq [[k v] events]
      (let [s (-> k
                  name
                  (str/replace "on-" "")
                  (str/replace "-" "_"))
            process-fn (or (when (map? process-fn)
                             (get process-fn k))
                           process-fn)]
        (.clearListeners
          google.maps.event
          obj
          s)
        (.addListener
          google.maps.event
          obj
          s
          (fn [e]
            (let [rest-args (when process-fn
                              (process-fn e))]
              (apply v e rest-args))))))))

(defmethod add-obj :marker
  [{:keys [gmap el]}]
  (let [props (second el)
        ;;props (assoc props :map gmap)
        ;;props (apply dissoc props EVENT_KEYS)
        marker (js/mapboxgl.Marker. (clj->js props))]
    (prn marker gmap)
    (.addTo marker gmap)
    #_(attach-event-listeners
        marker
        props
        (fn [e]
          (let [pos (.getPosition marker)]
            {:lat (.lat pos) :lng (.lng pos)})))
    marker))

(defmethod update-obj :marker
  [{:keys [gmap obj el]}]
  (let [props (second el)]
    (.setOptions obj (-> props
                         kebob->camel
                         clj->js))
    (attach-event-listeners
      obj
      props
      (fn [e]
        (let [pos (.getPosition obj)]
          {:lat (.lat pos) :lng (.lng pos)}))))
  obj)

(defmethod remove-obj :marker
  [{:keys [gmap obj el]}]
  (.setMap obj nil)
  nil)

(defmethod add-obj :polyline
  [{:keys [gmap obj el]}]
  (let [props (second el)
        props (assoc props :map gmap)
        polyline (google.maps.Polyline. (-> props
                                            kebob->camel
                                            clj->js))]
    (attach-event-listeners
      polyline
      props
      (fn [e]
        (let [pos (.-latLng e)]
          {:lat (.lat pos) :lng (.lng pos)})))
    polyline))

(defmethod remove-obj :polyline
  [{:keys [gmap obj el]}]
  (.setMap obj nil)
  nil)

(defmethod update-obj :polyline
  [{:keys [gmap obj el]}]
  (let [props (second el)
        {:keys [path geodesic stroke-weight stroke-opacity]} props]
    (.setOptions obj (-> props
                         kebob->camel
                         clj->js))
    (attach-event-listeners
      obj
      props
      (fn [e]
        (let [pos (.-latLng e)]
          {:lat (.lat pos)
           :lng (.lng pos)}))))
  obj)

(defn add-or-update [k gmap o el]
  (let [args {:key k
              :gmap gmap
              :obj o
              :el el}]
    (cond

      ;; map object, but no new element (remove)
      (and o (not el))
      (do
        (remove-obj args)
        nil)

      ;; element, but no representing (create)
      (and (not o) el)
      (add-obj args)

      ;; element and map marker (update)
      (and o el)
      (update-obj args))))

(defn populate-els [k gmap objs els]
  (let [len (max (count objs) (count els))
        padded-objs (pad-coll len objs nil)
        padded-els (pad-coll len els nil)
        pairs (->> (interleave padded-objs padded-els)
                   (partition 2))]
    (->> pairs
         (map (fn [[o m] pairs]
                (add-or-update k gmap o m)))
         (remove nil?)
         doall)))

(defn populate-map [gmap objs children]
  (let [children (unwrap-children children)
        groups (group-by first children)
        obj-keys (keys objs)
        el-keys (keys groups)
        new-objs (->> groups
                      (map (fn [[k els]]
                             [k (populate-els
                                  k
                                  gmap
                                  (get objs k)
                                  els)]))
                      (into {})
                      doall)]

    ;; clear non existent el keys
    (doseq [k (set/difference
                (set obj-keys)
                (set el-keys))]
      (doseq [obj (get objs k)]
        (remove-obj {:key k :obj obj :el nil})))

    new-objs))

(defn parse-args [args]
  (let [opts (if (map? (first args))
               (first args)
               nil)
        opts (or opts
                 {"center"
                  {:lat 37.7749 :lng -122.4194}
                  "zoom"
                  3})
        children (if (map? (first args))
                   (rest args)
                   args)]
    {:opts opts
     :children children}))

(defn center-from-map [gmap]
  (let [pos (.getCenter gmap)]
    {:lat (.lat pos) :lng (.lng pos)}))

(defn bounds-from-map [gmap]
  (let [bounds (.getBounds gmap)]
    (when bounds
      (let [ne (.getNorthEast bounds)
            sw (.getSouthWest bounds)]
        {:north (.lat ne)
         :east (.lng ne)
         :south (.lat sw)
         :west (.lng sw)}
        #_{:east (.lat ne)
           :north (.lng ne)
           :west (.lat sw)
           :south (.lng sw)}))))

(defn zoom-from-map [gmap]
  (.getZoom gmap))

(defn map-event-transforms [gmap]
  {:on-center-changed (fn [e]
                        [(center-from-map gmap)])
   :on-bounds-changed (fn [e]
                        [(bounds-from-map gmap)
                         (center-from-map gmap)
                         (zoom-from-map gmap)])
   :on-zoom-changed (fn [e]
                      [(.getZoom gmap)])
   :on-dragend (fn [e]
                 [(bounds-from-map gmap)
                  (center-from-map gmap)
                  (zoom-from-map gmap)])
   :on-drag (fn [e]
              [(bounds-from-map gmap)
               (center-from-map gmap)
               (zoom-from-map gmap)])
   :on-click (fn [e]
               (let [pos (.-latLng e)]
                 [{:lat (.lat pos) :lng (.lng pos)}]))})

(defonce setup-at
  (set!
    (.-accessToken js/mapboxgl)
    "pk.eyJ1IjoiaGV5emsiLCJhIjoiY2l2Z2J5NmdyMDE3YzJ4bG1kYWNsd2FkcyJ9.wDoOVs2uTd8x-cftHb63jA"))

(defn map-did-mount [this]
  (let [map-canvas (rea/dom-node this)
        args (rest (rea/argv this))
        {:keys [opts children]} (parse-args args)
        initial (:initial opts)
        initial-bounds (-> opts :initial :bounds)
        gmap (js/mapboxgl.Map.
               (-> {:container map-canvas
                    :style "https://s3.amazonaws.com/flnassets/mbstyle.json"}
                   kebob->camel
                   clj->js))
        #_(js/google.maps.Map.
            map-canvas
            (-> (merge
                  opts
                  initial)
                kebob->camel
                clj->js))
        objs nil #_(populate-map
                     gmap
                     {}
                     children)]
    #_(when initial-bounds
        (.fitBounds gmap (clj->js initial-bounds)))
    #_(attach-event-listeners gmap opts (map-event-transforms gmap))
    (rea/set-state this {:gmap gmap :objs objs})
    gmap))

(defn sigdiff-latlng? [p1 p2]
  false)

(defn dist-between [a b]
  (let [a (.abs js/Math a)
        b (.abs js/Math b)]
    (if (> a b)
      (- a b)
      (- b a))))

(defn sigdiff-bounds? [c1 c2]
  (let [max-delta 0.0001]
    (or
      (> (dist-between (:north c1) (:north c2)) max-delta)
      (> (dist-between (:south c1) (:south c2)) max-delta)
      (> (dist-between (:east c1) (:east c2)) max-delta)
      (> (dist-between (:west c1) (:west c2)) max-delta))))

(defn map-rec-props [this next-props]
  (let [{:keys [gmap objs]} (rea/state this)
        {:keys [opts children]} (parse-args (rest next-props))

        center (:center opts)
        bounds (:bounds opts)
        zoom (:zoom opts)
        opts (dissoc opts :center :bounds)
        new-objs (populate-map gmap objs children)]
    #_(.setOptions gmap (-> opts
                            kebob->camel
                            clj->js))

    (when center #_(and center (sigdiff-latlng? center (center-from-map gmap)))
          (.setCenter gmap (clj->js center)))
    (when (and bounds (sigdiff-bounds? bounds (bounds-from-map gmap)))
      (.fitBounds gmap (clj->js bounds)))
    (when zoom
      (.setZoom gmap zoom))
    (attach-event-listeners
      gmap
      opts
      (map-event-transforms gmap))
    (rea/set-state this {:gmap gmap :objs new-objs})))

(defn $map-view [& children]
  (rea/create-class
    {:reagent-render map-render
     :component-did-mount map-did-mount
     :component-will-receive-props map-rec-props}))


(defn meters-between [[p1 p2]]
  (.computeDistanceBetween google.maps.geometry.spherical
    (google.maps.LatLng. (clj->js p1))
    (google.maps.LatLng. (clj->js p2))))
