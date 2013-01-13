(ns nsfw-site.app
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.util :as util]
            [nsfw.storage :as storage]
            [nsfw.geo :as geo]
            [nsfw.components :as comp]
            [cljs.reader :as reader]))

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

(def supported-versions {:safari "Safari 1.0"})


(def $body (dom/$ "body"))

(def hero (comp/bleed-box
           {:img "/img/dog3.jpg"}
           [:div.navbar
            [:h1
             [:span.nsfw-icon
              (:alembic icon-chars)]
             "NSFW"]]
           [:div.hero-content
            [:h3 "Get web stuff done with Clojure"]
            [:p "(yey)"]
            #_[:p "NSFW is a collection of tasty Clojure bits."]
            #_[:p
             "We aim to make the hard stuff easy, and we've got everything "
             "from date math, to one-line webservers, to a bunch of random "
             "(but eminently useful) one-offs."]
            #_[:ul.supported-browsers
               [:li
                [:img {:src "/img/chrome-icon.png"
                       :width 75
                       :height 75}]
                [:div.caption "Chrome 23+"]]
               [:li
                [:img {:src "/img/safari-icon.png"
                       :width 75
                       :height 75}]
                [:div.caption "Safari 6+"]]
               [:li
                [:img {:src "/img/firefox-icon.png"}]
                [:div.caption "Firefox 17+"]]]
            #_[:div.sign-in-twitter
               [:img {:src "https://twitter.com/images/resources/twitter-bird-dark-bgs.png"}]
               "Sign in with Twitter"]]))

(def banner (dom/$ [:div.post-bleed-banner
                    "Divider!"]))


(defn data-input [atom & [initial-value]]
  (let [textarea   (dom/$ [:textarea {:spellcheck "false" :rows 5}
                           (or initial-value (pr-str @atom))])
        el         (dom/$ [:div.data-input textarea])
        on-change  (fn [v]
                     (try
                       (reset! atom (-> textarea
                                        dom/val
                                        reader/read-string))
                       (dom/rem-class textarea :error)
                       (catch js/Error e
                         (dom/add-class textarea :error))))]
    (dom/val-changed textarea on-change)
    el))

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

(defn text-atom-vis [atom]
  (bind/render
   (dom/$ [:div.code])
   atom
   (fn [new old el] (pr-str new))))

(defn html-atom-vis [atom]
  (bind/render
   (dom/$ [:div.html-vis])
   atom
   (fn [new old el]
     (try*
      (dom/$ new)
      (catch e
          [:div.err "Couldn't parse html."])))))

