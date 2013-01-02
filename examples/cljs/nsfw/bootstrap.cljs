(ns nsfw.bootstrap
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]))

(def icon-chars
  '{:snowman ☃
    :umbrella ☂
    :delta Δ
    :sun ☀
    :section §
    :dagger †
    :double-dagger ‡
    :wtf ⁈
    :broken-bar ¦
    :interrobang ‽
    :white-circle-dots ⚇
    :white-circle-dot ⚆
    :black-circle-dots ⚉
    :black-circle-dot ⚈
    :crossed-swords ⚔
    :dharma-wheel ☸
    :circled-bullet ⦿
    :floral-heart ❦
    :circled-white-star ✪
    :black-square ■
    :white-square □
    :black-diamonds ❖
    :heavy-ol-black-star ✮
    :nw-ne-arrows ⤧
    :snowflake ❄
    :hot-springs ♨
    :airplane ✈
    :white-smiling-face ☺
    :cloud ☁
    :nw-crossing-ne ⤲
    :anchor ⚓
    :reference-mark ※
    :comet ☄
    :alembic ⚗})

(defn bleed-box [opts & content]
  (dom/$ [:div.bleed-box
          [:div.full-bleed {:style (format "background-image: url(%s);" (:img opts))}]
          [:div.bleed-box-content content]]))

(def body (dom/$ "body"))

(def hero (bleed-box
           {:img "/img/dog3.jpg"}
           [:div.navbar
            [:h1
             [:span.nsfw-icon
              (:alembic icon-chars)]
             "NSFW"]]
           [:div.hero-content
            [:h1 "Get stuff done with Clojure."]
            [:p "NSFW is a collection of tasty Clojure bits."]
            [:p "We aim to make the hard stuff easy, and we've got everything from date math, to one-line webservers, to a bunch of random (but eminently useful) one-offs."]
            [:div.sign-in-twitter
             [:img {:src "https://twitter.com/images/resources/twitter-bird-dark-bgs.png"}]
             "Sign in with Twitter"]]))

(def banner (dom/$ [:div.post-bleed-banner
                    "Divider!"]))

(def icons
  (dom/$
   [:div.icons
    [:h2 "Icons"]
    [:ul.icon-overview
     (->> icon-chars
          (sort-by first)
          (map
           (fn [[k v]]
             (let [name (name k)]
               [:li.icon-cubby
                [:span.glyph (str v)]
                [:br]
                " " name]))))]
    [:div.clear]]))

(defn main []
  (-> body
      (dom/append hero)
      (dom/append banner)
      (dom/append icons)
      #_(dom/append (repeat 100 [:br]))))
