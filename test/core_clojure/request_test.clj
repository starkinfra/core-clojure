(ns core-clojure.request-test
  (:require [clojure.test :refer [deftest is testing]]
            [core-clojure.user :refer [user]] 
            [core-clojure.utils.request :refer [fetch]]))


(deftest fetch-get
  (testing "fetch method"
    (let [request (fetch
                    "bank"
                    "0.0.0"
                    (user)
                    :get
                    "invoice"
                    {}
                    {:limit 1}
                    "v2"
                    "en-US"
                    15
                    ""
                    false)] 
    (is (and (contains? request :status)
             (contains? request :content))))))
