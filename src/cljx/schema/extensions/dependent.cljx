(ns schema.extensions.dependent
  (:require [schema.core :as s]
            #+clj [schema.macros :as macros]
            [schema.utils :as utils])
  #+cljs (:require-macros [schema.macros :as macros]))

(defn- map-vals [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defrecord FieldDependent [field schema-map]
  s/Schema
  (walker [this]
    (let [walkers (map-vals s/subschema-walker schema-map)]
      (fn [x]
        (let [schema-walker (-> x (get field) walkers)]
          (if-not schema-walker
            (macros/validation-error this x (list (keys schema-map) (utils/value-name x)))
            (schema-walker x))))))
  (explain [this] (list 'field-dependent :on field :of schema-map)))

(defn field-dependent
  ([field {:as value-schema-pairs}]
    (field-dependent field {} value-schema-pairs))
  ([field unchanging-fields {:as value-schema-pairs}]
    (->> value-schema-pairs
         (map (fn [[v s]] [v (merge unchanging-fields s {field (s/enum v)})]))
         (apply concat)
         (apply hash-map)
         (FieldDependent. field))))

(defn field-dependent? [s]
  (instance? FieldDependent s))

(defn field-dependent-schemae [s]
  (vals (:schema-map s)))