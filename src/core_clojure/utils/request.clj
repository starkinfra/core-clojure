(ns core-clojure.utils.request
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [core-clojure.error :refer [stark-error]]
            [core-clojure.user.user :refer [validate]]
            [core-clojure.utils.case :refer [kebab-map-to-camel-json
                                             transform-keys-camel]])
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:import [com.starkbank.ellipticcurve Ecdsa PrivateKey]
           [java.net URLEncoder]
           (java.time Instant)))


(defn- dynamic-request [method url options]
  (let [method-fn (resolve (symbol "clj-http.client" (name method)))]
    (if method-fn
      (method-fn url options)
      (throw (IllegalArgumentException. (str "Unknown HTTP method: " method))))))


(defn- encode-query [params]
  (if (or (nil? params) (empty? params))
    ""
    (let [query-string (atom "/?")
          encode-param (fn [[k v]]
                         (if (sequential? v)
                           (str (name k) "=" (URLEncoder/encode (str/join "," v) "UTF-8"))
                           (str (name k) "=" (URLEncoder/encode (str v) "UTF-8"))))]
      (swap! query-string str
             (str/join "&" (map encode-param (filter (fn [[_ v]] v) params))))
      @query-string)))


(defn- get-access-signature [access-id access-time payload private-key]
  (let [message              (format "%s:%s:%s" access-id access-time payload)
        private-key-from-pem (PrivateKey/fromPem private-key)]
    (Ecdsa/sign message private-key-from-pem)))


(defn- pre-process [host sdk-version user path payload query api-version language timeout prefix]
  (let [service
        (atom ((keyword host) {:infra "starkinfra"
                               :bank "starkbank"
                               :sign "starksign"}))
        hostname
        (atom ((keyword (:environment user))
               {:production (str "https://api." @service ".com/" api-version)
                :sandbox (str "https://sandbox.api." @service ".com/" api-version)}))
        encoded-query
        (atom (encode-query (transform-keys-camel query)))
        url
        (atom (str @hostname "/" path @encoded-query))
        access-time
        (atom (str (.getEpochSecond (Instant/now))))
        access-id
        (str (:type user) "/" (:id user))
        body
        (atom (kebab-map-to-camel-json payload))
        timeout-ms
        (* timeout 1000)
        prefix
        (if (or (nil? prefix) (empty? prefix))
          ""
          (str prefix "-"))
        clojure-version
        (str
         (:major *clojure-version*) "."
         (:minor *clojure-version*) "."
         (:incremental *clojure-version*) ".")
        user-agent
        (str prefix "Clojure-" clojure-version "-SDK-" @service "-" sdk-version)
        access-signature
        (get-access-signature  access-id @access-time @body (:private-key user))]
    {:url @url
     :options {:socket-timeout timeout-ms
               :connection-timeout timeout-ms
               :body @body
               :headers {"Access-Time"      @access-time,
                         "User-Agent"       user-agent
                         "Content-Type"     "application/json"
                         "Accept-Language"  language
                         "Access-Id"        access-id
                         "Access-Signature" (.toBase64 access-signature)}}}))


(defn fetch
  [host sdk-version user method path payload query api-version language timeout prefix throw-error]
  (validate (:private-key user) (:environment user))
  (let [request-options (pre-process
                         host
                         sdk-version
                         user
                         path
                         payload
                         query
                         api-version
                         language
                         timeout
                         prefix)
        request (atom "")]
    (try
      (reset! request (dynamic-request method (:url request-options) (:options request-options)))
      (catch Exception e
        (if throw-error
          (case (:status (.getData e))
            400 (throw (ex-info "" (stark-error (:body (.getData e)))))
            500 (throw (ex-info "" (stark-error (:body (.getData e)))))
            throw Exception)
          (reset! request {:status (:status (.getData e))
                           :body (stark-error (:body (.getData e)))}))))
    {:status (:status @request) :content (:body @request)}))
