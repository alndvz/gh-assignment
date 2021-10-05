(ns id-generator.core-test
  (:require [id-generator.core :as sut]
            [db.core :as db]
            [clojure.test :refer [deftest is use-fixtures]]))

(def id-key-1 {:key :id-key-1})
(def id-key-2 {:key :id-key-2 :key-2 "123"})

(defn cleanup-ids [f]
  (f)
  (db/evict id-key-1 id-key-2))

(use-fixtures :once cleanup-ids)

(deftest test-generate-id
  (is (= 1 (sut/generate-id id-key-1))
      "The first call to generate-id should always return a 1")
  (is (= 2 (sut/generate-id id-key-1))
      "Subsequent calls should increment")
  (is (= 1 (sut/generate-id id-key-2))
      "A call with a different key should return 1 again")
  (is (= 2 (sut/generate-id id-key-2))
      "And another call should increment"))
