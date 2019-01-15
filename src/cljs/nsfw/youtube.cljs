(ns nsfw.youtube
  (:require [clojure.string :as str]
            [nsfw.util :as nu]
            [nsfw.page :as page]
            [reagent.core :as r]
            [nsfw.components :as nc]
            [dommy.core :as dommy]
            [cljs-http.client :as hc]
            [cljs.core.async :as async
             :refer [<! >! chan close! put! take! timeout]
             :refer-macros [go go-loop]]
            [taoensso.timbre
             :as log
             :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce !tag (atom nil))

(defonce !delayed-inits (atom []))

(defn attach-ready []
  (set! js/onYouTubeIframeAPIReady
    (fn []
      (doseq [f @!delayed-inits]
        (f))
      (reset! !delayed-inits []))))

;; Player API

(defn play-video [p]
  (when p
    (.playVideo p)))

(defn pause-video [p]
  (when p
    (.pauseVideo p)))

(defn stop-video [p]
  (when p
    (.stopVideo p)))

(defn seek-to [p ms & [allow-seek-ahead?]]
  (when p
    (try
      (.seekTo p (/ ms 1000) allow-seek-ahead?)
      (catch js/Error e
        (log/warn "nsfw.youtube: player not initialized")))))

(defn mute [p]
  (when p
    (.mute p)))

(defn unmute [p]
  (when p
    (.unmute p)))

(defn muted? [p]
  (when p
    (.isMuted p)))

(defn get-volume [p]
  (when p
    (.getVolume p)))

(defn set-volume
  ;; n [0 100]
  [p n]
  (when p
    (.setVolume p n)))

(defn set-size [p width height]
  (when p
    (.setSize p width height)))

(defn get-playback-rate [p]
  (when p
    (.getPlaybackRate p)))

(defn set-playback-rate [p n]
  (when p
    (.setPlaybackRate p n)))

(defn get-available-playback-rates [p]
  (when p
    (js->clj (.getAvailablePlaybackRates p))))

;; vid info

(defn get-duration [p]
  (when p
    (* 1000 (.getDuration p))))

(defn get-video-url [p]
  (when p
    (.getVideoUrl p)))

(defn get-video-data [p]
  (when p
    (js->clj (.getVideoData p) :keywordize-keys true)))

(defn get-video-embed-code [p]
  (when p
    (.getVideoEmbedCode p)))

(defn add-listener [p event f]
  (when p
    (.addEventListener p event f)))

(defn rem-listener [p event f]
  (when p
    (.removeEventListener p event f)))

;; playback status

(defn get-video-loaded-fraction [p]
  (when p
    (.getVideoLoadedFraction p)))

(defn get-player-state [p]
  (when p
    (and
      (fn? (.-getPlayerState p))
      (.getPlayerState p))))

(defn round-to-resolution [yt-time]
  (nu/round (* 1000 yt-time)))

(defn get-current-time [p]
  (when p
    (round-to-resolution
     (.getCurrentTime p))))

(defn get-video-start-bytes [p]
  (when p
    (.getVideoStartBytes p)))

(defn get-video-bytes-loaded [p]
  (when p
    (.getVideoBytesLoaded p)))

(defn get-video-bytes-total [p]
  (when p
    (.getVideoBytesTotal p)))

(defn script-loaded? []
  (try
    (= 1 (.-loaded js/YT))
    (catch js/Error e false)))

(defn script-attached? []
  (or (script-loaded?)
      @!tag))

(defn attach-script []
  (let [tag (.createElement js/document "script")
        _ (set! (.-src tag) "https://www.youtube.com/iframe_api")
        first-script-node (.item (.getElementsByTagName js/document "script") 0)
        parent (.-parentNode first-script-node)]
    (.insertBefore parent tag first-script-node)
    (reset! !tag tag)))

(defn run-after-script-load [f]
  (if (script-loaded?)
    (f)
    (swap! !delayed-inits
      conj
      f)))

(defn create-player [node
                     {:keys [video-id
                             width height
                             autoplay?
                             player-vars

                             on-ready
                             on-state-change]
                      :or [width 500
                           height 300]}]
  (js/YT.Player.
   node
   (clj->js
    {:height height
     :width width
     :playerVars player-vars
     :videoId video-id
     :events {:onReady on-ready
              :onStateChange on-state-change}})))

(defn playing? [p]
  (= (get-player-state p) 1))

(defn toggle-play-pause [p]
  (if (playing? p)
    (pause-video p)
    (play-video p)))

(defn toggle-play-stop [p]
  (if (playing? p)
    (stop-video p)
    (play-video p)))

(defn gather-props [player]
  {:playing? (playing? player)
   :time (get-current-time player)
   :duration (get-duration player)})

(defn update-player-opts [p old new]
  (when p
    (when (or (not= (:width old) (:width new))
              (not= (:height old) (:height new)))
      (set-size p (:width new) (:height new)))))

(defn update-playing [p playing-flag?]
  (when p
    (when-not (= (playing? p) playing-flag?)
      (if playing-flag?
        (play-video p)
        (pause-video p)))))

(defn handle-loop [p [low high]]
  (when (and low high)
    (let [ms (get-current-time p)]
      (when (> ms high)
        (seek-to p low))
      (when (< ms low)
        (seek-to p low)))))

(defn $video [{:keys [on-player
                      on-playing
                      on-paused
                      on-ready
                      on-state-change
                      on-props

                      on-time
                      throttle-time

                      loop]
               :or {on-playing (fn [])
                    on-paused (fn [])}
               :as opts}]
  (let [!node (atom nil)
        !player (atom nil)

        !runs (atom {})
        !current-run-id (atom nil)

        !loop (atom loop)

        internal-on-playing
        (fn []
          (let [next-run-id (nu/uuid)
                last-run-id @!current-run-id]
            (swap!
              !runs
              (fn [runs]
                (-> runs
                    (assoc last-run-id nil)
                    (assoc next-run-id true))))

            (reset! !current-run-id next-run-id)

            (go-loop []
              (when (get @!runs next-run-id)
                (when on-time
                  (on-time
                    @!player
                    (get-current-time @!player)))
                (when @!loop
                  (handle-loop @!player @!loop))
                (<! (timeout (or throttle-time 200)))
                (recur))))

          (when on-playing
            (on-playing @!player)))

        internal-on-paused
        (fn []
          (when @!current-run-id
            (swap! !runs assoc @!current-run-id nil))

          (when on-paused
            (on-paused @!player)))

        initialize-video (fn [opts]
                           (reset! !player
                             (create-player
                               @!node
                               (merge
                                 (select-keys
                                   opts
                                   [:width :height :video-id :player-vars])
                                 {:on-ready
                                  (fn [e]
                                    (when on-ready
                                      (on-ready @!player e))
                                    (when on-props
                                      (when-let [props (gather-props @!player)]
                                        (on-props
                                          @!player
                                          (gather-props @!player)))))

                                  :on-state-change
                                  (fn [e]
                                    (let [state (.-data e)]
                                      (condp = state
                                        -1 nil
                                        0 nil
                                        1 (internal-on-playing)
                                        2 (internal-on-paused)
                                        3 nil
                                        5 nil
                                        nil))


                                    (when on-state-change
                                      (on-state-change e))

                                    (when on-props
                                      (when-let [props (gather-props @!player)]
                                        (on-props
                                          @!player
                                          (gather-props @!player)))))})))

                           (when on-player
                             (when @!current-run-id
                               (swap! !runs assoc @!current-run-id nil))
                             (on-player @!player)))]

    (r/create-class
      {:component-did-mount
       (fn [_]
         (when-not (script-attached?)
           (attach-script))
         (attach-ready)
         (run-after-script-load
           (fn []
             (initialize-video opts))))

       :component-will-unmount
       (fn [_]
         (when @!tag
           (try
             (.removeFromParent @!tag)
             (catch js/Error e
               (prn "Error removing yt script from parent")
               nil)))
         (when @!current-run-id
           (swap! !runs assoc @!current-run-id nil)))

       :component-did-update
       (page/cdu-diff
         (fn [[{old-loop :loop :as old-opts}]
              [{new-loop :loop :as new-opts}]]
           (run-after-script-load
             (fn []
               (if (not= (:video-id old-opts)
                         (:video-id new-opts))
                 (initialize-video new-opts)
                 (update-player-opts
                   @!player
                   old-opts
                   new-opts))

               (when (not= old-loop new-loop)
                 (reset! !loop new-loop))))

           #_(run-after-script-load
               (fn []
                 (when (not= (:playing? old-opts) (:playing? new-opts))
                   (update-playing
                     @!player
                     (:playing? new-opts)))))))

       :reagent-render
       (fn [{:keys [video-id width height]}]
         [:div.yt-vid-wrapper
          {:key video-id
           :style {:width width
                   :height height}}
          [:div
           {:ref #(when % (reset! !node %))}]])})))


(defn <videos-by-ids [{:keys [api-key]} video-ids
                      & [{:keys [limit offset]
                          :or {limit 10}}]]
  (go
    (let [res (<! (hc/get
                    "https://www.googleapis.com/youtube/v3/videos"
                    {:query-params {:key api-key
                                    :part "snippet"
                                    :maxResults limit
                                    :id (->> video-ids
                                             (interpose ",")
                                             (apply str))}}))]
      (if (:success res)
        [(:body res)]
        [nil res]))))
