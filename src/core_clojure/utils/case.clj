(ns core-clojure.utils.case
  (:require
   [clojure.string :as str]
   [cheshire.core :as chesire]
   [clojure.walk :as walk]))


(defn kebab-to-camel [payload]
  (let [parts (clojure.string/split (name payload) #"-")]
    (keyword (str (first parts) (apply str (map clojure.string/capitalize (rest parts)))))))

(defn transform-keys [payload]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (into {} (map (fn [[key value]] [(kebab-to-camel key) value]) x))
       x))
   payload))

(defn kebab-map-to-camel-json [payload]
  (if (or (nil? payload) (empty? payload))
    ""
    (chesire/generate-string (transform-keys payload)))
  )

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
