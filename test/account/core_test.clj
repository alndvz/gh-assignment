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
