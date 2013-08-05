(ns nsfw.affix
  (:require [nsfw.util :as util]
            [nsfw.dom :as $]
            [goog.dom :as gdom]
            [goog.style :as gstyle]))

(defn el [$el offset]
  (doseq [$el $el]
    (util/log $el)
    ($/listen js/window
              :scroll
              (fn [e]
                (let [scroll-height (gdom/getDocumentHeight)
                      scroll-offset (gdom/getDocumentScroll)]
                  (if (>= (.-y scroll-offset) offset)
                    ($/add-class $el "affix")
                    ($/rem-class $el "affix")))))))