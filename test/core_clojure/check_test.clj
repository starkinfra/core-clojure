(ns core-clojure.check-test
   (:require [clojure.test :refer [deftest is testing]]
             [core-clojure.utils.check :refer [check-private-key]]
             ))

(def private-key-content "-----BEGIN EC PRIVATE KEY-----\nMHQCAQEEIMYJ8sHNdOaDgmHqre26O2hByw1LDwMOgjuX67jiW6QYoAcGBSuBBAAK\noUQDQgAE8O6e/QoLFA51pIdOSJI/C34q7zPLo/f3GKCoJS1VYJBxpihqb1brcB4X\nNQfJu/7wSFc/Id/y1yPtHVMOFfRRQg==\n-----END EC PRIVATE KEY-----")

(deftest parse-private-key
  (testing "get method query and simple"
    (let [pkey (check-private-key private-key-content)]
      (is (= (str (type pkey)) "class com.starkbank.ellipticcurve.PrivateKey"))))
)
