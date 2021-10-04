(ns user
  (:require [server.core :as server]))

(defn start []
  (server/-main))
