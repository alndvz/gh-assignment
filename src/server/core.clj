(ns server.core
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [reitit.middleware :as middleware]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [mount.core :as mount]
            [server.handler :as handler]))

(def ring-handler
  (ring/ring-handler
   (ring/router
    ["/account" {:middleware [:negotiation]}
     ["" {:post handler/create-account}]
     ["/:id" {:get handler/view-account}]
     ["/:id/deposit" {:post handler/deposit-into-account}]
     ["/:id/withdraw" {:post handler/withdraw-from-account}]
     ["/:id/send" {:post handler/transfer}]
     ["/:id/audit" {:get handler/view-audit-log}]]
    {::middleware/registry {:negotiation muuntaja/format-middleware}
     :data {:muuntaja m/instance}})))

(mount/defstate server
  :start (http/start-server (http/wrap-ring-async-handler #'ring-handler) {:port 8080})
  :stop (.close server))

(defn -main []
  (mount/start))
