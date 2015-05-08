(ns schema.extensions.extensions-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs cemerick.cljs.test
            [schema.core :as s]
            [schema.extensions.ids :as ids]
            [schema.extensions.derived :as derived]
            [schema.extensions.endpoint :as endpoint]
            [schema.extensions.unix-timestamp :as unix-timestamp]
            [schema.extensions.unit-interval :as unit-interval]
            [schema.extensions.util :as sext-util]
            [schema.extensions.field :as sext-field]
            [#+clj clj-time.core #+cljs cljs-time.core :as t]
            [#+clj clj-time.coerce #+cljs cljs-time.coerce :as c])
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest testing)]))

(deftest ids-test
  (let [schema (ids/add-id {:a s/Str})]
    (testing "add-id"
      (is (nil? (s/check schema {:id "123" :a "abc"})))
      (is (some? (s/check schema {:a "abc"}))))
    (testing "remove-id"
      (is (nil? (s/check (ids/remove-id schema) {:a "abc"})))
      (is (some? (s/check (ids/remove-id schema) {:id "123" :a "abc"}))))))

(deftest derived-test
  (let [schema {:a s/Str
                :b (derived/derived s/Str (fn [{a :a} b] (str (subs a 0 3) "-cola-" (name b))))
                :c (derived/derived s/Int (fn [{a :a} _] (count a)))}
        pre-data {:a "coca"}
        post-data {:a "coca" :b "coc-cola-b" :c 4}]
    (testing "derived?"
      (is (derived/derived? (:b schema))))
    (testing "derive-coercion helper"
      (is (nil? (derived/derive-coercion s/Int)))
      (is (nil? (derived/derive-coercion {:a s/Int})))
      (is (= post-data ((derived/derive-coercion schema) pre-data))))
    (testing "validation before derivation"
      (is (some? (s/check schema pre-data))))
    (testing "derivation"
      (is (= post-data (derived/derive-schema schema pre-data))))
    (testing "validation after derivation"
      (is (nil? (s/check schema post-data))))
    (testing "throw in derivation validation"
      (is (not (nil? (s/check schema {:a {:x "map"}})))))))

(deftest defaulting-test
  (let [schema {:a (derived/defaulting s/Int 10)
                :b (derived/defaulting s/Str (fn [] "lola"))}
        pre-data-1 {}
        post-data-1 {:a 10 :b "lola"}
        pre-data-2 {:a 5 :b "fira"}
        post-data-2 {:a 5 :b "fira"}]
    (testing "derived?"
      (is (derived/derived? (:b schema))))
    (testing "validation before derivation"
      (is (some? (s/check schema pre-data-1))))
    (testing "derivation"
      (is (= post-data-1 (derived/derive-schema schema pre-data-1))))
    (testing "validation after derivation"
      (is (nil? (s/check schema post-data-1))))
    (testing "defaults dont write over values"
      (is (= post-data-2 (derived/derive-schema schema pre-data-2))))))

(deftest optional-test
  (let [schema {:a (derived/optional {:b s/Int})}]
    (testing "valid empty object after derivation"
      (is (= (derived/derive-schema schema {}) {:a nil}))
      (is (nil? (s/check schema (derived/derive-schema schema {})))))
    (testing "valid full object"
      (is (nil? (s/check schema {:a {:b 2}}))))
    (testing "derivation doesnt change full object"
      (is (= (derived/derive-schema schema {:a {:b 2}}) {:a {:b 2}})))))

(deftest unix-timestamp-test
  (testing "valid timestamp"
    (is (nil? (s/check unix-timestamp/UnixTimestamp 1428945331))))
  (testing "creating a valid timestamp"
    (is (nil? (s/check unix-timestamp/UnixTimestamp (unix-timestamp/date-time->unix (t/now))))))
  (testing "invalid timestamp"
    (is (some? (s/check unix-timestamp/UnixTimestamp 111))))
  (testing "not even an integer"
    (is (some? (s/check unix-timestamp/UnixTimestamp "abc")))))

(deftest ms-timestamp-test
  (testing "valid timestamp"
    (is (nil? (s/check unix-timestamp/MsTimestamp 1428945331000))))
  (testing "creating a valid timestamp"
    (is (nil? (s/check unix-timestamp/MsTimestamp (unix-timestamp/date-time->ms (t/now))))))
  (testing "invalid timestamp"
    (is (some? (s/check unix-timestamp/MsTimestamp 111))))
  (testing "not even an integer"
    (is (some? (s/check unix-timestamp/MsTimestamp "abc")))))

(deftest unit-interval-test
  (testing "0 is valid in the unit interval"
    (is (nil? (s/check unit-interval/UnitIntervalNum 0))))
  (testing "1 is valid in the unit interval"
    (is (nil? (s/check unit-interval/UnitIntervalNum 1))))
  (testing "0.5 is valid in the unit interval"
    (is (nil? (s/check unit-interval/UnitIntervalNum 0.5))))
  (testing "-1 is invalid in the unit interval"
    (is (some? (s/check unit-interval/UnitIntervalNum -1))))
  (testing "1000 is invalid in the unit interval"
    (is (some? (s/check unit-interval/UnitIntervalNum 1000)))))

(deftest field-update-test
  (testing "field update works twice"
    (is (= {:a 1 :b 2}
           (-> s/Int
               (sext-util/field-update {:a 1})
               (sext-util/field-update {:b 2})
               sext-util/field-meta)))))

(deftest field-test
  (testing ":derived = (derived)"
    (let [schema (sext-field/field s/Int :derived (fn [a b] 2))]
      (is (derived/derived? schema))
      (is (= {:a 2} (derived/derive-schema {:a schema} {})))))
  (testing ":default = (defaulting)"
    (let [schema (sext-field/field s/Int :default 2)]
      (is (derived/derived? schema))
      (is (= {:a 2} (derived/derive-schema {:a schema} {})))))
  (testing ":optional = (optional)"
    (let [schema (sext-field/field s/Int :optional true)]
      (is (derived/derived? schema))
      (is (= {:a nil} (derived/derive-schema {:a schema} {})))))
  (testing ":endpoint = (endpoint)"
    (let [schema (sext-field/field {s/Keyword s/Int} :endpoint true :singular "foo")]
      (is (endpoint/endpoint? schema))
      (is (= "foo" (endpoint/endpoint-singular schema)))))
  (testing "triggers on false"
    (let [schema {:a (sext-field/field  s/Bool :default false)}]
      (is (derived/derived? (:a schema)))
      (is (= {:a false} (derived/derive-schema schema {})))
      (is (= {:a true} (derived/derive-schema schema {:a true})))))
  (testing "describe"
    (is (= (meta (sext-field/field s/Int :description "foobar" :yomama "yoyos"))
           (meta (sext-field/describe s/Int "foobar" :yomama "yoyos"))))))