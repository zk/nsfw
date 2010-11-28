(ns nsfw.test.gen
  (:use [nsfw.gen] :reload)
  (:use [clojure.test]))

(deftest test-resolve-file
  (is (= "./hello/world" (resolve-file "hello" "world")))
  (is (= "./hello/world" (resolve-file :hello :world))))

(deftest test-resolve-js
  (is (= "./resources/public/js/hello.js" (resolve-js "hello")))
  (is (= "./resources/public/js/hello.js" (resolve-js :hello)))
  (is (= "./resources/public/js/hello/world.min.js" (resolve-js :hello :world.min))))

(deftest test-read-project-name
  (is (= "nsfw" (read-project-name))))

(deftest test-underscore-project-name
  (is (= "my_proj" (underscore-project-name "my-proj"))))

(deftest test-resolve-project-src-file
  (is (= "./src/nsfw/hello.clj" (resolve-project-src-file "hello.clj")))
  (is (= "./src/nsfw/hello.clj" (resolve-project-src-file :hello.clj))))