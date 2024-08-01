(ns core-clojure.error
  (:require [cheshire.core :as cheshire]))

(defn- parse-json [json-str]
  (cheshire/parse-string json-str keyword))

(defn stark-error [error]
  (parse-json error))
