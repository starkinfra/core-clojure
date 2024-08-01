(ns core-clojure.key
  (:import (com.starkbank.ellipticcurve PrivateKey)
           (java.io File PrintWriter)))

(defrecord Key [privatePem publicPem])

(defn create-key
  ([]
   (let [private-key (PrivateKey.)]
     (->Key (.toPem private-key) (.toPem (.publicKey private-key)))))

  ([save-path]
   (let [private-key (PrivateKey.)
         public-key (.publicKey private-key)
         key (->Key (.toPem private-key) (.toPem public-key))
         directory (File. save-path)]
     (when-not (.exists directory)
       (.mkdirs directory))
     (with-open [out-private (PrintWriter. (File. save-path "privateKey.pem"))
                 out-public (PrintWriter. (File. save-path "publicKey.pem"))]
       (.println out-private (:privatePem key))
       (.println out-public (:publicPem key)))
     key)))
