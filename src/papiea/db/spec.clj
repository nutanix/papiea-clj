(ns papiea.db.spec
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer [$ne]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def conn (mg/connect))
(def db (mg/get-db conn "intent-engine"))

(def entity-spec "entity-spec")

(defn get-entity [prefix uuid]
  (dissoc (mc/find-one-as-map db entity-spec
                              {:metadata.uuid uuid
                               :prefix prefix})
          :prefix :_id))

(defn get-entities
  "Get all entities under a prefix"
  [prefix]
  (map (fn[r] (dissoc r :prefix :_id))
       (mc/find-maps db entity-spec {:prefix prefix})))

(defn swap-entity-spec!
  "Updates or inserts an entity. If the entity is new it should have its
  spec_version be -1 so after insertion the first one will be zero"
  [prefix entity]
  (try+
   (if (:spec entity)
     (dissoc
      (mc/find-and-modify db entity-spec
                          {:metadata (:metadata entity)
                           :prefix prefix
                           :spec {$ne (:spec entity)} ;; Ensure that we actually made a change!
                           }
                          {:$inc {:metadata.spec_version 1}
                           :$set {:spec (:spec entity)
                                  :_id  (-> entity :metadata :uuid)}}
                          {:return-new true :upsert true})
      :_id :_prefix)
      ;; throw an error of user is trying to remove an item that does not exists. This should be made atomic
     (if (mc/find-by-id db entity-spec (-> entity :metadata :uuid))
       (mc/remove-by-id db entity-spec (-> entity :metadata :uuid))
       (throw+ "Trying to remove an Item that does not exist")))
   (catch Object e
     (throw+ {:entity entity
              :cause e} "Cant insert or update entity. Check that you are
                      using the latest spec_version. Another cause for
                      this error is if you are trying to change and
                      existing entity without actually making any
                      changes"))))

(defn clear-entities []
  (mc/remove db entity-spec))

(defn remove-entity [prefix uuid]
  (mc/remove db entity-spec {:prefix prefix :_id uuid}))
