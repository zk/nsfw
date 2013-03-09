(ns nsfw.dom
  (:use [nsfw.util :only [log]])
  (:require [dommy.template :as template]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.style :as style]
            [goog.events :as events]
            [goog.dom.query]
            [cljs.core :as cc]
            [nsfw.util :as util])
  (:refer-clojure :exclude [val replace remove empty drop]))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ICollection
  (-conj [coll o]
    (throw "Error: Can't conj onto a NodeList.")))

(defn ge->map
  "Turns a google closure event into a map.
   See http://goo.gl/87L84 for more info."
  [e]
  {:type (.-type e)
   :timestamp (.-timestamp e)
   :target (.-target e)
   :current-target (.-currentTarget e)
   :related-target (.-relatedTarget e)
   :offset-x (.-offsetX e)
   :offset-y (.-offsetY e)
   :client-x (.-clientX e)
   :client-y (.-clientY e)
   :screen-x (.-screenX e)
   :screen-y (.-screenY e)
   :button (.-button e)
   :key-code (.-keyCode e)
   :ctrl-key (.-ctrlKey e)
   :alt-key (.-altKey e)
   :shift-key (.-shiftKey e)
   :meta-key (.-metaKey e)
   :default-prevented (.-defaultPrevented e)
   :state (.-state e)
   :event e})

(defn ensure-coll [el]
  (if (coll? el)
    el
    [el]))

;; Dom

(defn root []
  (aget (dom/getElementsByTagNameAndClass "html") 0))

(defn selector
  ([s]
     (dom/query s))
  ([base s]
     (dom/query s base)))

(defn $
  ([o]
     (cond
      (coll? o) (template/node o)
      (or (keyword? o)
          (string? o)) (selector (name o))
      :else o))
  ([base o]
     (selector base (name o))))

(defn unwrap [el]
  (if (coll? el)
    (first el)
    el))

(defn val
  ([el]
     (.-value (unwrap el)))
  ([el new-value]
     (set! (.-value (unwrap el)) new-value)))

(defn wrap-content
  [content]
  (cond
   (and (coll? content)
        (keyword? (first content))) ($ content)
   (string? content) ($ content)
   :else content))

(defn append [els content]
  (doseq [el (ensure-coll els)]
    (if el
      (if (and (coll? content)
               (not (keyword? (first content))))
        (doseq [c content]
          (when c
            (.appendChild el (wrap-content c)))
          (when content
            (when-let [on-insert (aget content "on-insert")]
              (on-insert el))))
        (do (when content
              (.appendChild el (wrap-content content)))
            (when content
              (when-let [on-insert (aget content "on-insert")]
                (on-insert el)))))
      (throw "Can't call dom/append on a null element")))
  els)

(defn prepend [els content]
  (doseq [el (ensure-coll els)]
    (if el
      (if (and (coll? content)
               (not (keyword? (first content))))
        (doseq [c content]
          (when c
            (dom/insertChildAt el (wrap-content c) 0))
          (when content
            (when-let [on-insert (aget content "on-insert")]
              (on-insert el))))
        (do (when content
              (dom/insertChildAt el (wrap-content content) 0))
            (when content
              (when-let [on-insert (aget content "on-insert")]
                (on-insert el)))))
      (throw "Can't call dom/append on a null element")))
  els)

(def apd append)

(defn append-to [child parents]
  (doseq [el (ensure-coll parents)]
    (append el child))
  child)

(defn style [els css-map]
  (let [jsobj (clj->js css-map)]
    (doseq [el (ensure-coll els)]
      (style/setStyle el jsobj)))
  els)

(defn size [el]
  (let [res (style/getSize el)]
    [(.-width res)
     (.-height res)]))

(defn attrs
  [els m]
  (doseq [el (ensure-coll els)]
    (doseq [key (keys m)]
      (.setAttribute el (name key) (get m key))))
  els)

(defn text [els text]
  (doseq [el (ensure-coll els)]
    (dom/setTextContent el (str text)))
  els)

(defn replace [els content]
  (doseq [el (ensure-coll els)]
    (dom/replaceNode content el))
  els)

(defn remove [els]
  (doseq [el (ensure-coll els)]
    (dom/removeNode el)))

(defn empty [els]
  (doseq [el (ensure-coll els)]
    (dom/removeChildren el))
  els)

(defn has-class? [el cls]
  (classes/has el (name cls)))

(defn add-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/add el (name cls)))
  els)

(defn rem-class [els cls]
  (doseq [el (ensure-coll els)]
    (classes/remove el (name cls)))
  els)

(def body ($ "body"))

;; Events

(defn prevent [e]
  (.preventDefault (:event e)))

(defn stop-prop [e]
  (.stopPropagation (:event e)))

(defn onload [f]
  (set! (.-onload js/window) f))

