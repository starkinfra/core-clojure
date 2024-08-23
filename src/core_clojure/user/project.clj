(ns core-clojure.user.project
  (:require [core-clojure.user.user :refer [validate]]))

(defn- access-id [id]
  (str "project/"  id)
  )

(defn project [environment id private-key]
  (let [validated-user (validate private-key environment)]
    {:environment environment
     :id id
     :private-key private-key
     :access-id (access-id id)
     :type "project"})
  )
