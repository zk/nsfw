(in-ns 'user)

(require '[nsfw.reup :as reup])

(def reup
  (reup/setup
    {:start-app-sym 'main/start-app
     :stop-app-sym 'main/stop-app
     :tests-regex #"devguide.*-test"}))

(reup)
