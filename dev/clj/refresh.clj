(ns refresh
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defonce refresh-after-token (atom nil))

(defn refresh-after "(refresh :after f) with arguments" [& args]
  (reset! refresh-after-token args)
  (refresh :after 'refresh/refresh-after-callback))

(defn refresh-after-callback []
  (when (nil? @refresh-after-token) (throw (Exception. "Refresh failed")))
  (let [[fname & args] @refresh-after-token
        f (eval fname)]
    (apply f args)))