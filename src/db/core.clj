(ns db.core
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]
            [mount.core :as mount]))

(mount/defstate node
  :start (xt/start-node {:rocksdb {:xtdb/module 'xtdb.rocksdb/->kv-store
                                   :db-dir (io/file "./rocksdb")}
                         :xtdb/tx-log {:kv-store :rocksdb}
                         :xtdb/document-store {:kv-store :rocksdb}})
  :stop (.close node))

(defn put-random []
  (let [tx (xt/submit-tx node [[::xt/put
                                {:xt/id (rand-int 10000) :random (rand-int 10000)}]])]
    (xt/await-tx node tx)
    tx))

(defn write-many [entities & {:keys [wait?]}]
  (let [crux-entities (map (fn [{:keys [:db/id] :as entity}]
                             (->> (assoc entity :xt/id id))) entities)
        txes (mapv (fn [crux-entity]
                     [::xt/put crux-entity]) crux-entities)
        tx (xt/submit-tx node txes)]
    (when wait? (xt/await-tx node tx))
    tx))

(defn query [query & args]
  (apply xt/q (xt/db node) query args))

(defn evict [& entity-ids]
  (->> (mapv (fn [eid]
               [::xt/evict eid]) entity-ids)
       (xt/submit-tx node)))
