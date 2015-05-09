(ns schema.extensions.schema-walker
  (:require [clojure.walk :as walk]
            [clojure.string :as string]
            [schema.core :as schema
              #+cljs :refer #+cljs [AnythingSchema EnumSchema Predicate Maybe NamedSchema Both ConditionalSchema Either]]
            [schema.extensions.endpoint :refer [endpoint? endpoint-singular]]
            [schema.extensions.dependent :refer [field-dependent? field-dependent-schemae]])
  #+clj (:import [schema.core AnythingSchema EnumSchema Predicate Maybe NamedSchema Both ConditionalSchema Either]))

(defn- field? [s]
  (and (vector? s)
       (= 2 (count s))))

(defn- field-path [s]
  (first s))

(defn- any-instance? [klasses instance]
  (some #(instance? % instance) klasses))

(defn- extract-schemae [s]
  (cond (any-instance? [AnythingSchema
                        Predicate
                        EnumSchema] s) []
        (any-instance? [Maybe
                        NamedSchema
                        Both
                        ConditionalSchema] s) (extract-schemae (:schema s))
        (any-instance? [Either] s) (apply concat (map extract-schemae (:schemas s)))
        (field-dependent? s) (apply concat (map extract-schemae (field-dependent-schemae s)))
        (map? s) [s]
        :otherwise []))

(defn- field-schema [s]
  (second s))

(defn- field-key [k]
  (cond (= k schema/Keyword) ":id"
        :otherwise (name (schema/explicit-schema-key k))))

(defn- addressable? [s]
  (try (field-key (first s))
       (catch #+clj Exception #+cljs js/Error e false)))

(defn- set-prefix-path [prefix m]
  (map (fn [[k v]] [(str prefix (field-key k)) v])
    (filter addressable? m)))

(defn walk-schema [f schema & {:keys [sep] :or {sep "."}}]
  (walk/prewalk
    (fn [s]
      (when (field? s) (f (field-path s) (field-schema s)))
      (cond (and (field? s) (extract-schemae (field-schema s)))
              (apply list (map #(set-prefix-path (str (field-path s) (if (empty? (field-path s)) "" sep)) %) (extract-schemae (field-schema s))))
            (seq? s) s))
    ["" schema]))

(defn- get-endpoint-schema [eschema]
  (-> eschema first))

(defn- get-endpoint-id-schema [eschema]
  (let [es (-> eschema first)]
    (cond (field-dependent? es) (-> es :base-schema :id)
          (map? es) (:id es)
          :otherwise schema/Str)))

(defn- get-endpoint-keywords [prefix sep]
  (->> sep
       re-pattern
       (string/split prefix)
       (filter #(not (empty? %)))
       (map keyword)
       (apply vector)))

(defn make-endpoint-getter [keywords]
  (fn ([obj] (get-in obj keywords))
      ([obj id] (get-in obj (conj keywords id)))))

(defn- walk-endpoints [f schema]
  (walk-schema #(when (endpoint? %2)
                      (let [keywords (get-endpoint-keywords %1 "/")]
                        (f {:path %1
                            :singular (endpoint-singular %2)
                            :schema (get-endpoint-schema %2)
                            :keywords keywords
                            :id-schema (get-endpoint-id-schema %2)})))
               schema
               :sep "/"))

(defn all-endpoints [schema]
  (let [output (atom [])]
    (walk-endpoints #(swap! output conj %) schema)
    @output))