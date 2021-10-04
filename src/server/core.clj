(ns server.core
  (:require [aleph.http :as http]
            [manifold.deferred :as d]
            [db.core :as db]))

(defn slow-handler [_]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body (pr-str (db/put-random))})

(defn handler [req]
  (let [dfr (d/deferred)]
    (d/success! dfr (slow-handler req))
    dfr))

(http/start-server handler {:port 8080})
