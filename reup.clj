(in-ns 'user)

(require '[nsfw.reup :as reup])

(def reup
  (reup/setup
    {:tests-regex #"nsfw.*-test"}))

(reup)
