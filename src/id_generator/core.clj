(ns id-generator.core
  (:require [db.core :as db]
            [mount.core :as mount]
            [clojure.core.async :as async]))

(defn load-sequences! []
  (->> (db/query '{:find [?key ?last]
                   :where [[?e :id-generator/last ?last]
                           [?e :db/id ?key]]})
       (into {})
       (atom)))

(mount/defstate sequence-tracker :start (load-sequences!))

(defn persist-next! [key next-in-sequence]
  (db/write-many [{:db/id key :id-generator/last next-in-sequence}] :wait? true))

(defn generate-id [key]
  (let [safe-inc (fnil inc -1)
        next-id (get (swap! sequence-tracker update key safe-inc) key)]
    (persist-next! key next-id)
    next-id))
