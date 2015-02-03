(ns nsfw.env-test
  (:require [nsfw.env :as env] :reload)
  (:require [clojure.test :refer :all]))

(deftest test-env
  (binding [env/*env* {"FOO" "BAR"
                       "SNAKE_CASE" "HI!"
                       "SOME_INT" "12"

                       "TRUE" "true"
                       "FALSE" "false"
                       "TRUE_UPPER" "TRUE"
                       "FALSE_UPPER" "FALSE"}]


    (is (= (env/env :foo) "BAR"))

    (is (= (env/env :snake-case) "HI!"))

    (is (= (env/int :some-int) 12))

    (is (env/bool :true))
    (is (not (env/bool :false)))
    (is (env/bool :true-upper))
    (is (not (env/bool :false-upper)))

    ;; not found
    (is (nil? (env/env :not-found)))))
