(ns schema.extensions.partial-validator
  (:require [schema.core :refer [start-walker walker]]
            [schema.utils :as sutils]
            [schema.coerce :as coerce]
            #+clj [schema.macros :as sm]
            [schema.extensions.util :refer [field-meta]])
  #+cljs (:require-macros [schema.macros :as sm]))

(defn not-modifiable? [s]
  (-> s field-meta :modifiable false?))

(defn- make-partial-walker [f]
  (fn [schema]
    (start-walker
      (fn [s]
        (let [walk (walker s)
              c (or (coerce/json-coercion-matcher s) identity)]
          (fn [x]
            (when (not= x :schema.core/missing)
                  (->> x c (f s walk))))))
      schema)))

;; Basic partial validator

(def partial-walker (make-partial-walker (fn [s walk x] (walk x))))

(defn partial-check [schema data]
  (-> schema
      partial-walker
      (#(% data))
      sutils/error-val))

(defn partial-validate
  "Throw an exception if value does not partially satisfy schema; otherwise, return value."
  [schema value]
  (when-let [error (partial-check schema value)]
    (sm/error! (sutils/format* "Value does not match schema: %s" (pr-str error))
                   {:schema schema :value value :error error}))
  value)

;; Not Modifiable check partial validator

(def not-modified-partial-walker
  (make-partial-walker
    (fn [s walk x]
      (if (not-modifiable? s)
          (sm/validation-error s x 'modifiable)
          (walk x)))))

(defn not-modified-partial-check [schema data]
  (-> schema
      not-modified-partial-walker
      (#(% data))
      sutils/error-val))

(defn not-modified-partial-validate
  "Throw an exception if value does not satisfy schema; otherwise, return value."
  [schema value]
  (when-let [error (not-modified-partial-check schema value)]
    (sm/error! (sutils/format* "Value does not match schema: %s" (pr-str error))
                   {:schema schema :value value :error error}))
  value)