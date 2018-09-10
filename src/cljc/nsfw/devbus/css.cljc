(ns nsfw.devbus.css
  (:require [nsfw.css :as ncss]))

(def spec {:sizes {:xs 5 :sm 10 :md 20 :lg 50 :xl 100}
           :fonts {:header "'Helvetica Neue', Arial, sans-serif"
                   :copy "'Helvetica Neue', Arial, sans-serif"
                   :impact "'Helvetica Neue', Arial, sans-serif"
                   :monospace "'Source Code Pro', monospace"}})

(ncss/inject-css-defs spec)

(def rules
  (vec
    (concat
      (ncss/gen-all-rules spec)
      [])))

(def rules-string
  (ncss/compile-to-string rules))
