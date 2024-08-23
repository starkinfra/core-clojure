(ns core-clojure.utils.check
  (:import (com.starkbank.ellipticcurve PrivateKey)))

(defn check-environment [env]
  (case env
    "production" env
    "sandbox" env
    nil (throw (IllegalArgumentException. "please set an environment"))
    (throw (IllegalArgumentException. "environment must be either production or sandbox"))))

(defn check-private-key [private-key]
  (try
    (let [parsed-key (PrivateKey/fromPem private-key)]
      (if (= (.name (.curve parsed-key)) "secp256k1")
        parsed-key
        (throw (IllegalArgumentException. "private_key must be valid secp256k1 ECDSA string in pem format"))))
    (catch Exception e
      (throw (IllegalArgumentException. "private_key must be valid secp256k1 ECDSA string in pem format")))))

