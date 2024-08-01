(ns core-clojure.utils.api
  (:require
   [clojure.string :as str]))


(defn last-name [resource]
  (let [split-string (str/split resource #"-")]
    (last split-string)))

(defn last-name-plural [resource]
  (let [last-name (last-name resource)]
    (cond
      (.endsWith last-name "s") (keyword last-name)
      (and (.endsWith last-name "y") (not (.endsWith last-name "ey")))
      (keyword (str (subs last-name 0 (dec (count last-name))) "ies"))
      :else (keyword (str last-name "s")))))

(defn endpoint [resource]
  (-> resource
      (str/replace "-log" "/log")
      (str/replace "-attempt" "/attempt")))

(defn build-resource-map [resource-name entities]
  {(keyword resource-name) entities})
