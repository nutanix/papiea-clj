(ns papiea.staged
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer [$in]]
            [clojure.spec.alpha :as s]
            [com.rpl.specter :refer :all]
            [papiea.core :refer [call-api]]
            [slingshot.slingshot :refer [throw+ try+]]))

(def conn (mg/connect))
(def db (mg/get-db conn "intent-engine"))

(def staged "staged-execution")

(defn get-entity-at-stage [entity stage]
  (dissoc (mc/find-one-as-map db staged {:_id (-> entity :metadata :uuid) :status.stages.completed {$in [stage]}}) :_id))

(defn start-staged-execution [entity version]
  (let [e (merge entity {:_id (-> entity :metadata :uuid) :status {:stages {:version version
                                                                            :completed [:init]}}})]
    (mc/insert db staged e)
    e))

(defn update-staged-execution [entity stage]
  (let [entity (setval [:status :stages :completed END] [stage] entity)]
    (mc/update-by-id db staged (-> entity :metadata :uuid) entity)
    entity))

(defn execute-stage
  "`resolve-fn` function must accept a hash-map with two keys :stage and :entity"
  [resolve-fn stage entity]
  (or (get-entity-at-stage entity stage)
      (try+
       (def entity entity)
       (let [stage-result (call-api resolve-fn {:stage  stage
                                                :entity entity})]
         (update-staged-execution stage-result stage))
       (catch Object e
         (throw+)))))

(defn clean-stages-if-done [stages entity])

(defn staged-execution [provider-fn resolve-fn]
  (fn[entity]
    (let [existing                 (get-entity-at-stage entity :init)
          {:keys [version stages]} (call-api provider-fn (if existing
                                                           (setval [:status :stages :version]
                                                                   (-> existing :status :stages :version)
                                                                   entity)
                                                           entity))
          entity                   (or existing (start-staged-execution entity version))
          result                   (reduce (fn[e stage] (execute-stage resolve-fn stage e)) entity stages)
          entity-id                (-> entity :metadata :uuid)]
      (when (= (inc(count stages)) (count (some-> (mc/find-one-as-map db staged {:_id entity-id})
                                                  :status :stages :completed)))
        (mc/remove-by-id db staged entity-id))
      result
      )))

(defn get-stage-keyword [s]
  (let [[kind stage] (s/conform :core.provider/stage s)]
    (condp = kind
      :string (keyword stage)
      :keyword stage)))

(mc/find-maps db staged)


(defn clear-entities []
  (mc/remove db staged))
