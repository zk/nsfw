(ns nsfw.components
  (:use [nsfw.util :only [log]])
  (:require [nsfw.dom :as dom]
            [nsfw.util :as u]))

(defn image-bleed-box [opts content]
  (dom/$ [:div.bleed-box
          [:div.full-bleed {:style (format "background-image: url(%s);" (:img opts))}]
          [:div.bleed-box-content content]]))

(defn video-bleed-box [{:keys [poster mp4 webm ogv delay]} content]
  (let [video (dom/$ [:video.full-bleed {:loop "loop"}
                      (when mp4
                        [:source {:src mp4
                                  :type "video/mp4; codecs=\"avc1.4D401E, mp4a.40.2\""}])
                      (when webm
                        [:source {:src webm
                                  :type "video/webm; codecs=\"vp8.0, vorbis\""}])

                      (when ogv
                        [:source {:src ogv
                                  :type "video/ogg; codecs=\"theora, vorbis\""}])])
        el (dom/$ [:div.bleed-box
                   video
                   [:div.bleed-box-content content]])]
    (when poster
      (dom/attrs video {:poster poster}))
    (if delay
      (do
        (u/timeout #(do (log "load called")
                        (.play video)) delay))
      (dom/attrs video {:autoplay "autoplay"}))
    el))

(defn bleed-box [opts & content]
  (if (:img opts)
    (image-bleed-box opts content)
    (video-bleed-box opts content)))