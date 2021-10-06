(ns account.core
  (:require [id-generator.core :as id-gen]
            [db.core :as db]))

(def id-gen-key :account)

(defn error [msg]
  (throw (ex-info msg {})))

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
    (when (nil? account) (error "Cannot deposit in to non-existent account"))
    (when-not (pos-int? amount) (error "Cannot deposit zero or a negative value in to account"))
    (let [updated-account (update account :balance + amount)]
      (db/write-many [(assoc updated-account :db/id account-number)] :wait? true)
      updated-account)))

(defn withdraw [account-number amount]
  (let [{:keys [balance] :as account} (view account-number)]
    (when (nil? account) (error "Cannot withdraw from non-existent account"))
    (when-not (pos-int? amount)
      (error "Cannot withdraw zero or a negative value from account"))
    (when (< balance amount) (error "Insufficient funds"))
    (let [updated-account (update account :balance - amount)]
      (db/write-many [(assoc updated-account :db/id account-number)] :wait? true)
      updated-account)))

(defn transfer [from-account-number to-account-number amount]
  (when (= from-account-number to-account-number)
    (error "Cannot transfer funds to the same account"))
  (let [{:keys [balance] :as from-account} (view from-account-number)
        to-account (view to-account-number)]
    (when (nil? from-account) (error "Cannot transfer from a non-existent account"))
    (when (nil? to-account) (error "Cannot transfer to a non-existent account"))
    (when-not (pos-int? amount)
      (error "Cannot transfer a zero or negative value between accounts"))
    (when (< balance amount) (error "Insufficient funds"))
    (let [updated-from-account (update from-account :balance - amount)
          updated-to-account (update to-account :balance + amount)]
      ;; Both entities are within a transaction, both or none should
      ;; get written
      (db/write-many [(assoc updated-from-account :db/id from-account-number)
                      (assoc updated-to-account :db/id to-account-number)]
                     :wait? true)
      updated-from-account)))
