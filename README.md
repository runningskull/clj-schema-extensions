# schema extensions

Schema Extensions on [schema](https://github.com/Prismatic/schema).

## Using

This repo uses `cljx` and is a bit different from straight `clj` repos.

- You'll need `phantomjs` to run `cljs` tests, `brew install phantomjs`
- `lein cleantest` is a custom alias to run all the tests in both `clj` and `cljs`
- `lein repl` should work as before, but `(refresh)` won't reload new code unless you have `lein cljx auto` running in the background.

## License

MIT license in LICENSE file.