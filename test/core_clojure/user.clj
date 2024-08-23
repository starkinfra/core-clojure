(ns core-clojure.user)

(defn user []
  {:environment "sandbox"
   :id (System/getenv "SANDBOX_ID"); "9999999999999999"
   :private-key (System/getenv "SANDBOX_PRIVATE_KEY"); "-----BEGIN EC PRIVATE KEY-----\nMHUCAQEEIUozJdDjfNVL9ulX1CmRW7a7TgmeaFsem7G5GzFAyky2HaAHBgUrgQQA\nCqFEA0IABJlS4omSpIcq/MC1a39wProUxPlpcsirelSHOzGmwKJ4ZtYHhW7bYr1Y\nxX4Ae2b2ff/v/GNgn3nSsJ73QaUgn7s=\n-----END EC PRIVATE KEY-----"
   :type "project"})
