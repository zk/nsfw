(ns nsfw.html
  (:use [hiccup core]
        [clojure.contrib.string :only (as-str)]))

(defmacro defopts
  "Allows passing of key-value pairs at the end of a function call,
   which will be collected into a map named `opts`.

   Example:

       (defopts sum [a b]
         (merge opts {:total (+ a b)}))

       (sum 1 2 :difficulty \"simple\" :calc-time (System/currentTimeMillis))
       ;;=> {:total 3 :difficulty \"simple\" :calc-time 123456789012}"
  [name arg-vec & body]
  (let [args arg-vec
        opts (symbol "opts")]
    `(defn ~name [~@args & ~opts]
       (let [~opts (apply hash-map ~opts)]
         ~@body))))

(defn href [href content & opts]
  (let [opts (apply hash-map opts)]
    (html [:a (merge opts {:href href})
           content])))

(defn image-url [name]
  (str "/images/" name))

(defn image [name & opts]
  (let [opts (apply hash-map opts)
        opts (merge {:src (image-url name)} opts)]
    (html [:img opts])))

(defn meta-tag [he content]
  (html [:meta {:http-equiv he :content content}]))

(defopts label [content]
  (html [:label opts
         content]))




