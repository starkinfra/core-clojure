(ns core-clojure.api-test
  (:require [clojure.test :refer [deftest is testing]]
            [core-clojure.utils.api :refer [last-name-plural]]))


(deftest keeps-existing-plurals
  (testing "names that already work"
    (doseq [[resource plural] {"transaction" :transactions
                               "boleto-payment" :payments
                               "pix-domain" :domains
                               "dict-key" :keys
                               "category" :categories
                               "credit-holmes" :holmes
                               "address" :address
                               "log" :logs
                               "entity" :entities
                               "key" :keys}]
      (is (= plural (last-name-plural resource)) resource))))

(deftest adds-es-after-ch-sh-and-x
  (testing "names ending in ch, sh or x"
    (doseq [[resource plural] {"ai-speech" :speeches
                               "branch" :branches
                               "swish" :swishes
                               "tax-box" :boxes}]
      (is (= plural (last-name-plural resource)) resource))))
