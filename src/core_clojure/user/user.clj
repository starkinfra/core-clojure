(ns core-clojure.user.user
  (:require [core-clojure.utils.check :refer [check-private-key check-environment]]))

(defn validate [private-key environment]
  {:environment (check-environment environment) :private-key (check-private-key private-key)})
