(ns nsfw.bootstrap
  (:require [nsfw.util :as util]
            [nsfw.dom :as $]
            [goog.dom :as gdom]
            [goog.style :as gstyle]
            [goog.fx.dom :as fx-dom]
            [goog.fx.easing :as easing]))

(defn affix-el [$el offset]
  (doseq [$el ($/ensure-coll $el)]
    ($/listen js/document
              :scroll
              (fn [e]
                (let [scroll-height (gdom/getDocumentHeight)
                      scroll-offset (gdom/getDocumentScroll)]
                  (if (>= (.-y scroll-offset) offset)
                    ($/add-class $el "affix")
                    ($/rem-class $el "affix")))))))

(defn affix-init []
  (doseq [$el ($/query "[data-spy='affix']")]
    (let [offset (try
                   (js/parseInt ($/attr $el :data-offset-top))
                   (catch js/Exception e
                     10))]
      (affix-el $el offset))))


(defn scroll-to-init []
  (doseq [$el ($/query :.scroll-to)]
    (let [target ($/attr $el :href)
          target ($/query target)]
      ($/click $el (fn [e]
                     (when-not (first target)
                       (throw (str "nsfw.scroll-to: Couldn't find scroll target " target)))
                     (let [y-pos (- (.-y (gstyle/getPageOffset (first target))) 20)
                           y-doc (.-y (gdom/getDocumentScroll))]
                       (.play (fx-dom/Scroll. (first ($/query :body))
                                              (clj->js [0, y-doc])
                                              (clj->js [0, y-pos])
                                              200
                                              easing/easeOut))
                       ($/prevent e)))))))



(defn init []
  (affix-init)
  (scroll-to-init))
