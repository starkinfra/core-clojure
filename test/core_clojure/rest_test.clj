(ns core-clojure.rest-test
  (:require [clojure.test :refer [deftest is testing]]
            [core-clojure.utils.rest :as rest]
            [core-clojure.user :refer [user]]
            [clj-time.core :as t] 
            [clj-time.format :as f]))

(defn future-date [days]
  (let [now (t/now)
        future (t/plus now (t/days days))
        formatter (f/formatter "yyyy-MM-dd")]
    (f/unparse formatter future)))

(deftest rest-get-stream
  (testing "get stream method "
    (doseq [item 
            (rest/get-stream
             "bank"
             "0.0.0"
             (user)
             "invoice"
             {:limit 1
              :status "paid"}
             "v2"
             "en-US"
             15)]
      (is (int?
           (:amount (rest/get-id
                     "bank"
                     "0.0.0"
                     (user)
                     "invoice"
                     (:id item)
                     {}
                     "v2"
                     "en-US"
                     15)))) 
      (is (string?
           (rest/get-content
            "bank"
            "0.0.0"
            (user)
            "invoice"
            (:id item)
            "pdf"
            {}
            "v2"
            "en-US"
            15)))
      (is (contains? (rest/get-sub-resource
                      "bank"
                      "0.0.0"
                      (user)
                      "invoice"
                      (:id item)
                      "payment"
                      {}
                      "v2"
                      "en-US"
                      15) :payment))
     )))
  


(deftest rest-get-page
  (testing "get page method "
    (let [invoices (rest/get-page
                    "bank"
                    "0.0.0"
                    (user)
                    "invoice"
                    {:limit 1 :status "paid"}
                    "v2"
                    "en-US"
                    15)] 
      (is (int? 
           (:amount (get (:content invoices) 0))
           )
          )
      )
    ))

(deftest rest-get-public-key
  (testing "get public key method "
    (let [public-key (rest/get-public-key
                      "bank"
                      "0.0.0"
                      (user)
                      "v2"
                      "en-US"
                      15)]
      (is (string? public-key)))))


(deftest rest-post
  (testing "post method "
    (let [invoices {:amount 400000}
          corporate-invoice (rest/post
                             "bank"
                             "0.0.0"
                             (user)
                             "corporate-invoice"
                             invoices
                             ""
                             "v2"
                             "en-US"
                             15)]
      (is (int?
           (:amount (:invoice corporate-invoice)))))))

(deftest rest-post-multi
  (testing "post multi method "
    (let [payload [{:amount 400000
                                :name "Arya Stark"
                                :taxId "20.018.183/0001-80"}
                               {:amount 200000
                                :name "John Snow"
                                :taxId "20.018.183/0001-80"}]
          invoices (rest/post-multi
                    "bank"
                    "0.0.0"
                    (user)
                    "invoice"
                    payload
                    ""
                    "v2"
                    "en-US"
                    15)]
      (is (int?
           (:amount (get invoices 0)))))))

(deftest rest-post-single
  (testing "post single method "
    (let [invoices {:amount 400000}
          corporate-invoice (rest/post-single
                    "bank"
                    "0.0.0"
                    (user)
                    "corporate-invoice"
                    invoices
                    ""
                    "v2"
                    "en-US"
                    15)]
      (is (int?
           (:amount corporate-invoice))))))

(deftest rest-post-sub-resource
  (testing "post sub resource method "
    (let [uuid (:uuid (rest/post-single
                       "bank"
                       "0.0.0"
                       (user)
                       "merchant-session"
                       {:allowedFundingTypes ["debit" "credit"]
                        :allowedInstallments [{:totalAmount 0 :count 1}
                                              {:totalAmount 120 :count 2}
                                              {:totalAmount 180 :count 12}]
                        :expiration 3600
                        :challengeMode "disabled"
                        :tags ["yourTags"]}
                       ""
                       "v2"
                       "en-US"
                       15))
          merchant-session {
                            :amount 180
                            :installmentCount 12
                            :cardExpiration "2035-01"
                            :cardNumber "5277696455399733"
                            :cardSecurityCode "123"
                            :holderName "Holder Name"
                            :holderEmail "holdeName@email.com"
                            :holderPhone "11111111111"
                            :fundingType "credit"
                            :billingCountryCode "BRA"
                            :billingCity "São Paulo"
                            :billingStateCode "SP"
                            :billingStreetLine1 "Rua do Holder Name, 123"
                            :billingStreetLine2 ""
                            :billingZipCode "11111-111"
                            :metadata {:userAgent "Postman"
                                       :userIp "255.255.255.255"
                                       :language "pt-BR"
                                       :timezoneOffset 3
                                       :extraData "extraData"}}
          merchant-session-purchase (rest/post-sub-resource
                                     "bank"
                                     "0.0.0"
                                     (user)
                                     "merchant-session"
                                     uuid
                                     "purchase"
                                     merchant-session
                                     ""
                                     "v2"
                                     "en-US"
                                     15)]
      
      (is (:message merchant-session-purchase) "Merchant Purchase successfully created"))))


