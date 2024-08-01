(ns core-clojure.user.organization
  (:require [core-clojure.user.user :refer [validate]]))

(defn- access-id
  ([id] (access-id id nil))
  ([id workspace-id]
   (if (nil? workspace-id)
     (str "project/"  id)
     (str "project/"  id "/workspace/" workspace-id))))


(defn organization
  ([environment id private-key]
   (organization environment id private-key nil))
  ([environment id private-key workspace-id]
   (let [validated-user (validate private-key environment)]
     {:environment (:environment validated-user)
      :id id
      :private-key (:private-key validated-user)
      :access-id (access-id id workspace-id)
      :workspace-id workspace-id})))

(defn orgaization-replace [organization workspace-id]
   (assoc organization :workspace-id workspace-id))
