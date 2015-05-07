(ns schema.extensions.ids
  (:require [schema.core :as s]))

(defn add-id
  "Add an id to a raw schema"
  [schema]
  (assoc schema :id s/Str)) ;;TODO make :_id

(defn remove-id
  "Remove an id from a raw schema"
  [schema]
  (dissoc schema :id))
