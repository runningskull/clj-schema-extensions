(defproject org.clojars.yanatan16/schema-extensions "0.1.1"
  :description "Schema Extensions for Prismatic's schema"
  :url "http://github.com/yanatan16/clj-schema-extensions"
  :license {:name "MIT"
            :url "http://github.com/yanatan16/clj-schema-extensions/blob/master/LICENSE"}

  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :auto-clean false
  :source-paths ["target/generated/src/clj" "src/cljx"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj" "test/cljx"]

  :prep-tasks [["cljx" "once"] "javac" "compile"]

  :dependencies [[org.clojure/clojure "1.6.0"]

                 ;; cljx
                 [prismatic/schema "0.4.1"]

                 ;; clj
                 [clj-time "0.9.0"]

                 ;; cljs
                 [com.andrewmcveigh/cljs-time "0.3.4"]
                 ]

  :aliases {"ctest" ["do" "clean," "cljx" "once," "test," "cljsbuild" "test"]}

  :profiles
    {:dev  {:dependencies [[org.clojure/clojurescript "0.0-2850"]]
            :plugins [[com.keminglabs/cljx "0.6.0"]
                      [lein-cljsbuild "1.0.5"]
                      [com.cemerick/clojurescript.test "0.3.3"]]}
     :repl {:source-paths ["dev/clj"]
            :dependencies [[org.clojure/tools.namespace "0.2.10"]]}}

   :cljsbuild {:test-commands {"unit" ["phantomjs" :runner
                                       "this.literal_js_was_evaluated=true"
                                       "target/js/unit-test.js"]}
               :builds
               {:test {:source-paths ["src/cljx"
                                      "test/cljx"
                                      "target/generated/src/cljs"
                                      "target/generated/test/cljs"]
                       :compiler {:output-to "target/js/unit-test.js"
                                  :optimizations :whitespace
                                  :pretty-print true}}}}

  :cljx {:builds [{:source-paths ["src/cljx"]
                 :output-path "target/generated/src/clj"
                 :rules :clj}

                {:source-paths ["src/cljx"]
                 :output-path "target/generated/src/cljs"
                 :rules :cljs}

                {:source-paths ["test/cljx"]
                 :output-path "target/generated/test/clj"
                 :rules :clj}

                {:source-paths ["test/cljx"]
                 :output-path "target/generated/test/cljs"
                 :rules :cljs}]}

  :repl-options {:welcome (user/welcome)
                 :init-ns user})
