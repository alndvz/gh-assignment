(ns db.core
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]))

(def node (xt/start-node {:rocksdb {:xtdb/module 'xtdb.rocksdb/->kv-store
                                       :db-dir (io/file "./rocksdb")}
                          :xtdb/tx-log {:kv-store :rocksdb}
                          :xtdb/document-store {:kv-store :rocksdb}}))

(defn put-random []
  (let [tx (xt/submit-tx node [[::xt/put
                                {:xt/id (rand-int 10000) :random (rand-int 10000)}]])]
    (xt/await-tx node tx)
    tx))
