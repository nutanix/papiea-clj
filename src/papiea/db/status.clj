(ns papiea.db.status
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer [$ne]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def conn (mg/connect))
(def db (mg/get-db conn "intent-engine"))

(def entity-status "entity-status")

(defn get-entity [prefix uuid]
  (dissoc (mc/find-one-as-map db entity-status
                              {:metadata.uuid uuid
                               :prefix prefix})
          :prefix :_id))

(defn get-entities
  "Get all entities under a prefix"
  [prefix]
  (map (fn[r] (dissoc r :prefix :_id))
       (mc/find-maps db entity-status {:prefix prefix})))

(defn update-entity-status!
  "Updates or inserts an entity. Does not track spec_version as status
  reflect real world state and is not tied to versioning."
  [prefix entity]
  (dissoc
   (mc/find-and-modify db entity-status 
                       {:_id (:uuid(:metadata entity))
                        :prefix prefix}
                       {:$set {:status (:status entity)
                               :metadata (:metadata entity)
                               :_id (-> entity :metadata :uuid)}}
                       {:return-new true :upsert true})
   :_id :prefix))

(defn clear-entities []
  (mc/remove db entity-status))

