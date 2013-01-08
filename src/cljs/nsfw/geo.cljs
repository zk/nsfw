(ns nsfw.geo
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom])
  (:refer-clojure :exclude [map]))

(def geoloc (.-geolocation js/navigator))

(defn geoloc->map [g]
  (let [c (.-coords g)]
    {:timestamp (.-timestamp g)
     :accuracy (.-accuracy c)
     :altitude (.-altitude c)
     :altitude-accuracy (.-altitudeAccuracy c)
     :heading (.-heading c)
     :lat (.-latitude c)
     :lng (.-longitude c)
     :speed (.-speed c)}))

(defn get-pos [callback]
  (.getCurrentPosition geoloc
                       (fn [geoloc]
                         (callback (geoloc->map geoloc)))))

(defn map [el & [opts]]
  (google.maps.Map. el (clj->js
                        {:zoom 2
                         :mapTypeId google.maps.MapTypeId.ROADMAP
                         :center (google.maps.LatLng. 37.7750 -122.4183)
                         :scrollwheel false})))


(defn center-on [map lat lng]
  (.setCenter map (google.maps.LatLng. lat lng)))

(defn zoom [map level]
  (.setZoom map level))