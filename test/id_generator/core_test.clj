(ns id-generator.core-test
  (:require [id-generator.core :as sut]
            [db.core :as db]
            [fixtures.core :as fixtures]
            [clojure.test :refer [deftest is use-fixtures]]))

(def id-key-1 {:key :id-key-1})
(def id-key-2 {:key :id-key-2 :key-2 "123"})

(defn cleanup-ids [f]
  (f)
  (reset! sut/sequence-tracker {})
  (db/evict id-key-1 id-key-2))

(use-fixtures :once fixtures/start-deps cleanup-ids)

(deftest test-generate-id
  (is (= 0 (sut/generate-id id-key-1))
      "The first call to generate-id should always return a 0")
  (is (= 1 (sut/generate-id id-key-1))
      "Subsequent calls should increment")
  (is (= 0 (sut/generate-id id-key-2))
      "A call with a different key should return 0 again")
  (is (= 1 (sut/generate-id id-key-2))
      "And another call should increment")
  (is (apply distinct? (pmap (fn [_] (sut/generate-id :a)) (range 100)))
      "We should be able to generate id's from many threads, and never get the same id"))
