(ns schema.extensions.dependent
  (:require [schema.core :as s]
            [schema.coerce :refer (json-coercion-matcher)]
            #+clj [schema.macros :as macros]
            [schema.utils :as utils])
  #+cljs (:require-macros [schema.macros :as macros]))

(defn- map-vals [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defrecord FieldDependent [field converter schema-map]
  s/Schema
  (walker [this]
    (let [walkers (map-vals s/subschema-walker schema-map)]
      (fn [x]
        (let [v (-> x (get field) converter)
              schema-walker (walkers v)]
          (if-not schema-walker
            (macros/validation-error this x (list v :in (apply vector (keys schema-map))))
            (schema-walker (assoc x field v)))))))
  (explain [this] (list 'field-dependent :on field :of schema-map)))

(defn field-dependent
  ([field type {:as value-schema-pairs}]
    (field-dependent field type {} value-schema-pairs))
  ([field type unchanging-fields {:as value-schema-pairs}]
    (->> value-schema-pairs
         (map (fn [[v s]] [v (merge unchanging-fields s {field (s/enum v)})]))
         (apply concat)
         (apply hash-map)
         (FieldDependent. field (or (json-coercion-matcher type) identity)))))

(defn field-dependent? [s]
  (instance? FieldDependent s))

(defn field-dependent-schemae [s]
  (vals (:schema-map s)))