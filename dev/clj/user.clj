(ns user
  (:require [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.string :refer [join]]
            [refresh :refer [refresh-after]])
  (:use     clojure.repl))

(defn welcome []
  (-> []
    (conj "Hey there, welcome to raflschema's repl:")
    (conj "(run-test 'namespace) run a test")
    (conj "(run-test) run *all* the tests")
    (conj "(run-test #\"^schema\\.extensions.*test$\") run a subset of all tests")
    (conj "(welcome) to see this message again")
    (conj "")
    (#(join "\n" %))
    println))

(defn last-err [& [n]]
  (print-stack-trace *e (or n 10)))

(def re-all #"^schema\.extensions.*test$")
(defn run-test [& [arg]]
  (cond (symbol? arg) (refresh-after 'clojure.test/run-tests arg)
        (nil? arg)    (refresh-after 'clojure.test/run-all-tests re-all)
        :otherwise    (refresh-after 'clojure.test/run-all-tests arg)))