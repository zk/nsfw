(ns nsfw.css
  (:require [clojure.string :as str]))

(defn prefix
  ([m]
   (->> m
        (map (fn [[k v]]
               (prefix k v)))
        (reduce merge)))
  ([k v]
   (->> ["-webkit-"
         "-moz-"
         "-ms-"
         ""]
        (map (fn [prefix]
               [(->> k
                     name
                     (str prefix)
                     keyword)
                v]))
        (into {}))))

(defn transform [& parts]
  {:transform (apply str parts)
   :-webkit-transform (apply str parts)})

(defn transition [v]
  {:transition v
   :-webkit-transition (if (string? v)
                         (str/replace v #"transform" "-webkit-transform")
                         v)
   :-moz-transition v
   :-ms-transition v
   :-o-transition v})

(defn scale [v]
  (transform "scale(" v ")"))

(defn elq [n & [bps]]
  (let [bps (or bps {:xs 375
                     :sm 768})
        bp (->> bps
                (sort-by second)
                (drop-while #(> n (second %)))
                first)]
    (when bp
      {:data-elq-size (first bp)})))

(def vertical-center
  (merge
    {:position 'absolute
     :top "50%"}
    (transform "translateY(-50%)")))

(def horizontal-center
  (merge
    {:position 'absolute
     :left "50%"}
    (transform "translateX(-50%)")))

(def center-vertically vertical-center)

(def center-both
  (merge
    {:position 'absolute
     :top "50%"
     :left "50%"}
    (transform "translate(-50%,-50%)")))

(defn anim-height [height]
  (merge
    {:height height
     :overflow 'hidden}
    (transition "height 0.2s ease")))

(def offscreen-right
  (merge
    (transform "translateX(120%)")))

(defn png-outline [size color]
  (merge
    (prefix
      :filter
      (str "
drop-shadow(" size " 0px 0 " color ")
drop-shadow(0px " size " 0 " color ")
drop-shadow(-" size " 0px 0 " color ")
drop-shadow(0px -" size " 0 " color ") "))))

(def blue-bg-color "#034F84")
(def blue-fg-color "white")

(def red-bg-color "#DD4132")
(def red-fg-color "white")
