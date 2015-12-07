(ns nsfw.forms
  (:require [clojure.string :as str]))

(defn bound-input [!app path opts]
  [:input
   (merge
     {:on-change (fn [e]
                   (swap! !app assoc-in path (.. e -target -value)))
      :value (get-in @!app path)}
     opts)])

(defn textarea [{:keys [!cursor path opts]}]
  [:textarea
   (merge
     {:on-change (fn [e]
                   (when !cursor
                     (swap! !cursor assoc-in path (.. e -target -value))))
      :value (when !cursor
               (get-in @!cursor path))
      :class "form-control"}
     opts)])

(defn select [{:keys [!cursor path opts]} children]
  (vec
    (concat
      [:select
       (merge
         {:on-change (fn [e]
                       (when !cursor
                         (swap! !cursor assoc-in path (.. e -target -value))))
          :value (if !cursor (get-in @!cursor path))}
         opts)]
      (for [{:keys [text value]} children]
        ^{:key (or value text)}
        [:option {:value (or value text)}
         text]))))

(defn number-char? [n]
  (and (>= n 48)
       (<= n 57)))

(defn input [& [opts]]
  (let [adtl-opt-keys [:!cursor :path :parse-value :format-value]
        {:keys [!cursor path parse-value format-value valid-char-code? type]} opts
        html-opts (apply dissoc opts adtl-opt-keys)
        parse-value (or parse-value identity)
        format-value (or format-value identity)
        valid-char-code? (fn [{:keys [code ctrl? meta?]} value]
                           (if valid-char-code?
                             (or (<= code 47)
                                 (>= code 91)
                                 ctrl?
                                 meta?
                                 (valid-char-code? code value))
                             true))
        checkbox? (when type
                    (= "checkbox" (name type)))
        adtl-opts (if checkbox?
                    {:checked (when !cursor
                                (get-in @!cursor path))}
                    {:value (when !cursor
                              (format-value (get-in @!cursor path)))})

        class (if checkbox?
                {}
                {:class "form-control"})
        adtl-opts (if !cursor
                    (merge
                      adtl-opts
                      {:on-key-down
                       (fn [e]
                         (let [v (.. e -keyCode)]
                           (when-not (valid-char-code?
                                       {:code v
                                        :ctrl? (.-ctrlKey e)
                                        :meta? (.-metaKey e)}
                                       (get-in @!cursor path))
                             (.preventDefault e)
                             (.stopPropagation e))))
                       :on-change
                       (fn [e]
                         (if checkbox?
                           (swap! !cursor
                             assoc-in path
                             (.. e -target -checked))
                           (swap! !cursor
                             assoc-in path
                             (parse-value (.. e -target -value))))
                         (.preventDefault e))})
                    adtl-opts)]
    [:input (merge
              adtl-opts
              class
              html-opts)]))

(defn card-type [s]
  (when s
    (condp #(re-find %1 %2) s
      #"^4[0-9]{6,}$" :visa
      #"^5[1-5][0-9]{5,}$" :mastercard
      #"^3[47][0-9]{5,}$" :amex
      #"^3(?:0[0-5]|[68][0-9])[0-9]{4,}$" :diners
      #"^6(?:011|5[0-9]{2})[0-9]{3,}$" :discover
      #"^(?:2131|1800|35[0-9]{3})[0-9]{3,}$" :jcb
      nil)))

(defn numbers-only [s]
  (when s
    (->> (str/replace s #"[^0-9]" ""))))

#_(def test-cards ["4242424242424242" :visa
                   "401288888888188" :visa
                   "4000056655665556" :visa
                   "5555555555554444" :mastercard
                   "5200828282828210" :mastercard
                   "5105105105105100" :mastercard
                   "378282246310005" :amex
                   "371449635398431" :amex
                   "6011111111111117" :discover
                   "6011000990139424" :discover
                   "30569309025904" :diners
                   "38520000023237" :diners
                   "3530111333300000" :jcb
                   "3566002020360505" :jcb])

#_(doseq [[card type] (partition 2 test-cards)]
    (prn card type (= type (card-type card))))

(defn cc-number [opts]
  (input
    (merge
      {:valid-char-code? number-char?
       :pattern "\\d*"
       :format-value (fn [v]
                       (when v
                         (->> v
                              numbers-only
                              (partition-all 4)
                              (interpose " ")
                              flatten
                              (apply str))))
       :parse-value (fn [s]
                      (when s
                        (->> (str/replace s #"\s+" "")
                             (take 20)
                             (apply str))))}
      opts)))

(defn format-number [s & [{:keys [round]}]]
  (when s
    (let [round (or round 2)
          s (str s)
          parts (str/split s #"\.")
          whole (first parts)
          frac (second parts)
          cleaned-whole (str/replace
                          (str whole)
                          #"[^\d]+"
                          "")
          cleaned-frac (str/replace
                         (str frac)
                         #"[^\d]+"
                         "")
          cleaned-frac (if round
                         (->> cleaned-frac
                              (take round)
                              (apply str))
                         cleaned-frac)]
      (if (and (empty? cleaned-whole) (empty? cleaned-frac))
        nil
        (str
          (->> cleaned-whole
               reverse
               (partition-all 3)
               (interpose [","])
               (apply concat)
               reverse
               (apply str))
          (when-not (empty? cleaned-frac)
            (str "." cleaned-frac)))))))

(defn format-price [s]
  (when s
    (let [formatted (format-number s)]
      (if (empty? formatted)
        nil
        (str "$" formatted)))))

(defn parse-int [s]
  (when s
    (try
      (let [res (js/parseInt
                  (str/replace
                    (str s)
                    #"[^\d]+"
                    ""))]
        (if (js/isNaN res)
          nil
          res))
      (catch js/Error e nil))))
