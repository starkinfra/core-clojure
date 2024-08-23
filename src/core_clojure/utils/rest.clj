(ns core-clojure.utils.rest
  (:require [core-clojure.utils.api :refer [build-resource-map endpoint
                                            last-name last-name-plural]]
            [core-clojure.utils.case :refer [json-to-map cast-keys-to-kebab]]
            [core-clojure.utils.request :refer [fetch]]))


(defn get-page
  [host sdk-version user path query api-version language timeout]
  (let [request (atom "")
        resource (last-name-plural path)]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     (endpoint path)
                     ""
                     query
                     api-version
                     language
                     timeout
                     ""
                     true)) 
     {:cursor (:cursor (json-to-map (:content @request)))
      :content (resource (cast-keys-to-kebab (json-to-map (:content @request))))}
    ))

(defn get-stream
  [host sdk-version user path query api-version language timeout]
  (letfn [(fetch-items [query] 
            (let [response (get-page
                            host
                            sdk-version
                            user
                            path
                            (if (:limit query)
                              (assoc query :limit (min (:limit query) 100))
                              query)
                            api-version
                            language
                            timeout)
                  cursor (if (= (:cursor response) "")
                           nil
                          (:cursor response))
                  items (:content response)
                  limit (:limit query)]
              (lazy-seq
               (concat
                items
                (when (or cursor limit)
                  (when (if limit 
                          (>= (- limit 100) 0)
                          true)
                    (fetch-items (assoc query
                                        :cursor cursor
                                        :limit (when limit (min (- limit 100) 100))))))))))]
    (fetch-items query)))

(defn get-id
  [host sdk-version user path id query api-version language timeout]
  (let [request (atom "")
        resource (keyword (last-name path))
        url (str (endpoint path) "/" id)]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     url
                     ""
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (resource (cast-keys-to-kebab (json-to-map (:content @request)))) 
    )
  )

(defn get-content
  "adicionar params para serem passados na query"
  [host sdk-version user path id sub-resource query api-version language timeout]
  (let [request (atom "")
        url (str (endpoint path) "/" id "/" sub-resource)]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     url
                     ""
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (:content @request)
    )
  )

(defn get-sub-resource
  [host sdk-version user path id sub-resource query api-version language timeout]
  (let [request (atom "")
        url (str (endpoint path) "/" id "/" (endpoint sub-resource))]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     url
                     ""
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (cast-keys-to-kebab (json-to-map (:content @request)))
    )
  )

(defn get-public-key
  [host sdk-version user api-version language timeout]
  (let [request (atom "")
        resource :public-keys
        ]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     "public-key"
                     ""
                     {:limit 1}
                     api-version
                     language
                     timeout
                     ""
                     true))
    (:content (get (resource (cast-keys-to-kebab (json-to-map (:content @request)))) 0))
    )
  )

(defn post
  [host sdk-version user resource-name payload query api-version language timeout]
  (let [request (atom "")]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :post
                     resource-name
                     payload
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (cast-keys-to-kebab (json-to-map (:content @request)))
    )
  )

(defn post-multi
  [host sdk-version user resource-name payload query api-version language timeout]
  (let [request (atom "")
        resource (last-name-plural resource-name)
        entities-map (build-resource-map resource payload)]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :post
                     resource-name
                     entities-map
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (resource (cast-keys-to-kebab (json-to-map (:content @request)))))
  )

(defn post-single 
  [host sdk-version user resource-name payload query api-version language timeout]
  (let [request (atom "")
        resource (keyword (last-name resource-name))]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :post
                     resource-name
                     payload
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (resource (cast-keys-to-kebab (json-to-map (:content @request)))))
  )

(defn post-sub-resource
  [host sdk-version user resource-name id sub-resource payload  query api-version language timeout]
  (let [request (atom "")
        url (str resource-name "/" id "/" (endpoint sub-resource))]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :post
                     url
                     payload
                     query
                     api-version
                     language
                     timeout
                     ""
                     true))
    (cast-keys-to-kebab (json-to-map (:content @request))))
  )

(defn delete-id [host sdk-version user path id api-version language timeout]
  (let [request (atom "")
        url (str path "/" id "/")] 
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :delete
                     url
                     ""
                     ""
                     api-version
                     language
                     timeout
                     ""
                     true)) 
    (cast-keys-to-kebab (json-to-map (:content @request))))
  )

(defn patch-id [host sdk-version user path payload id api-version language timeout ]
  (let [request (atom "")
        url (str path "/" id)
        resource (keyword (last-name path))]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :patch
                     url
                     payload
                     ""
                     api-version
                     language
                     timeout
                     ""
                     true))
    (resource (cast-keys-to-kebab (json-to-map (:content @request)))))
  )

(defn get-raw
  [host sdk-version user path query api-version language timeout prefix throw-error]
  (let [request (atom "")]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :get
                     path
                     {}
                     query
                     api-version
                     language
                     timeout
                     prefix
                     throw-error))
    {:status (:status @request) :content (cast-keys-to-kebab (json-to-map (:content @request)))}
    )
  )

(defn post-raw
  [host sdk-version user resource-name payload query api-version language timeout prefix throw-error]
  (let [request (atom "")]
    (reset! request (fetch
                     host
                     sdk-version
                     user
                     :post
                     resource-name
                     payload
                     query
                     api-version
                     language
                     timeout
                     prefix
                     throw-error))
    {:status (:status @request) :content (cast-keys-to-kebab (json-to-map (:content @request)))}
    )
  )

(defn patch-raw 
  [host sdk-version user path payload query api-version language timeout prefix throw-error]
  (let [request (atom "")]
     (reset! request (fetch
                      host
                      sdk-version
                      user
                      :patch
                      path
                      payload
                      query
                      api-version
                      language
                      timeout
                      prefix
                      throw-error))
     {:status (:status @request) :content (cast-keys-to-kebab (json-to-map (:content @request)))}
    )
  )

(defn put-raw 
  [host sdk-version user path payload query api-version language timeout prefix throw-error]
  (let [request (atom "")]
     (reset! request (fetch
                      host
                      sdk-version
                      user
                      :put
                      path
                      payload
                      query
                      api-version
                      language
                      timeout
                      prefix
                      throw-error))
     {:status (:status @request) :content (cast-keys-to-kebab (json-to-map (:content @request)))}
    )
  )

(defn delete-raw 
  [host sdk-version user path api-version language timeout prefix throw-error]
  (let [request (atom "")]
     (reset! request (fetch
                      host
                      sdk-version
                      user
                      :delete
                      path
                      {}
                      {}
                      api-version
                      language
                      timeout
                      prefix
                      throw-error))
     {:status (:status @request) :content (cast-keys-to-kebab (json-to-map (:content @request)))}
    )
  )
