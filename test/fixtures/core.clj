(ns fixtures.core
  (:require [mount.core :as mount]
            [db.core :as db]))

(defn start-deps [f]
  (mount/start (mount/swap {#'db/node (db/memory-node)}))
  (f))
