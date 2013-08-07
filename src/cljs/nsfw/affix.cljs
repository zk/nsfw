(ns nsfw.affix
  (:require [nsfw.util :as util]
            [nsfw.dom :as $]
            [goog.dom :as gdom]
            [goog.style :as gstyle]))

(defn el [$el offset]
  (doseq [$el ($/ensure-coll $el)]
    ($/listen js/window
              :scroll
              (fn [e]
                (let [scroll-height (gdom/getDocumentHeight)
                      scroll-offset (gdom/getDocumentScroll)]
                  (if (>= (.-y scroll-offset) offset)
                    ($/add-class $el "affix")
                    ($/rem-class $el "affix")))))))

(defn init []
  (doseq [$el ($/query "[data-spy='affix']")]
    (let [offset (try
                   (js/parseInt ($/attr $el :data-offset-top))
                   (catch Exception e
                     200))]
      (el $el offset))))