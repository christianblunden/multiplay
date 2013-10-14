# Multi play

## A Skeleton Multiplayer project

This project was first put together for the October 2013 uSwitch London Clojure Dojo.

To get started:

``` bash
lein run
```

or play in the repl

``` bash
lein repl
```

``` clojure
(user/go!)    ;start server
(user/reset)  ;reset server
```

## Build your own game

The bulk of the game play has been left blank so that you can build your own game.  The essential files that you need to "fill in the blanks" include:

``` clojure
  (ns multiplay.game.core)  ;; clj
  (ns multiplay.views.arena) ;; cljs
```

## Acknowledgements

The skeleton code was graciously provided by 3rd place Clojure Cup 2013 team:

[Torus Pong](https://github.com/uswitch/torus-pong)

## License

Copyright © 2013 Christian Blunden

Distributed under the Eclipse Public License, the same as Clojure.
