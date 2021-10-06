(ns account.core
  (:require [id-generator.core :as id-gen]
            [db.core :as db]))

(def id-gen-key :account)

(defn error [msg map]
  (throw (ex-info msg map)))

;; Notes
;; The uniqueness constraint is on the account-number,
;; therefore multiple accounts can be created with the
;; same name.
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

(defn deposit [account-number amount]
  (let [account (view account-number)]
    (when (nil? account) (error "Cannot deposit in to non-existent account" {}))
    (when-not (pos-int? amount) (error "Cannot deposit zero or a negative value in to account" {}))
    (let [updated-account (update account :balance + amount)]
      (db/write-many [(assoc updated-account :db/id account-number)] :wait? true)
      updated-account)))

(defn withdraw [account-number amount]
  (let [{:keys [balance] :as account} (view account-number)]
    (when (nil? account) (error "Cannot withdraw from non-existent account" {}))
    (when-not (pos-int? amount) (error "Cannot withdraw zero or a negative value from account" {}))
    (when (< balance amount) (error "Insufficient funds" {}))
    (let [updated-account (update account :balance - amount)]
      (db/write-many [(assoc updated-account :db/id account-number)] :wait? true)
      updated-account)))
