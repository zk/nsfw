# nsfw

A Leiningen template for NSFW.

## Usage

```sh
lein new nsfw <project name>
```

## Run

```sh
cd <project name>
foreman start

# visit http://localhost:5000
```

## Deploy

```sh
chmod u+x ./bin/build

git init
git add .
git commit -am 'initial'

heroku create <deploy-name>
git push heroku master

```

## License

Copyright © 2013 Zachary Kim

Distributed under the Eclipse Public License, the same as Clojure.
