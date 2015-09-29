(ns nsfw.forms)

(defn bound-input [!app path opts]
  [:input
   (merge
     {:on-change (fn [e]
                   (swap! !app assoc-in path (.. e -target -value)))
      :value (get-in @!app path)}
     opts)])

(defn bound-textarea [!app path opts]
  [:textarea
   (merge
     {:on-change (fn [e]
                   (swap! !app assoc-in path (.. e -target -value)))
      :value (get-in @!app path)}
     opts)])

#_(defn input [& [opts]]
    (let [adtl-opt-keys [:cursor :path :parse-value :format-value]
          {:keys [cursor path parse-value format-value valid-char-code?]} opts
          html-opts (apply dissoc opts adtl-opt-keys)
          parse-value (or parse-value identity)
          format-value (or format-value identity)
          valid-char-code? (or valid-char-code? (constantly true))
          adtl-opts {:value (format-value (get-in cursor path))}
          adtl-opts (if cursor
                      (merge
                        adtl-opts
                        {:on-key-down
                         (fn [e]
                           (let [v (.. e -keyCode)]
                             (when-not (valid-char-code? v)
                               (.preventDefault e)
                               (.stopPropagation e))))
                         :on-change
                         (fn [e]
                           (om/update! cursor path (parse-value (.. e -target -value)))
                           (.preventDefault e))})
                      adtl-opts)]
      [:input.form-control (merge adtl-opts html-opts)]))
