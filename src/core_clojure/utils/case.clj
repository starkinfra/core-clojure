(ns core-clojure.utils.case
  (:require
   [clojure.string :as str]
   [cheshire.core :as chesire]
   [clojure.walk :as walk]))


(defn kebab-to-camel [payload]
  (let [parts (clojure.string/split (name payload) #"-")]
    (keyword (str (first parts) (apply str (map clojure.string/capitalize (rest parts)))))))

(defn transform-keys-camel [payload]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (into {} (map (fn [[key value]] [(kebab-to-camel key) value]) x))
       x))
   payload))

(defn kebab-map-to-camel-json [payload]
  (if (or (nil? payload) (empty? payload))
    ""
    (chesire/generate-string (transform-keys-camel payload)))
  )

(defn camel-to-kebab [s]
  (-> s
      (str/replace #"([a-z])([A-Z])" "$1-$2")
      (str/lower-case)))

(defn cast-keys-to-kebab [m]
  (letfn [(transform-key [k]
            (if (keyword? k)
              (keyword (camel-to-kebab (name k)))
              k))]
    (walk/postwalk (fn [x] (if (map? x)
                        (into {} (map (fn [[k v]] [(transform-key k) v]) x))
                        x)) m)))

(defn json-to-map
  [json-string]
  (chesire/parse-string json-string true))
