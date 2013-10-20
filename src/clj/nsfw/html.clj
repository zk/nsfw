(ns nsfw.html
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as hiccup-page]
            [clojure.zip :as zip])
  (:import [org.pegdown PegDownProcessor]
           [org.pegdown Parser]
           [org.pegdown Extensions]))

(defn href
  "ex. (href \"http://google.com\" \"Google!\" :rel \"nofollow\")"
  [href content & opts]
  (let [opts (apply hash-map opts)]
    [:a (merge opts {:href href})
     content]))

(defn script [path]
  [:script {:type "text/javascript" :src path}])

(defn stylesheet [path]
  [:link {:rel "stylesheet" :href path}])

(defn css-rule [rule]
  (let [sels (reverse (rest (reverse rule)))
        props (last rule)]
    (str (apply str (interpose " " (map name sels)))
         "{" (apply str (map #(str (name (key %))
                                   ":"
                                   (if (keyword? (val %))
                                     (name (val %))
                                     (val %))
                                   ";") props)) "}")))

(defn css
  "ex.
    (css [:h1 {:height :20px}] [:h2 {:height :10px}])
    => h1{height:20px;}h2{height:10px}"
  [& rules]
  (apply str (map css-rule rules)))

(defn embed-css
  "Quick and dirty dsl for inline css rules, similar to hiccup.

   ex. `(css [:h1 {:color \"blue\"}] [:div.content p {:text-indent \"1em\"}])`
   => `h1 {color: blue;} div.content p {text-indent: 1em;}`"
  [& rules]
  [:style {:type "text/css"}
   (apply str (map css-rule rules))])

(defn html5 [& body]
  (hiccup-page/html5 body))

(defn html [body]
  (hiccup/html body))

(defn markdown [s]
  (let [pd (PegDownProcessor. (int (bit-or Extensions/AUTOLINKS
                                           Extensions/FENCED_CODE_BLOCKS
                                           Extensions/TABLES)))]
    (.markdownToHtml pd s)))

;; Components
(defn parse-comp [[tag opts-raw & body]]
  (let [opts (if (map? opts-raw)
               opts-raw
               {})
        body (if (map? opts-raw)
               body
               (concat [opts-raw] body))]
    [opts body]))

(defn tag-match? [components node]
  (and (coll? node)
       (keyword? (first node))
       (components (first node))))

(defn vec-or-seq? [o]
  (or (seq? o)
      (vector? o)))

(defn nested-seq? [o]
  (and (vec-or-seq? o)
       (vec-or-seq? (first o))))

(defn has-nested-seqs? [node]
  (when (vec-or-seq? node)
    (some nested-seq? node)))

(defn unwrap-nested-seqs [node]
  (let [nested-seqs (filter nested-seq? node)]
    (loop [nested-seqs nested-seqs
           node node]
      (if (empty? nested-seqs)
        node
        (recur (rest nested-seqs)
               (let [ns (first nested-seqs)
                     pre (take-while #(not= ns %) node)
                     post (->> node
                               (drop-while #(not= ns %))
                               (drop 1))]
                 (vec (concat pre ns post))))))))

(defn tag-structure-as-comp [structure tag]
  (let [[el & rem] structure
        opts (if (map? (first rem))
               (assoc (first rem) :data-nsfw-comp tag)
               {:data-nsfw-comp tag})
        rem (if (map? (first rem))
              (rest rem)
              rem)
        res (vec (concat [el opts] rem))]
    res))

(defn apply-comps [components structure]
  (let [z (zip/zipper #(or (vector? %) (seq? %))
                      identity
                      (fn [node children]
                        (with-meta (vec children) (meta node)))
                      structure)]
    (loop [n z]
      (if (zip/end? n)
        (zip/root n)
        (recur (zip/next
                (cond
                 (tag-match? components (zip/node n))
                 (zip/edit n (fn [node]
                               (let [tag (first node)
                                     [opts body] (parse-comp node)
                                     f (components tag)
                                     res (if (fn? f)
                                           (f opts body)
                                           f)
                                     res (tag-structure-as-comp res tag)
                                     res (apply-comps components res)]
                                 res)))

                 (has-nested-seqs? (zip/node n))
                 (zip/edit n unwrap-nested-seqs)

                 :else n)))))))

(defn apply-comps! [!components structure]
  (apply-comps @!components structure))

(defn mk-comp [!components]
  (fn [& comps]
    (->> comps
         (partition 2)
         (reduce #(assoc %1 (first %2) (second %2)) {})
         (swap! !components merge))))

(defn mk-transformer [!components]
  (fn [body-coll]
    (let [body-coll (if (-> body-coll first keyword?)
                      [body-coll]
                      body-coll)]
      (->> (map #(apply-comps! !components %) body-coll)
           html))))

(defn response [body]
  {:body body
   :headers {"Content-Type" "text/html;charset=utf-8"}})