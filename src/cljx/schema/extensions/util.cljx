(ns schema.extensions.util
  (:require [schema.core :as s]))

(def PosInt (s/both s/Int (s/pred #(> % 0) 'greater-than-zero?)))

(def IPAddress #"\d{1,3}.\d{1,3}.\d{1,3}.\d{1,3}")

(def Email #"[^@]+@[^.]+\..+")

#+clj (defn meta-able? [x] (instance? clojure.lang.IObj x))
#+cljs (defn meta-able? [x] (cond (= x s/Bool) false
                                  :otherwise (try (with-meta x (meta x))
                                                  true
                                                  (catch js/Error e false))))

(defn field-update
  [schema meta-data]
  (with-meta (if (meta-able? schema)
               schema
               (s/both schema))
             (let [m (meta schema)]
              (merge m {:json-schema (merge meta-data (:json-schema m))}))))

(defn field-meta [f] (-> f meta :json-schema))