(deftest rest-delete
  (testing "delete method "
    (let [transfer (rest/post-multi
                    "bank"
                    "0.0.0"
                    (user)
                    "transfer"
                    [{:amount 1000000
                      :bankCode "20018183"
                      :branchCode "2201"
                      :accountNumber "10000-0"
                      :taxId "20.018.183/0001-80"
                      :name "Daenerys Targaryen Stormborn"
                      :externalId "my-external-id"
                      :tags ["daenerys" "invoice/1234"]
                      :scheduled  (future-date 10)}]
                    ""
                    "v2"
                    "en-US"
                    15)
          transfer-delete (rest/delete-id
                           "bank"
                           "0.0.0"
                           (user)
                           "transfer"
                           (:id (get transfer 0))
                           "v2"
                           "en-US"
                           15)]
      (is (:message transfer-delete) "Transfer(s) successfully deleted"))))

(deftest rest-patch-id
  (testing "patch id method "
    (let [list [{:amount 400000
                     :name "Arya Stark"
                     :taxId "20.018.183/0001-80"}
                    {:amount 200000
                     :name "John Snow"
                     :taxId "20.018.183/0001-80"}]
          invoices (rest/post-multi
                    "bank"
                    "0.0.0"
                    (user)
                    "invoice"
                    list
                    ""
                    "v2"
                    "en-US"
                    15)
          patchedInvoice (rest/patch-id
                          "bank"
                          "0.0.0"
                          (user)
                          "invoice"
                          {:status "canceled"}
                          (:id (get invoices 0))
                          "v2"
                          "en-US"
                          15)]
      
      (is (:status patchedInvoice) "canceled"))))

(deftest rest-get-raw
  (testing "get raw method "
    (let [item
          (rest/get-raw
           "bank"
           "0.0.0"
           (user)
           "invoice"
           {:limit 1
            :status "created"}
           "v2"
           "en-US"
           15
           "Joker"
           true)]
      (is (= (:status item) 200)))
    ))

(deftest rest-post-raw
  (testing "post raw method "
    (let [invoices {:invoices [{:amount 400000
                                :name "Arya Stark"
                                :taxId "20.018.183/0001-80"}]}
          invoices (rest/post-raw
                    "bank"
                    "0.0.0"
                    (user)
                    "invoice"
                    invoices
                    ""
                    "v2"
                    "en-US"
                    15
                    "Joker"
                    true)]
      (is (= (:status invoices) 200)))))

(deftest rest-patch-raw
  (testing "patch raw method "
    (let [list [{:amount 400000
                                :name "Arya Stark"
                                :taxId "20.018.183/0001-80"}
                               {:amount 200000
                                :name "John Snow"
                                :taxId "20.018.183/0001-80"}]
          invoices (rest/post-multi
                    "bank"
                    "0.0.0"
                    (user)
                    "invoice"
                    list
                    ""
                    "v2"
                    "en-US"
                    15)
          patchedInvoice (rest/patch-raw
                          "bank"
                          "0.0.0"
                          (user)
                          (str "invoice/" (:id (get invoices 0)))
                          {:status "canceled"}
                          {}
                          "v2"
                          "en-US"
                          15
                          "Joker"
                          true)
          ]
      (is (= (:status patchedInvoice) 200))
      )))

(deftest rest-put-raw
  (testing "put raw method "
    (let [profile {:profiles [{:interval "day"
                                :delay 0}]}
          profiles (rest/put-raw
                    "bank"
                    "0.0.0"
                    (user)
                    "split-profile"
                    profile
                    ""
                    "v2"
                    "en-US"
                    15
                    "Joker"
                    true)] 
      (is (= (:status profiles) 200))
      )))

(deftest rest-delete-raw
  (testing "delete raw method "
    (let [transfer (rest/post-multi
                    "bank"
                    "0.0.0"
                    (user)
                    "transfer"
                    [{:amount 1000000
                                  :bankCode "20018183"
                                  :branchCode "2201"
                                  :accountNumber "10000-0"
                                  :taxId "20.018.183/0001-80"
                                  :name "Daenerys Targaryen Stormborn"
                                  :externalId "my-external-id"
                                  :tags ["daenerys" "invoice/1234"]
                                  :scheduled  (future-date 10)}]
                    ""
                    "v2"
                    "en-US"
                    15)
          transfer-delete (rest/delete-raw
                           "bank"
                           "0.0.0"
                           (user)
                           (str "transfer/" (:id (get transfer 0)))
                           "v2"
                           "en-US"
                           15
                           "Joker"
                           false)]
      (is (= (:status transfer-delete) 200)))))