(def html-section
  (let [a (atom [:div#my-div
                 [:h3.banner "hello world"]
                 [:ol
                  [:li "baz"]
                  [:li "bar"]]
                 [:input {:type "text" :placeholder "text here!"}]])]
    [:div.row
     [:div.span10
      [:h3 "HTML"]]]
    [:div.row
     [:div.span6

      [:p "HTML is generated using code found in the "
       [:code "nsfw.dom"]
       " clojurescript namespace."]
      [:p
       "For example, to generate the snippet you see "
       "to the bottom right, you'd do someing like:"]
      [:pre
       "(def $body (dom/$ \"body\"))"
       "\n\n"
       "(dom/append $body [:div
                    [:h3.banner \"hello world\"]
                    [:ol
                      [:li \"foo\"]
                      [:li \"bar\"]
                      [:li \"baz\"]]])
"]
      [:p "It's common practice to prefix bindings holding DOM elements with "
       [:code "$"]
       ", i.e. "
       [:code "$body"]
       ", "
       [:code "$open-button"]
       ", etc."]]
     [:div.span6
      [:div.example.html-example
       (data-input a "[:div#my-div
  [:h3.banner \"hello world\"]
  [:ol
    [:li \"baz\"]
    [:li \"bar\"]]
  [:input {:type \"text\" :placeholder \"text here!\"}]]")
       (html-atom-vis a)]]]))

(def basic-structure
  [:div.container
   [:div.row
    [:div.span12
     [:h2 "Basic Structure"]]]
   html-section])

(defn local-storage-example []
  (let [input (dom/$ [:input {:type "text" :id "my-key" :placeholder "ex. [1 2 3]"}])
        output (dom/$ [:em (pr-str (:my-key storage/local))])]
    (dom/on-enter input (fn [_]
                          (try
                            (storage/lset! :my-key (reader/read-string (dom/val input)))
                            (dom/text output (pr-str (:my-key storage/local)))
                            (dom/rem-class input :error)
                            (catch js/Object e
                              (dom/add-class input :error)
                              (throw e)))))
    [:div.example.local-storage-example
     [:pre
      ";; Local storage acts like a map / hash."
      "\n\n"
      "(storage/lset! :my-key \"foo\")"
      "\n\n"
      "(:my-key storage/local) ;=> \"foo\""
      "\n\n"
      "(storage/lget! :my-key) ;=> \"foo\""
      "\n\n"
      "(storage/lget! :none \"default\")\n"
      ";=> \"default\""
      ]
     [:form.form-inline
      [:label.control-label {:for "my-key"} "my-key"]
      ": "
      input
      " -> "
      output
      " (last value)"]]))

(def html5-storage
  [:div.container
   [:div.row
    [:div.span12
     [:h2 "Local Storage"]]]
   [:div.row
    [:div.span6
     [:p "Require " [:code "[nsfw.storage :as storage]"]]
     [:p "Local storage persistent key-value database, providing the get / set interfaces. Keys can be strings, and values can be any JavaScript value type."]
     [:p "NSFW extends the JS "
      [:code "Storage"]
      " object to implement several of Clojure's lookup interface, "
      [:code "ILookup"]
      ", so you can access local storage like you would a Clojure Map."
      "However, keys are set on local storage using a specific setter, as local "
      "storage is mutable."]
     [:p "Notice the value you enter for "
      [:code "my-key"]
      [:em " persists through page reloads"]
      "."]
     [:p "HTML5 local storage only allows you to store serializable JS objects, meaning most clojure data structures are turned into strings when stored natively. Therefore, NSFW storage helpers use "
      [:code "pr-str"]
      " and "
      [:code "read-string"]
      " to store and retreive values respectively."]]
    [:div.span6
     (local-storage-example)]]])



(def html5-geoloc
  [:div.container
   [:div.row
    [:div.span12
     [:h2 "Mapping / Geolocation"]]]
   [:div.row
    [:div.span6
     [:p "Require " [:code "[nsfw.geo :as geo]"]]
     [:p "Geolocation provides locaiton data for your users."]
     [:pre
      ";; Define Map Div\n"
      "(def $map (dom/$ [:div#map]))"
      "\n\n"
      ""
      "(def map (geo/map $map\n"
      "                  {:zoom 3\n"
      "                   :center [39 -98]}))"
      "\n\n"
      "(geo/pos (fn [{:keys [latlng]}]\n"
      "           (geo/center map latlng))"]]
    [:div.span6
     (let [$map (dom/$ [:div.map "MAP"])
           map (geo/map $map {:zoom 3 :center [39 -98]})]
       [:div.geoloc-example.example
        $map
        [:div
         (let [btn (dom/$ [:a.btn "Zoom To My Location"])]
           (dom/click btn (fn [e]
                            (dom/add-class btn :loading)
                            (geo/pos
                             (fn [{:keys [latlng]}]
                               (dom/rem-class btn :loading)
                               (geo/center map latlng)
                               (geo/zoom map 10))
                             (fn [err]
                               (log "err")
                               (dom/rem-class btn :loading))))))]])]]])



(def bleed-box-example
  (comp/bleed-box
   {:mp4 "/vid/flarez.mp4"
    :webm "/vid/flarez.webm"
    :ogv "/vid/flarez.ogv"}
   [:div.example.bleed-box-example
    [:div.container
     [:div.row
      [:div.span6
       [:h2 "Bleed Box"]
       [:p "Require " [:code "[nsfw.components :as comp]"]]
       [:p "Full-bleed background images or video!!!"]]
      [:div.span6
       [:div.example
        [:pre
         ";; Image Bleed Box"
         "\n"
         "(comp/bleed-box\n"
         "  {:img \"/path/to/image.jpg\"}\n"
         "  [:h1 \"Bleed Box Content\"])"
         "\n\n"
         ";; Video Bleed Box\n"
         "(comp/bleed-box\n"
         "  {:poster \"/path/to/poster.jpg\"\n"
         "   :mp4 \"/path/to.mp4\"\n"
         "   :webm \"/path/to.webm\"\n"
         "   :ogv \"/path/to.ogv\"\n"
         "   :delay 2000}\n"
         "  [:h1 \"Bleed Box Content\"])"]]]]]]))

(def event-binding
  (dom/$ [:div.container
          [:div.row
           [:div.span12
            [:h2 "Event Binding"]]]
          [:div.row
           [:div.span6
            [:p "Require " [:code "[nsfw.dom :as dom]"]]]]]))

(defn button-example [cls]
  (let [button (dom/$ [:a {:class (str "btn loading " cls)}
                       "Click Me!"])
        el (dom/$ [:div.button-row
                   [:label (str ".btn." cls ".loading")]
                   button])]
    (dom/click button (fn [e]
                        (.preventDefault e)
                        (dom/rem-class button :loading)
                        (u/timeout #(dom/add-class button :loading) 2000)))
    el))

(def loading-indicators
  (dom/$ [:div.container
          [:div.row
           [:div.span12
            [:h2 "Loading Indicators"]]]
          [:div.row
           [:div.span6
            [:p "Enable a loading state by adding a "
             [:code "loading"]
             " css class to many nsfw components."]
            [:p "Loading is supported by buttons, and most block-level elements."]]
           [:div.span6
            [:div.example.loading-example
             (map button-example ["btn-large" "btn" "btn-small" "btn-mini"])
             [:div.button-row
              (-> (dom/$ [:a.btn.btn-block.loading ".btn.btn-block.loading"])
                  (dom/click (fn [e el]
                               (dom/rem-class el :loading)
                               (.preventDefault e)
                               (u/timeout #(dom/add-class el :loading) 2000))))]
             [:div.div-loading.loading
              [:code "[:div.loading]"]]
             [:textarea.loading
              "[:textarea.loading]"]]]]]))


(def $body (dom/$ "body"))

(-> $body
    (dom/append hero)
    (dom/append banner)
    (dom/append basic-structure)
    (dom/append event-binding)
    (dom/append loading-indicators)
    (dom/append html5-storage)
    (dom/append html5-geoloc)
    (dom/append bleed-box-example)
    (dom/append [:div (repeat 10 [:br])]))