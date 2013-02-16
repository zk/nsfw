(ns nsfw.components
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.util :as u]))

(defn image-bleed-box
  [opts content]
  (dom/$
   [:div.bleed-box
    [:div.full-bleed {:style {:background-image (format "url(%s)" (:img opts))}}]
    [:div.bleed-box-content content]]))

;; Formats

(defn mp4 [{:keys [mp4]}]
  (when mp4
    [:source {:src mp4 :type "video/mp4; codecs=\"avc1.4D401E, mp4a.40.2\""}]))

(defn webm [{:keys [webm]}]
  (when webm
    [:source {:src webm :type "video/webm; codecs=\"vp8.0, vorbis\""}]))

(defn ogv [{:keys [ogv]}]
  (when ogv
    [:source {:src ogv :type "video/ogg; codecs=\"theora, vorbis\""}]))

(defn video [opts]
  (dom/$
   [:video.full-bleed {:loop "loop"}
    (map (fn [f] (f opts)) [mp4 webm ogv])]))

(defn video-bleed-box [{:keys [poster delay] :as opts} content]
  (let [$video (video opts)
        $el (dom/$ [:div.bleed-box
                    $video
                    [:div.bleed-box-content content]])]
    (when poster
      (dom/attrs $video {:poster poster}))
    (if delay
      (u/timeout #(.play $video) delay)
      (dom/attrs $video {:autoplay "autoplay"}))
    $el))

(defn bleed-box [opts & content]
  (if (:img opts)
    (image-bleed-box opts content)
    (video-bleed-box opts content)))

;;