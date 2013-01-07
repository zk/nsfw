(ns nsfw.bootstrap
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.bind :as bind]
            [nsfw.storage :as storage]
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

(defn bleed-box [opts & content]
  (dom/$ [:div.bleed-box
          [:div.full-bleed {:style (format "background-image: url(%s);" (:img opts))}]
          [:div.bleed-box-content content]]))

(def $body (dom/$ "body"))

(def hero (bleed-box
           {:img "/img/dog3.jpg"}
           [:div.navbar
            [:h1
             [:span.nsfw-icon
              (:alembic icon-chars)]
             "NSFW"]]
           [:div.hero-content
            [:h3 "Get stuff done with Clojure."]
            [:p "NSFW is a collection of tasty Clojure bits."]
            [:p "We aim to make the hard stuff easy, and we've got everything from date math, to one-line webservers, to a bunch of random (but eminently useful) one-offs."]
            [:p "NSFW is built on many, many great libraries. Check out the project.clj for more info."]
            #_[:div.sign-in-twitter
               [:img {:src "https://twitter.com/images/resources/twitter-bird-dark-bgs.png"}]
               "Sign in with Twitter"]]))

(def banner (dom/$ [:div.post-bleed-banner
                    "Divider!"]))


(defn data-input [atom]
  (let [textarea   (dom/$ [:textarea {:spellcheck "false" :rows 5}
                           (pr-str @atom)])
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
  (let [a (atom [:div
                 [:h3 "hello world"]
                 [:ol
                  [:li "foo"]
                  [:li "bar"]
                  [:li "baz"]]])]
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
                    [:h3 \"hello world\"]
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
       (data-input a)
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
      ";; Local storage acts like a map / hash / dict."
      "\n\n"
      "(storage/lset! :my-key \"foo\")"
      "\n\n"
      "(:my-key storage/local) ;;=> \"foo\""
      "\n\n"
      "(storage/lget! :my-key) ;;=> \"foo\""
      "\n\n"
      "(storage/lget! :none \"default\") ;;=> \"default\""
      ]
     [:form.form-inline
      [:label.control-label {:for "my-key"} "my-key"]
      ": "
      input
      " -> "
      output
      " (last value)"]]))

(defn indexdb-example []
  [:div.row
   [:div.span12
    [:h3""]]
   ])

(def html5-storage
  [:div
   [:div.row
    [:div.span12
     [:h3 "Storage"]]]
   [:div.row
    [:div.span6
     [:p
      "NSFW provides a simple set of local storage helpers. More information on "
      "HTML5 local storage can be found "
      [:a {:href "http://diveintohtml5.info/storage.html"}
       "here"]
      " and "
      [:a {:href "http://www.html5rocks.com/en/features/storage"} "here"]
      "."]
     [:p
      "There are three ways to store and access data locally: a "
      [:em "key-value store"]
      ", a flat-file database with "
      [:em "heirarchical key-value persistence and basic indexing"]
      ", and a full-featured "
      [:em "SQL storage and query engine"]
      "."]
     [:p "Unfortunately, at the time of this writing, support for the IndexedDB and the SQL engine is dicey across browser vendors, so we haven't made support a priority."]]
    [:div.span6
     [:div.example
      [:pre
       ";; Local Storage"
       \newline
       "(:my-key storage/local) => \"foo\""
       \newline \newline]]]]
   [:div.row
    [:div.span12
     [:h3 "Local Storage"]]]
   [:div.row
    [:div.span6
     [:p
      "Require "
      [:code "[nsfw.storage :as storage]"]]
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
      " persists through page reloads."]
     [:p "HTML5 local storage only allows you to store serializable JS objects, meaning most clojure data structures are turned into strings when stored natively. Therefore, NSFW storage helpers use "
      [:code "pr-str"]
      " and "
      [:code "read-string"]
      " to store and retreive values respectively."]]
    [:div.span6
     (local-storage-example)]]
   [:div.row
    [:div.span12
     [:h3 "IndexDB"]]]
   [:div.row
    [:div.span6
     [:p "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat."]]]])

(def html5-specific
  [:div.container
   [:div.row
    [:div.span10
     [:h2 "HTML5 Helpers"]]]
   html5-storage])

(defn main []
  (-> $body
      (dom/append hero)
      (dom/append banner)
      (dom/append basic-structure)
      (dom/append html5-specific)
      (dom/append [:div (repeat 10 [:br])])))