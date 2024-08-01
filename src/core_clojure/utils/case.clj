(ns core-clojure.utils.case
  (:require
   [clojure.string :as str]
   [cheshire.core :as chesire]))


(defn- kebab-to-camel [s]
  (let [parts (str/split s #"-")]
    (str (first parts)
         (apply str (map str/capitalize (rest parts))))))

(defn- transform-keys [m]
  (into {}
        (for [[k v] m]
          [(kebab-to-camel (name k)) v])))

(defn kebab-map-to-camel-json [payload]
  (if (or (nil? payload) (empty? payload))
    ""
    (chesire/generate-string (transform-keys payload))))

(defn camel-to-kebab [s]
  (let [pattern (re-pattern "([a-z])([A-Z])")]
    (-> s
        (str/replace pattern "$1-$2")
        str/lower-case)))

(defn transform-keys-to-kebab [m]
  (into {}
        (for [[k v] m]
          [(keyword (camel-to-kebab (name k))) v])))

(defn camel-json-to-kebab-map [json-str]
  (let [parsed-json (chesire/parse-string json-str true)]
    (transform-keys-to-kebab parsed-json)))
