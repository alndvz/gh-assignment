(ns db.core-test
  (:require [db.core :as sut]
            [clojure.test :refer [deftest is]]))

(def entity-id :id1)
(def entity {:db/id entity-id :name "John"})

(deftest test-basic-db
  (sut/write-many [entity] :wait? true)
  (is (= #{[entity]} (sut/query '{:find [(pull ?e [:db/id :name])]
                                  :where [[?e :db/id entity-id]]})))
  (sut/evict entity-id)
  (is (= #{[{}]} (sut/query '{:find [(pull ?e [:db/id :name])]
                              :where [[?e :db/id entity-id]]}))))
