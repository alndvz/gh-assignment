(ns db.core
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]
            [mount.core :as mount]))

(defn rocks-node []
  (xt/start-node {:rocksdb {:xtdb/module 'xtdb.rocksdb/->kv-store
                            :db-dir (io/file "./rocksdb")}
                  :xtdb/index-store {:kv-store :rocksdb}
                  :xtdb/tx-log {:kv-store :rocksdb}
                  :xtdb/document-store {:kv-store :rocksdb}}))

(defn memory-node []
  (xt/start-node {}))

(mount/defstate node
  :start (rocks-node)
  :stop (.close node))

(defn write-many [entities & {:keys [wait?]
                              {:keys [:db/id] :as match-entity} :match-entity}]
  (let [xt-entities (map (fn [{:keys [:db/id] :as entity}]
                           (->> (assoc entity :xt/id id))) entities)
        put-ops (mapv (fn [xt-entity]
                        [::xt/put xt-entity]) xt-entities)
        match-op (when match-entity
                   [[::xt/match id (assoc match-entity :xt/id id)]])
        tx (->> (concat match-op put-ops)
                (filter identity)
                (vec))
        stx (xt/submit-tx node tx)]
    (when wait? (xt/await-tx node stx))
    stx))

(defn query [query & args]
  (apply xt/q (xt/db node) query args))

(defn evict [& entity-ids]
  (->> (mapv (fn [eid]
               [::xt/evict eid]) entity-ids)
       (xt/submit-tx node)
       (xt/await-tx node)))

(defn unpack-docs [query-result]
  (map (fn [[doc]] (dissoc doc :xt/id)) query-result))
