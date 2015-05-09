(ns schema.extensions.dependent-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :refer-macros (is deftest testing)]
            [schema.core :as s #+cljs :refer #+cljs (EnumSchema)]
            [schema.extensions.dependent :refer (field-dependent)]
            [schema.extensions.schema-walker :as walker])
  #+clj (:import [schema.core EnumSchema]))

(deftest field-dependent-test
  (let [schema (field-dependent :type s/Keyword {:foo s/Str :bar s/Int}
                 {:t1 {:arg1 s/Str}
                  :t2 {:arg2 s/Str}
                  :t3 {:arg1 s/Str :barg s/Int}})
        schema2 (field-dependent :on s/Bool {false {:arg s/Str} true {:arg s/Int}})]
    (testing "checks clean"
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type :t1 :arg1 "arg1"})))
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type :t2 :arg2 "arg2"})))
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type :t3 :arg1 "arg1" :barg 8749})))
      (is (nil? (s/check schema2 {:on false :arg "arg"})))
      (is (nil? (s/check schema2 {:on true :arg 874 }))))
    (testing "handles uncoerced values"
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type "t1" :arg1 "arg1"})))
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type "t2" :arg2 "arg2"})))
      (is (nil? (s/check schema {:foo "foo" :bar 749 :type "t3" :arg1 "arg1" :barg 8749})))
      (is (nil? (s/check schema2 {:on "false" :arg "arg"})))
      (is (nil? (s/check schema2 {:on "true" :arg 874 }))))
    (testing "invalid checks"
      (is (some? (s/check schema {:foo "foo" :type :t1 :arg1 "arg1"})))
      (is (some? (s/check schema {:foo "foo" :bar 749})))
      (is (some? (s/check schema {:foo "foo" :bar 749 :type :t1 :arg2 "arg2"})))
      (is (some? (s/check schema {:foo "foo" :bar 749 :type :t3 :arg1 "arg1"})))
      (is (some? (s/check schema {:foo "foo" :bar 749 :type :t1 :arg1 "arg1" :barg 8749})))
      (is (some? (s/check schema2 {:on true :arg "arg" }))))))

(defn sorter-cmp [[str1 sch1] [str2 sch2]]
  (cond (not= (compare str1 str2) 0) (compare str1 str2)
        (instance? EnumSchema sch1) (apply compare (map #(apply vector (:vs %)) [sch1 sch2]))
        :otherwise (compare sch1 sch2)))

(deftest schema-walker-field-dependent-test
  (testing "Walk a schema with field-dependent in it"
    (let [calls (atom [])
          schema {:a (field-dependent :type s/Keyword {:t1 {:x s/Str} :t2 {:y s/Int}})}]
      (walker/walk-schema (fn [a b] (swap! calls conj [a b])) schema)
      (is (= (sort sorter-cmp @calls) [["" schema]
                                       ["a" (:a schema)]
                                       ["a.type" (s/enum :t1)]
                                       ["a.type" (s/enum :t2)]
                                       ["a.x" s/Str]
                                       ["a.y" s/Int]])))))