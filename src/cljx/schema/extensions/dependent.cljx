(ns schema.extensions.dependent
  (:require [schema.core :as s]
            [schema.coerce :refer (json-coercion-matcher)]
            #+clj [schema.macros :as macros]
            [schema.utils :as utils])
  #+cljs (:require-macros [schema.macros :as macros]))

(defn- full-schema-map [field base-schema schema-map]
  (reduce-kv #(assoc %1 %2 (merge base-schema %3 {field (s/enum %2)})) {} schema-map))

(defn- map-vals [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defrecord FieldDependent [field converter base-schema schema-map]
  s/Schema
  (walker [this]
    (let [walkers (map-vals s/subschema-walker (full-schema-map field base-schema schema-map))]
      (fn [x]
        (let [v (-> x (get field) converter)
              schema-walker (walkers v)]
          (if-not schema-walker
            (macros/validation-error this x (list v :in (apply vector (keys schema-map))))
            (schema-walker (assoc x field v)))))))
  (explain [this] (list 'field-dependent :on field :of schema-map)))

(defn field-dependent
  ([field type schema-map]
    (field-dependent field type {} schema-map))
  ([field type base-schema schema-map]
    (FieldDependent. field
                     (or (json-coercion-matcher type) identity)
                     base-schema
                     schema-map)))

(defn field-dependent? [s]
  (instance? FieldDependent s))

(defn field-dependent-schemae [s]
  (vals (full-schema-map (:field s) (:base-schema s) (:schema-map s))))