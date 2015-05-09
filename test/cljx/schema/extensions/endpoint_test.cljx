(ns schema.extensions.endpoint-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs cemerick.cljs.test
            [schema.core :as s]
            [schema.extensions.endpoint :as endpoint])
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest testing)]))

(deftest endpoint-test
  (let [schema {:a (endpoint/endpoint [{:id s/Str :b s/Str}] :singular "single")}
        data {:a [{:id "abc123" :b "yoyo"}
                  {:id "def456" :b "mama"}]}]
    (testing "endpoint?"
      (is (endpoint/endpoint? (:a schema))))
    (testing "endpoint-singular"
      (is (= "single" (endpoint/endpoint-singular (:a schema)))))
    (testing "validation"
      (is (nil? (s/check schema data))))))

(deftest get-elem
  (let [v [{:id "123" :a 1} {:id "456" :a 2}]]
    (testing "find object in array"
      (is (= {:id "123" :a 1} (endpoint/get-elem v "123"))))
    (testing "fail to find object in array"
      (is (= nil (endpoint/get-elem v "789"))))))
