(ns core-clojure.patch-multi-test
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [core-clojure.utils.rest :as rest])
  (:import [com.starkbank.ellipticcurve PrivateKey]))

;; Only the outgoing HTTP call (clj-http.client/patch) is replaced: signing, headers and payload building run for real.

(defn- example-user []
  {:environment "sandbox"
   :id "5656565656565656"
   :private-key (.toPem (PrivateKey.))
   :type "project"})

(defn- patch-multi-recording
  "Runs patch-multi with a fake clj-http patch that answers `reply` and returns [result recorded-requests]."
  [payload query reply]
  (let [recorded (atom [])]
    (with-redefs [client/patch (fn [url options]
                                 (swap! recorded conj {:url url :options options})
                                 (if (>= (:status reply) 400)
                                   (throw (ex-info "" reply))
                                   reply))]
      [(rest/patch-multi "infra" "0.0.0" (example-user) "issuing-card" payload query "v2" "en-US" 15)
       @recorded])))

(defn- sent-body [request]
  (json/parse-string (:body (:options request)) true))

(deftest patch-multi-sends-patch-with-plural-payload-and-parses-response
  (testing "request half and response half"
    (let [[cards recorded] (patch-multi-recording
                            [{:id "1" :holder-name "Tony Stark" :holder-tax-id "012.345.678-90"}
                             {:id "2" :holder-name "Tony Stark" :holder-tax-id "012.345.678-90"}]
                            {}
                            {:status 200
                             :body (json/generate-string {:cards [{:id "1" :holderName "Tony Stark"}
                                                                  {:id "2" :holderName "Tony Stark"}]})})
          request (first recorded)]
      (is (= 1 (count recorded)))
      (is (str/ends-with? (:url request) "/v2/issuing-card"))
      (is (get-in request [:options :headers "Access-Signature"]))
      (is (= {:cards [{:id "1" :holderName "Tony Stark" :holderTaxId "012.345.678-90"}
                      {:id "2" :holderName "Tony Stark" :holderTaxId "012.345.678-90"}]}
             (sent-body request)))
      (is (= ["1" "2"] (map :id cards)))
      (is (= "Tony Stark" (:holder-name (first cards)))))))

(deftest patch-multi-forwards-the-query
  (testing "query parameters travel in the url"
    (let [[_ recorded] (patch-multi-recording [{:id "1"}] {:expand "rules"}
                                              {:status 200 :body "{\"cards\": [{\"id\": \"1\"}]}"})]
      (is (str/includes? (:url (first recorded)) "/v2/issuing-card"))
      (is (str/ends-with? (:url (first recorded)) "?expand=rules")))))

(deftest patch-multi-raises-the-api-error
  (testing "a 400 from the API is thrown with its error list"
    (let [error (try
                  (patch-multi-recording [{:id "1" :holder-name "Tony Stark"}] {}
                                         {:status 400
                                          :body "{\"errors\": [{\"code\": \"invalidHolder\", \"message\": \"must all be provided together\"}]}"})
                  nil
                  (catch clojure.lang.ExceptionInfo e e))]
      (is (some? error))
      (is (str/includes? (pr-str (ex-data error)) "invalidHolder")))))
