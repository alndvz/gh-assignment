(ns account.core
  (:require [id-generator.core :as id-gen]
            [db.core :as db]))

(def id-gen-key :account)

;; Notes
;; The uniqueness constraint is on the account-number,
;; therefore multiple accounts can be created with the
;; same name. There's also no error handling.
(defn create [name]
  (let [account-number (id-gen/generate-id id-gen-key)
        account-entity {:db/id account-number
                        :account-number account-number
                        :name name
                        :balance 0}]
    (db/write-many [account-entity] :wait? true)
    (dissoc account-entity :db/id)))

(defn view [account-number]
  (let [account (db/query '{:find [(pull ?e [*])]
                            :where [[?e :account-number account-number]]
                            :in [account-number]}
                          account-number)]
    (-> (first (db/unpack-docs account))
        (dissoc :db/id))))
