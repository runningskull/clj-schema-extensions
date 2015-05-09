(ns schema.extensions.derived
  (:require [schema.core :refer [subschema-walker explain start-walker walker] :as s]
            [schema.coerce :as coerce]
            #+clj [schema.macros :as sm]
            [schema.extensions.util :refer [field-update field-meta]])
  #+cljs (:require-macros [schema.macros :as sm]))


;;; Derived
;;; schema elements derived on other schema elements at the same level

(defn derived
  "Calculate a field's value based on its parent object. Parameters are parent object and key name."
  [schema calculator]
  (field-update schema {:_derived {:calculator calculator}}))

(defn defaulting
  "Allow a field to be a default. Is not applied if a value exists. Takes a function or a value."
  [schema default]
  (-> schema
      (derived (fn [a k] (cond (a k)         (a k)
                               (fn? default) (default)
                               :otherwise    default)))))

(defn optional [schema]
  (-> schema
      s/maybe
      (defaulting nil)))

;; Helpers

(defn derived? [schema]
  (-> schema field-meta :_derived some?))

(defn- derived-calculator [schema]
  (-> schema field-meta :_derived :calculator))

(defn derive-coercion [schema]
  (let [kvs (and (map? schema)
                 (filter (fn [[k v]] (derived? v)) schema))]
    (when (and kvs (not (empty? kvs)))
          (fn [data]
            (if-not (map? data) data
              (->> kvs
                   (map
                    (fn [[k ds]]
                      (try [k ((derived-calculator ds) data k)]
                        (catch #+clj Exception #+cljs js/Error e
                          [k (sm/validation-error ds data 'deriving-exception e)]))))
                   (apply concat)
                   (apply assoc data)))))))

(defn- deriver [schema]
  (start-walker
   (fn [s]
     ; (println "deriver" s)
     (let [walk (walker s)
           dc (or (derive-coercion s) identity)
           c (or (coerce/json-coercion-matcher s) identity)]
       (fn [x]
          (-> x dc c walk))))
   schema))

;; Deriving walker

(defn derive-schema [schema data]
  "Walk a schema, deriving any keys that are of the Derived type"
  ((deriver schema) data))