(defn listen [els evt f]
  (doseq [el (ensure-coll els)]
    (events/listen el (name evt) #(f (ge->map %) el)))
  els)

(defn handler [evt]
  (fn
    ([els f]
       (listen els (name evt) f))
    ([els sel f]
       (listen (dom/$ els sel) (name evt) f)
       els)))

(def click (handler :click))
(def dblclick (handler :dblclick))

(def mousedown (handler :mousedown))
(def mouseup (handler :mouseup))
(def mouseover (handler :mouseover))
(def mouseout (handler :mouseout))
(def mousemove (handler :mousemove))
(def selectstart (handler :selectstart))

(def keypress (handler :keypress))
(def keydown (handler :keydown))
(def keyup (handler :keyup))
(def blur (handler :blur))

(def change (handler :change))
(def select (handler :select))
(def submit (handler :submit))
(def input (handler :input))

(def dragstart (handler :dragstart))
(def dragenter (handler :dragenter))
(def dragover (handler :dragover))
(def dragleave (handler :dragleave))
(def drop (handler :drop))

(def touchstart (handler :touchstart))
(def touchmove (handler :touchmove))
(def touchend (handler :touchend))
(def touchcancel (handler :touchcancel))

(def contextmenu (handler :contextmenu))
(def error (handler :error))
(def help (handler :help))
(def load (handler :load))
(def losecapture (handler :losecapture))
(def readstatechange (handler :readstatechange))
(def resize (handler :resize))
(def scroll (handler :scroll))
(def unload (handler :unload))

(def hashchange (handler :hashchange))
(def pagehide (handler :pagehide))
(def pageshow (handler :pageshow))
(def popstate (handler :popstate))

(defn focus
  ([el]
     (.focus el)
     el)
  ([els f]
     (listen els :focus f)))

(defn select [el]
  (.select el))

(defn match-key [els key f]
  (doseq [el (ensure-coll els)]
    (keyup el (fn [e]
                (let [kc (.-keyCode e)]
                  (when (= key kc)
                    (f el (val el)))))))
  els)

(defn val-changed
  ([els f]
     (doseq [el (ensure-coll els)]
       (let [f (fn [e]
                 (f el (val el)))]  ; el, then val - side effects only
         (keyup el f)
         (change el f)))
     els)
  ([base sel f]
     (let [$el ($ base)
           $target ($ $el sel)]
       (val-changed $target f)
       $el)))

(defn on-enter [els f]
  (doseq [el (ensure-coll els)]
    (keydown el (fn [e]
                  (when (= 13 (:key-code e))
                    (prevent e)
                    (stop-prop e)
                    (f e el)))))
  els)

(defn keys-down [root & args]
  (let [sel (when (-> args count odd?)
              (first args))
        args (if (-> args count odd?)
               (rest args)
               args)
        pairs (partition 2 args)
        els (if sel ($ root sel) root)]
    (when (> (count els) 0)
      (doseq [[target-key f] pairs]
        (keydown
         els
         (fn [{:keys [key-code] :as e}]
           (when (= key-code target-key)
             (f e root))))))
    root))

(defn scroll-end [els f]
  (doseq [el (ensure-coll els)]
    (let [timer (atom -1)]
      (listen js/window "scroll"
              (fn [e]
                (when (> @timer -1)
                  (util/clear-timeout @timer))
                (reset! timer (util/timeout #(f e) 150))))))
  els)

(defn on-insert [els f]
  (doseq [el (ensure-coll els)]
    (aset el "on-insert" f)))

(defn viewport []
  (let [vp (dom/getViewportSize)]
    [(.-width vp)
     (.-height vp)]))

(defn bounds [el]
  (let [b (style/getBounds el)]
    {:width (.-width b)
     :height (.-height b)
     :left (.-left b)
     :top (.-top b)}))

(defn scroll-to
  [el]
  (.scrollIntoView el true))

(defn scroll-top [& [el]]
  (let [el (or el js/document)]
    (if (or (= js/window el) (= js/document el))
      (or (aget js/window "pageYOffset")
          (aget (.-documentElement js/document) "scrollTop")
          (aget (.-body js/document) "scrollTop"))
      (aget el "scrollTop"))))

(def transition-prop
  (let [styles (.-style (.createElement js/document "a"))
        props ["webkitTransition" "MozTransition" "OTransition" "msTransition"]]
    (or (first (filter #(= "" (aget styles %)) props))
        "Transition")))

(def trans-end-prop
  (condp = transition-prop
    "webkitTransition" "webkitTransitionEnd" ; webkit
    "OTransition" "oTransitionEnd" ; opera
    "transitionend"))

(defn trans*
  [el {:keys [done dur ease]
       :or   {done #() dur "1s" ease "ease"}
       :as   opts}]
  (let [st (dissoc opts :done :dur :ease)
        t  (str "all " (name dur) " " (name ease))]
    (when-not (= t (aget (.-style el) transition-prop))
      (aset (.-style el) transition-prop t))
    (style el st)
    (listen el
            trans-end-prop
            (util/run-once
             (fn []
               #_(aset (aget el "style") transition-prop "")
               (done))))
    el))

(defn trans [el & os]
  (let [os (loop [os os out []]
             (let [fo (first os)
                   so (second os)]
               (if-not so
                 (conj out fo)
                 (recur (rest os)
                        (conj out (assoc fo
                                    :done (fn []
                                            ((or (:done fo) #()))
                                            (trans* el so))))))))]
    (trans* el (first os))
    el))
