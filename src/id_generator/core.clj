(ns id-generator.core
  (:require [db.core :as db]
            [mount.core :as mount]))

(defn load-sequences! []
  (->> (db/query '{:find [?key ?last]
                   :where [[?e :id-generator/last ?last]
                           [?e :db/id ?key]]})
       (into {})
       (atom)))

(mount/defstate sequence-tracker :start (load-sequences!))

(defn persist-next! [key next-in-sequence]
  (db/write-many [{:db/id key :id-generator/last next-in-sequence}]))

(defn generate-id [key]
  (let [last-id (get @sequence-tracker key -1)
        next-id (+ last-id 1)]
    (persist-next! key next-id)
    (swap! sequence-tracker assoc key next-id)
    next-id))
