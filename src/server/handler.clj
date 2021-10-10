(ns server.handler
  (:require [account.core :as account]))

(defn strip-unwanted-keys [m]
  (let [strip #(dissoc % :db/id :type)]
    (if (map? m)
      (strip m)
      (map #(strip %) m))))

(defn create-account [{{:keys [name]} :body-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys (account/create name))})
    (catch Exception e
      (raise e))))

(defn view-account [{{:keys [id]} :path-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys (account/view id))})
    (catch Exception e
      (raise e))))

(defn deposit-into-account [{{:keys [id]} :path-params
                             {:keys [amount]} :body-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys (account/deposit id amount))})
    (catch Exception e
      (raise e))))

(defn withdraw-from-account [{{:keys [id]} :path-params
                              {:keys [amount]} :body-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys (account/withdraw id amount))})
    (catch Exception e
      (raise e))))

(defn transfer [{{:keys [id]} :path-params
                 {:keys [account-number amount]} :body-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys
                     (account/transfer id account-number amount))})
    (catch Exception e
      (raise e))))

(defn view-audit-log [{{:keys [id]} :path-params} respond raise]
  (try
    (respond {:body (strip-unwanted-keys (account/view-transactions id))})
    (catch Exception e
      (raise e))))
