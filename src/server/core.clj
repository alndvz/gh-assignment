(ns server.core
  (:require [aleph.http :as http]
            [manifold.deferred :as d]
            [db.core :as db]
            [mount.core :as mount]))

(defn slow-handler [_]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body (pr-str (db/put-random))})

(defn handler [req]
  (let [dfr (d/deferred)]
    (binding [*use-context-classloader* false]
        (d/success! dfr (slow-handler req)))
    dfr))

(mount/defstate server
  :start (http/start-server #'handler {:port 8080})
  :stop (.close server))

(defn -main []
  (mount/start))
