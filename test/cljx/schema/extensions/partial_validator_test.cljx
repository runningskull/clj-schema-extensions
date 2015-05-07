(ns schema.extensions.partial-validator-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs cemerick.cljs.test
            [schema.core :as s]
            [schema.extensions.partial-validator :refer [partial-check not-modified-partial-check not-modifiable?]]
            [schema.extensions.field :refer [field]])
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest testing)]))

(deftest partial-validate-test
  (let [schema {:a s/Int :b s/Int}]
    (testing "simple partial object"
      (is (nil? (partial-check schema {:a 1}))))
    (testing "empty partial object"
      (is (nil? (partial-check schema {}))))
    (testing "full object"
      (is (nil? (partial-check schema {:a 1 :b 2}))))
    (testing "invalid object"
      (is (some? (partial-check schema {:a "abc"}))))
    (testing "invalid full object"
      (is (some? (partial-check schema {:a "abc" :b 1}))))))

(deftest not-modifiable?-test
  (testing "modified? true"
    (is (not-modifiable? (field s/Int :modifiable false))))
  (testing "modified? false"
    (is (not (not-modifiable? (field s/Int))))
    (is (not (not-modifiable? (field s/Int :modifiable true))))))

(deftest not-modified-partial-check-test
  (let [modifiable-schema {:a s/Int}
        not-modifiable-schema {:a (field s/Int :modifiable false)}]
    (testing "when modifiable and not modified"
      (is (nil? (not-modified-partial-check modifiable-schema {}))))
    (testing "when modifiable and modified"
      (is (nil? (not-modified-partial-check modifiable-schema {:a 1}))))
    (testing "when not modifiable and not modified"
      (is (nil? (not-modified-partial-check not-modifiable-schema {}))))
    (testing "when not modifiable and modified"
      (is (some? (not-modified-partial-check not-modifiable-schema {:a 1}))))))