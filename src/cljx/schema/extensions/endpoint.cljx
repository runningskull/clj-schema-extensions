(ns schema.extensions.endpoint
  (:require [clojure.string :as string]
            [schema.core :as s]
            [schema.extensions.util :refer [field-meta field-update]]))


;;; Endpoint (schema elements)
;;; a schema that has endpoints

(defn endpoint
  "A value that must satisfy schema, and can be add."
  [schema & args]
  (field-update schema {:_endpoint (-> (apply hash-map args)
                                       (select-keys [:singular]))}))

(defn endpoint? [schema]
  (-> schema field-meta :_endpoint some?))

(defn endpoint-singular [schema]
  (-> schema field-meta :_endpoint :singular))

(defn get-elem [v id]
  (->> v
       (filter #(= (:id %) id))
        first))

(defn root-path [path]
  (str "/" path))

(defn db-path [path]
  (string/replace path #"/" "."))