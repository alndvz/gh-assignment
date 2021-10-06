(ns account.core-test
  (:require [account.core :as sut]
            [clojure.test :refer [deftest is]]))

(def account-name "Mr. Black")
(def more-account-names (map #(str "Account " %) (range 5)))

(deftest test-creating-an-account
  (let [new-account (sut/create account-name)
        more-accounts (map #(sut/create %) more-account-names)]
    (is (= account-name (:name new-account))
        "a new account should have the name we passed in to create")
    (is (= 0 (:balance new-account))
        "a new account should have a balance of zero")
    (is (int? (:account-number new-account))
        "a new account should have an account number")
    (is (apply distinct? (map :account-number more-accounts))
        "each account should have a unique account number")))

(deftest test-viewing-an-account
  (let [{:keys [account-number]} (sut/create account-name)
        account (sut/view account-number)]
    (is (= #{:account-number :name :balance :db/id :type} (set (keys account)))
        "viewing an account should return the correct set of keys")
    (is (nil? (sut/view 0))
        "a non-existent account should return nil")))

;; Notes
;; For these tests that use thrown-with-msg?, would be better to check a more
;; stable value other than the message.
(deftest test-deposit-into-account
  (let [{:keys [account-number]} (sut/create account-name)]
    (is (= 100 (:balance (sut/deposit account-number 100)))
        "returned account should have a new balance")
    (is (= 100 (:balance (sut/view account-number)))
        "viewed account should have a new balance")
    (is (thrown-with-msg? Exception #"Cannot deposit in to non-existent account"
                          (sut/deposit -1 100)))
    (is (thrown-with-msg? Exception #"Cannot deposit zero or a negative value in to account"
                          (sut/deposit account-number 0)))))

(deftest test-withdraw-from-account
  (let [{:keys [account-number]} (sut/create account-name)]
    (sut/deposit account-number 100)
    (is (= 95 (:balance (sut/withdraw account-number 5))))
    (is (thrown-with-msg? Exception #"Cannot withdraw zero or a negative value from account"
                          (sut/withdraw account-number -5)))
    (is (thrown-with-msg? Exception #"Insufficient funds"
                          (sut/withdraw account-number 100)))
    (is (thrown-with-msg? Exception #"Cannot withdraw from non-existent account"
                          (sut/withdraw -1 5)))))

(deftest test-transfer-between-accounts
  (let [{account-number-1 :account-number} (sut/create account-name)
        {account-number-2 :account-number} (sut/create account-name)
        _ (sut/deposit account-number-1 100)
        sending-account (sut/transfer account-number-1 account-number-2 5)
        receiving-account (sut/view account-number-2)]
    (is (= 95 (:balance sending-account))
        "sending account's balance should have reduced")
    (is (= 5 (:balance receiving-account))
        "receiving account's balance should have increased")
    (is (thrown-with-msg? Exception #"Cannot transfer a zero or negative value between accounts"
                          (sut/transfer account-number-1 account-number-2 -5)))
    (is (thrown-with-msg? Exception #"Insufficient funds"
                          (sut/transfer account-number-1 account-number-2 100)))
    (is (thrown-with-msg? Exception #"Cannot transfer from a non-existent account"
                          (sut/transfer -1 account-number-2 5)))
    (is (thrown-with-msg? Exception #"Cannot transfer to a non-existent account"
                          (sut/transfer account-number-1 -1 5)))
    (is (thrown-with-msg? Exception #"Cannot transfer funds to the same account"
                          (sut/transfer account-number-1 account-number-1 5)))))

(deftest test-account-transactions
  (let [{account-number-1 :account-number} (sut/create account-name)
        {account-number-2 :account-number} (sut/create account-name)
        {account-number-3 :account-number} (sut/create account-name)
        _ (sut/deposit account-number-1 100)
        _ (sut/deposit account-number-3 10)
        _ (sut/transfer account-number-1 account-number-2 5)
        _ (sut/transfer account-number-3 account-number-1 10)
        _ (sut/withdraw account-number-1 20)
        [entry-4 entry-3 entry-2 entry-1 entry-0] (sut/view-transactions account-number-1)]
    (is (= {:sequence 0
            :description "account created"} entry-0))
    (is (= {:sequence 1
            :credit 100
            :description "deposit"} entry-1))
    (is (= {:sequence 2
            :debit 5
            :description (str "send to #" account-number-2)} entry-2))
    (is (= {:sequence 3
            :credit 10
            :description (str "receive from #" account-number-3)} entry-3))
    (is (= {:sequence 4
            :debit 20
            :description "withdraw"} entry-4))))
