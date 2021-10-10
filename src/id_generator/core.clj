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

(declare id-gen-svc)

(mount/defstate sequence-tracker :start (load-sequences!))
(mount/defstate id-svc :start (id-gen-svc)
  :stop (fn [[in out]]
          (async/close! in)
          (async/close! out)))

(defn persist-next! [key next-in-sequence]
  (db/write-many [{:db/id key :id-generator/last next-in-sequence}]))

(defn id-gen-svc []
  (let [in (async/chan)
        out (async/chan)]
    (async/go-loop []
      (let [key (async/<! in)
            last-id (get @sequence-tracker key -1)
            next-id (+ last-id 1)]
        (persist-next! key next-id)
        (swap! sequence-tracker assoc key next-id)
        (when (async/>! out next-id)
          (recur))))
    [in out]))

(defn generate-id [key]
  (let [[in out] id-svc]
    (async/>!! in key)
    (async/<!! out)))
