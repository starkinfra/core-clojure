(ns core-clojure.user)

(def private-key-content "-----BEGIN EC PRIVATE KEY-----\nMHQCAQEEIMYJ8sHNdOaDgmHqre26O2hByw1LDwMOgjuX67jiW6QYoAcGBSuBBAAK\noUQDQgAE8O6e/QoLFA51pIdOSJI/C34q7zPLo/f3GKCoJS1VYJBxpihqb1brcB4X\nNQfJu/7wSFc/Id/y1yPtHVMOFfRRQg==\n-----END EC PRIVATE KEY-----")

(defn user []
  {:environment "sandbox"
   :id  "6293779816382464"
   :private-key private-key-content
   :type "project"})
