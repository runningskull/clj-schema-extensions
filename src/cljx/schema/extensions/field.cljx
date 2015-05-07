(ns schema.extensions.field
  (:require [schema.core :as s]
            [schema.extensions.endpoint :refer [endpoint]]
            [schema.extensions.derived :refer [derived defaulting optional]]
            [schema.extensions.util :refer [field-update]]
            #+clj [schema.extensions.util-macros :as um])
  #+cljs (:require-macros [schema.extensions.util-macros :as um]))

(def field-meta schema.extensions.util/field-meta)

;; Some things reproduced from ring.swagger.schema

(defn field
  "Attaches meta-data to a schema under :json-schema key. If the
   schema is of type which cannot have meta-data (e.g. Java Classes)
   schema is wrapped first into s/both Schema."
  [schema & args]
  (let [mapargs (apply hash-map args)]
    (-> schema
        (field-update mapargs)
        ((um/if-then (:derived mapargs) (derived (:derived mapargs))))
        ((um/if-then (:default mapargs) (defaulting (:default mapargs))))
        ((um/if-then (:optional mapargs) optional))
        ((um/if-then (:endpoint mapargs) (endpoint :singular (:singular mapargs)))))))

(defn describe
  "Attach description and possibly other meta-data to a schema."
  [schema desc & args]
  (apply field schema :description desc args))