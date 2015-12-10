(ns nsfw.dropzone
  (:require [com.dropzonejs]
            [reagent.core :as rea]))

(defn $dropzone [{:keys [url
                         message
                         on-success
                         on-error
                         on-uploadprogress
                         allow-multiple?]}]
  (let [!state (rea/atom {:message (or message "Drop Files Here")
                          :loading? false})]
    [(with-meta
       (fn []
         (let [{:keys [loading? message]} @!state]
           [:div.dropzone-wrapper
            (if loading?
              "Uploading File..."
              message)]))
       {:component-did-mount
        (fn [this]
          (let [dz (js/Dropzone.
                     (rea/dom-node this)
                     #js {:url url})]
            (.on dz "sending"
              (fn [& _]
                (swap! !state assoc :loading? true)))
            (.on dz "success"
              (fn [file response-str]
                (swap! !state assoc :loading? false)
                (when on-success
                  (on-success {:file file
                               :response-str response-str}))))
            (.on dz "error"
              (fn [file response-str]
                (swap! !state
                  assoc
                  :loading? false
                  :message "There was an error uploading your file.")
                (when on-error
                  (on-error file response-str))))
            (.on dz "uploadprogress"
              (fn [file pct bytes]
                (when on-uploadprogress
                  (on-uploadprogress
                    {:file file :pct (/ pct 100) :bytes bytes}))))
            (when-not allow-multiple?
              (.on dz "addedfile"
                (fn []
                  (this-as this
                    (try
                      (when (> (count (.-files this)) 1)
                        (.removeFile this (first (.-files this))))
                      (catch js/Error e nil))))))))})]))
