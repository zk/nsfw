(ns nsfw.listy
  (:require [nsfw.dom :as dom]))


(def words (map str '(abjure
                      abrogate
                      abstemious
                      acumen
                      antebellum
                      auspicious
                      belie
                      bellicose
                      bowdlerize
                      chicanery
                      chromosome
                      churlish
                      circumlocution
                      circumnavigate
                      deciduous
                      deleterious
                      diffident
                      enervate
                      enfranchise
                      epiphany
                      equinox
                      euro
                      evanescent
                      expurgate
                      facetious
                      fatuous
                      feckless
                      fiduciary
                      filibuster
                      gamete
                      gauche
                      gerrymander
                      hegemony
                      hemoglobin
                      homogeneous
                      hubris
                      hypotenuse
                      impeach
                      incognito
                      incontrovertible
                      inculcate
                      infrastructure
                      interpolate
                      irony
                      jejune
                      kinetic)))

(defn new-item []
  [:input {:type "text"}])

(defn gen-item []
  )

(defn gen-list [& [levels]]
  )

(defn gen-item []
  (str
   (nth words (rand (count words)))
   " "
   (nth words (rand (count words)))))

(defn gen-list [& [levels]]
  (concat
   (repeatedly (rand 5) gen-item)
   (when (> levels 0)
     [(gen-list (dec levels))])
   (repeatedly (rand 5) gen-item)))

(defn render-item [item]
  [:li [:div.item {:draggable "true"} item]])

(defn render-list [list]
  [:ul.list
   (map #(if (coll? %)
           (render-list %)
           (render-item %))
        list)])

(defn main []
  (-> dom/body
      (dom/apd [:header
                [:a.title {:href "#"}
                 "Listy"]])
      (dom/apd (render-list (gen-list 2)))))