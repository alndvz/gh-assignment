(ns account.core
  (:require [id-generator.core :as id-gen]
            [db.core :as db]))

(def id-gen-key :account)

(defn create-transaction [account-number data]
  (let [sequence (id-gen/generate-id {:key :transaction-sequence
                                      :val account-number})]
    (assoc data
           :db/id (java.util.UUID/randomUUID)
           :type :transaction
           :account-number account-number
           :sequence sequence)))

(defn view-transactions [account-number]
  (db/unpack-docs
   (db/query '{:find [(pull ?e [:sequence :debit :credit :description]) ?seq]
               :where [[?e :type :transaction]
                       [?e :account-number account-number]
                       [?e :sequence ?seq]]
               :order-by [[?seq :desc]]
               :in [account-number]}
             account-number)))

(defn error [msg]
  (throw (ex-info msg {})))

;; Notes
;; The uniqueness constraint is on the account-number,
;; therefore multiple accounts can be created with the
;; same name.
(defn create [name]
  (let [account-number (str (id-gen/generate-id id-gen-key))
        account-entity {:db/id account-number
                        :type :account
                        :account-number account-number
                        :name name
                        :balance 0}
        transaction (create-transaction account-number {:description "account created"})]
    (db/write-many [account-entity transaction])
    account-entity))

(defn view [account-number]
  (let [account (db/query '{:find [(pull ?e [*])]
                            :where [[?e :db/id account-number]]
                            :in [account-number]}
                          account-number)]
    (first (db/unpack-docs account))))

(defn deposit [account-number amount]
  (let [account (view account-number)]
    (when (nil? account) (error "Cannot deposit in to non-existent account"))
    (when-not (pos-int? amount) (error "Cannot deposit zero or a negative value in to account"))
    (let [updated-account (update account :balance + amount)
          transaction (create-transaction account-number {:description "deposit"
                                                          :credit amount})]
      (db/write-many [updated-account transaction] :match-entity account)
      updated-account)))


(defn withdraw [account-number amount]
  (let [{:keys [balance] :as account} (view account-number)]
    (when (nil? account) (error "Cannot withdraw from non-existent account"))
    (when-not (pos-int? amount)
      (error "Cannot withdraw zero or a negative value from account"))
    (when (< balance amount) (error "Insufficient funds"))
    (let [updated-account (update account :balance - amount)
          transaction (create-transaction account-number {:description "withdraw"
                                                          :debit amount})]
      (db/write-many [updated-account transaction] :match-entity account)
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
          updated-to-account (update to-account :balance + amount)
          sent-trans (create-transaction from-account-number
                                         {:description (str "send to #" to-account-number)
                                          :debit amount})
          recv-trans (create-transaction to-account-number
                                         {:description (str "receive from #" from-account-number)
                                          :credit amount})]
      ;; All entities are within a transaction, all or none should
      ;; get written
      (db/write-many [updated-from-account
                      updated-to-account
                      sent-trans
                      recv-trans]
                     :match-entity from-account)
      updated-from-account)))
