(ns id-generator.core
  (:require [db.core :as db]))

(defn generate-id [key]
  (let [last-id (db/query '{:find [?last]
                            :where [[?e :db/id key]
                                    [?e :id-generator/last ?last]]
                            :in [key]}
                          key)
        next-id (if (empty? last-id) 1 (inc (first (first last-id))))]
    (db/write-many [{:db/id key :id-generator/last next-id}] :wait? true)
    next-id))
