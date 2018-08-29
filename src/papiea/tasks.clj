(ns papiea.tasks
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [papiea.core :refer [uuid]]
            [monger.operators :refer [$in]]
            [clojure.spec.alpha :as s]
            [com.rpl.specter :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]))

(def conn (mg/connect))
(def db (mg/get-db conn "intent-engine"))

(def tasks "tasks")

(defn register-new-task [entity]
  (let [now  (java.util.Date.)
        uuid (uuid)
        d    {:_id                   uuid
              :status                "PENDING"
              :uuid                  uuid
              :creation_time         (str now)
              :start_time            (str now)
              :last_update_time      (str now)
              :percentage_complete   0
              :entity_reference_list [{:kind (or (some-> entity :metadata :kind) "")
                                       :uuid (-> entity :metadata :uuid)
                                       :name (or (some-> entity :spec :name) "")}]}]
    (mc/insert db tasks d)
    d))

(defn update-task
  ([uuid] (update-task uuid {}))
  ([uuid task]
   (let [uuid (if (map? uuid) (-> uuid :metadata :uuid) uuid)]
     (if-let [p (mc/find-map-by-id db tasks uuid)]
       (let [now (java.util.Date.)
             d (merge p
                      task
                      {:last_update_time (str now)})]
         (mc/update db tasks {:_id uuid} d)
         d)))))

(defn get-task [uuid]
  (dissoc (mc/find-map-by-id db tasks uuid) :_id))

(defn get-all-tasks []
  (map (fn[x] (dissoc x :_id)) (mc/find-maps db tasks)))

(defn clear-tasks[]
  (mc/remove db tasks))

#_(def d (register-new-task {:metadata {:uuid "1"
                                     :kind "something"}
                          :spec {:name "test"}}))

;;(println (:uuid(first (mc/find-maps db tasks))))

;;(update-task (:uuid d) {:operation_type "asd"})

;;(mc/remove db tasks)
