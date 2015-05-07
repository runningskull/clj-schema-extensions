(ns schema.extensions.schema-walker-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs cemerick.cljs.test
            [schema.core :as s]
            [schema.extensions.endpoint :refer [endpoint]]
            [schema.extensions.schema-walker :as walker]
            #+clj [schema.extensions.util-macros :as um])
  #+cljs (:require-macros [schema.extensions.schema-walker :as walker-macro]
                          [cemerick.cljs.test :refer (is deftest testing)]))

(deftest walk-schema
  (testing "Walk a basic schema"
    (let [calls (atom [])]
      (walker/walk-schema (fn [a b] (swap! calls conj [a b])) {:a s/Str :b s/Int})
      (is (= @calls [["" {:a s/Str :b s/Int}] ["a" s/Str] ["b" s/Int]]))))
  (testing "Walk a deep schema"
    (let [calls (atom [])]
      (walker/walk-schema (fn [a b] (swap! calls conj [a b])) {:a {:b s/Str :c s/Int}})
      (is (= @calls [["" {:a {:b s/Str :c s/Int}}] ["a" {:b s/Str :c s/Int}] ["a.b" s/Str] ["a.c" s/Int]]))))
  (testing "Walk a deep schema with custom sep"
    (let [calls (atom [])]
      (walker/walk-schema (fn [a b] (swap! calls conj [a b])) {:a {:b s/Str :c s/Int}} :sep "/")
      (is (= @calls [["" {:a {:b s/Str :c s/Int}}] ["a" {:b s/Str :c s/Int}] ["a/b" s/Str] ["a/c" s/Int]]))))
  (testing "Walk a schema with record wrappers"
    (let [calls (atom [])]
      (walker/walk-schema (fn [a b] (swap! calls conj [a b])) {:a (s/maybe {:b s/Str})})
      (is (= @calls [["" {:a (s/maybe {:b s/Str})}] ["a" (s/maybe {:b s/Str})] ["a.b" s/Str]])))))


(deftest each-endpoints
  (testing "Find a single endpoint"
    (is (= (walker/all-endpoints {:a (endpoint [{:b s/Str}] :singular "foo")})
           [{:path "a" :schema {:b s/Str} :keywords [:a] :id-schema nil :singular "foo"}])))
  (testing "Find a multiple endpoint"
    (is (= (walker/all-endpoints {:a (endpoint [{:b s/Str}] :singular "foo")
                                  :x {:y (endpoint [{:y s/Int}] :singular "bar")}})
           [{:path "a" :schema {:b s/Str} :keywords [:a] :id-schema nil :singular "foo"}
            {:path "x/y" :schema {:y s/Int} :keywords [:x :y] :id-schema nil :singular "bar"}])))
  (testing "ID Schema"
    (is (= (walker/all-endpoints {:a (endpoint [{:b s/Str :id s/Int}])})
           [{:path "a" :schema {:b s/Str :id s/Int} :keywords [:a] :id-schema s/Int :singular nil}]))))