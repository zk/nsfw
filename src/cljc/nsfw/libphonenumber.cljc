(ns nsfw.libphonenumber
  #? (:cljs
      (:require [i18n.phonenumbers.PhoneNumberUtil :as pnu]
                [i18n.phonenumbers.PhoneNumberFormat :as pnf]))
  #? (:clj
      (:import [com.google.i18n.phonenumbers
                PhoneNumberUtil
                PhoneNumberUtil$PhoneNumberFormat])))

(defn get-instance []
  #?(:cljs (pnu/getInstance)
     :clj (PhoneNumberUtil/getInstance)))


(def E164
  #?(:clj PhoneNumberUtil$PhoneNumberFormat/E164
     :cljs pnf/E164))

(def INTERNATIONAL
  #?(:clj PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
     :cljs pnf/INTERNATIONAL))

(def NATIONAL
  #?(:clj PhoneNumberUtil$PhoneNumberFormat/NATIONAL
     :cljs pnf/NATIONAL))

(def RFC3966
  #?(:clj PhoneNumberUtil$PhoneNumberFormat/RFC3966
     :cljs pnf/RFC3966))

(defn valid? [number-str country]
  (let [pu (get-instance)
        parsed (.parse pu number-str country)]
    (.isValidNumber pu parsed)))

(defn format-e164 [number-str country]
  (let [pu (get-instance)
        parsed (.parse pu number-str country)]
    (.format pu parsed E164)))

(defn format-international [number-str country]
  (let [pu (get-instance)
        parsed (.parse pu number-str country)]
    (.format pu parsed INTERNATIONAL)))

(defn format-national [number-str country]
  (let [pu (get-instance)
        parsed (.parse pu number-str country)]
    (.format pu parsed NATIONAL)))

(defn format-rfc3966 [number-str country]
  (let [pu (get-instance)
        parsed (.parse pu number-str country)]
    (.format pu parsed RFC3966)))
