(ns schema.extensions.unit-interval
  (:require [schema.extensions.field :refer [describe]]
            [schema.core :as s]))

(defn- in-unit-interval? [x]
  (<= 0 x 1))

(def UnitIntervalNum
  (describe (s/both s/Num
                    (s/pred in-unit-interval? 'in-unit-interval?))
            "A float between 0 and 1, inclusive"))