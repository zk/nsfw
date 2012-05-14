# nsfw

Various web (and non-web) helpers and utilities.

# Mongo

Connection helper:

    (parse-mongo-url "mongodb://foo:bar@localhost:27017/mydb")

    ;; => {:username "foo"
    ;;     :password "bar"
    ;;     :host     "localhost"
    ;;     :port     27107
    ;;     :db       "mydb"}         


## License

Copyright (C) 2010-2012 Zachary Kim

Distributed under the Eclipse Public License, the same as Clojure.
