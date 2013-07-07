# nsfw

Proving ground for library code.

[![Build Status](https://travis-ci.org/zkim/nsfw.png)](https://travis-ci.org/zkim/nsfw)

## Usage

`lein new nsfw <project name>`

If you get:

```bash
Retrieving nsfw/lein-template/maven-metadata.xml (1k)
    from http://clojars.org/repo/
Could not find metadata nsfw:lein-template/maven-metadata.xml in central (http://repo1.maven.org/maven2)
```

Just `rm -rf ./<project dir>` and try again.


## Dev

### Bumping Versions

* Try to keep the version `$REPO/project.clj`, `$REPO/lein-template/project.clj`, and the dep in
`$REPO/lein-template/src/leiningen/new/nsfw/project.clj` in sync.
* Run `bin/ship`


## License

Copyright (C) 2010-2012 Zachary Kim

Distributed under the Eclipse Public License, the same as Clojure.
