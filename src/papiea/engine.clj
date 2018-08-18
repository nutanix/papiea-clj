(ns papiea.engine
  (:require [com.rpl.specter :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as st]
            [clojure.set :as set]
            [papiea.specs]
            [tracks.core :as t]
            [papiea.core :refer [call-api fixed-rate ->timer]]
            [papiea.db.spec :as spdb]
            [papiea.db.status :as stdb]))
;;(map #(ns-unmap *ns* %) (keys (ns-interns *ns*)))

(defn state-settled? [entity]
  (= (:spec entity) (select-keys (:status entity) (keys (:spec entity)))))

(defn handleable-diffs-for [state prefix]
  (let [prefix-diffs (select (into prefix [ALL (complement state-settled?)]) state)]
    (map (fn[{:keys [spec status] :as w}]
           (cond
             (and spec (not status)) {:added w :prefix prefix}
             (and status (not spec)) {:removed w :prefix prefix}
             (and spec status)       {:changed w :prefix prefix}))
         prefix-diffs)))


(defn handleable-diffs [state prefixes]
  (remove (comp empty? :diffs)
          (map (fn[prefix] {:prefix prefix
                           :diffs (handleable-diffs-for state prefix)}) prefixes)))


(defn merge-entities-part [old-entities new-entities part]
  (let [new-entities-map (reduce (fn[o n] (assoc o (-> n :metadata :uuid) n)) {} new-entities)]
    (loop [old-entities old-entities new-entities-map new-entities-map merged-entities []]
      (cond (and (empty? old-entities)
                 (empty? new-entities-map)) merged-entities 
            (empty? new-entities-map)       (into merged-entities (transform [ALL] (fn[x] (dissoc x part)) old-entities))
            (empty? old-entities)           (into merged-entities (vals new-entities-map))
            :else                           (let [old (peek old-entities)
                                                  id  (-> old :metadata :uuid)
                                                  new (get new-entities-map id)]
                                              (recur (pop old-entities)
                                                     (dissoc new-entities-map id)
                                                     (conj merged-entities (assoc old part (get new part)))))))))

(defn-spec merge-entities-status :papiea.entity.list/statuses
  "Merges two lists of entities's status, based on uuid "
  [old-entities :papiea.entity.list/statuses new-entities :papiea.entity.list/statuses]
  (merge-entities-part old-entities new-entities :status))

(defn-spec merge-entities-specs :papiea.entity.list/specs
  [old-entities :papiea.entity.list/specs new-entities :papiea.entity.list/specs]
  (merge-entities-part (merge-entities-part old-entities new-entities :spec)
                       new-entities :metadata))

(defn ensure-entity-map [m ks]
  (if (empty? ks) m
      (let [[k & ks] ks]
        (assoc m k (ensure-entity-map (get m k (if (empty? ks) [] {})) ks)))))

(defn refresh-status
  "Based on the registered transformers, ask each one to supply its view of its entities status"
  ([transformers] (refresh-status {} transformers))
  ([state transformers]
   (reduce (fn[o [prefix {:keys [status-fn]}]]
             (if status-fn
               (transform prefix (fn[x] (let [db-statuses (stdb/get-entities prefix)
                                             statuses    (call-api status-fn db-statuses)
                                             removed     (map (fn[e] (dissoc e :status :spec))
                                                              (set/difference (set db-statuses) (set statuses)))]
                                         (doseq [entity (concat statuses removed)]
                                           (stdb/update-entity-status! prefix entity))                                       
                                         (merge-entities-status x statuses)))
                          (ensure-entity-map o prefix))
               (do(println "Error: Cant refresh" prefix " - no `status-fn` defined. Unsafely ignoring..")
                  (ensure-entity-map o prefix))
               ))
           state
           transformers)))

(declare prefix) ;; bug in cider while debugging..
(defn refresh-specs
  "Based on the registered transformers, ask each entity type for its entities specs in our specs-db"
  ([transformers] (refresh-specs {} transformers))
  ([state transformers]
   (reduce (fn[o [prefix _]]
             (transform prefix (fn[x] (merge-entities-specs x (spdb/get-entities prefix))) (ensure-entity-map o prefix)))
           state
           transformers)))

(defn previous-spec-version [entity]
  (transform [:metadata :spec_version] dec entity))

(defn unspec-version [entity]
  (select-keys (assoc (setval [:metadata :spec_version] nil entity)
                      :spec (:spec entity))
               [:metadata :spec]))

(defn ensure-spec-version
  ([prefix entity default-value]
   (if (-> entity :metadata :spec_version)
     entity
     (let [e (spdb/get-entity prefix (-> entity :metadata :uuid))
           spec-ver (if e (-> e :metadata :spec_version) default-value)]
       (setval [:metadata :spec_version] spec-ver entity)))))

(defn insert-spec-change!
  "Inserts a spec change. Every spec change induces an increment in [:metadata :spec_version].
   Most secure method is to supply the right :spec_version this change intends to modify.
   If none is provided, the system queries for the last one and auto-assigns it. Using this
   default mechanism might cause a race condition"
  [prefix entity]
  (spdb/swap-entity-spec! prefix (ensure-spec-version prefix entity -1)))

(defn turn-spec-to-status [transformers prefix success-fn failed-fn {:keys [added removed changed]}]
  (let [{:keys [add-fn del-fn change-fn]} (get transformers prefix)]
    (let [[modify data op] (cond added   [add-fn added :added]
                                 removed [del-fn removed :removed]
                                 changed [change-fn changed :changed])
          r      (try+ (modify data)
                       (catch Object o
                         {:status :failed
                          :error o}))]
      (if (= :failed (:status r))
        (failed-fn op data)
        (do (stdb/update-entity-status! prefix r) ;; Save the state
            (success-fn op data))
        ))))

(defn handle-diffs
  "apply the diffs"
  ([transformers] (handle-diffs transformers
                                (fn[op data] (println "Success: " op data))
                                (fn[op data] (println "Failed: " op data))))
  ([transformers success-fn failed-fn]
   (let [diffs (-> (refresh-specs transformers)
                   (refresh-status transformers)
                   (handleable-diffs (keys transformers)))]
     (doseq [{:keys [prefix diffs]} diffs
             diff diffs]
       (turn-spec-to-status transformers prefix success-fn failed-fn diff)))))

(defn change-succeeded [change-watch]
  (fn[op entity]
    (let [previous-entity (unspec-version (dissoc entity :status))]
      (println "watch:"  @change-watch)
      (println "lookup:" previous-entity)
      (when-let [done (get @change-watch previous-entity)]
        (swap! change-watch dissoc previous-entity)
        (deliver done {:status :ok})))))

(defn change-failed [change-watch]
  (fn[op entity]
    (let [previous-entity (unspec-version (dissoc entity :status))]
      (when-let [done (get @change-watch previous-entity)]
        (swap! change-watch dissoc previous-entity)
        (deliver done {:status :failed})))))

;; We model the async call as a watch on an atom. The watch is triggered every time the atom
;; value is changed, causing handle-diffs to be called with the registered transformers


(defprotocol PapieaEngine 
  (start-engine [this timeout transformers])
  (stop-engine [this])
  (notify-change [this])
  (change-spec! [this prefix entity]))

(defrecord DefaultEngine [change-watch handle-diff-notify started state]
  PapieaEngine
  (start-engine [this transformers timeout]
    (add-watch handle-diff-notify :process-diffs
               (fn[key a o n]
                 (println n "Looking for diffs")
                 (handle-diffs transformers
                               (change-succeeded change-watch)
                               (change-failed change-watch))))

    (when (compare-and-set! started false true)
      (println "Starting engine...")
      (swap! state assoc
             :tranformers transformers
             :interval-notify (fixed-rate (partial notify-change this) (->timer) 5000)))
    this)
  
  (stop-engine [this]
    (when (compare-and-set! started true false)
      (println "Stopping engine..")
      ((:interval-notify @state)) ;; stop the timer
      (swap! state dissoc :interval-notify :tranformers))
    this)
  
  (notify-change [this]
    ;; This function simply changes the value of the atom, causing the function to execute
    (swap! handle-diff-notify inc)
    this)

  (change-spec! [this prefix entity]
    (let [done          (promise)
          speced-entity (ensure-spec-version prefix entity -1)]
      (try+
       (swap! change-watch assoc (unspec-version speced-entity) done)
       (insert-spec-change! prefix speced-entity)
       (notify-change this)
       done
       (catch Object e
         (swap! change-watch dissoc speced-entity)
         ;;(println e)
         ;;(println "Throwing??")
         (throw+ (merge {:status :failure} e))))))
  )

(defn new-papiea-engine []
  (println "Created new engine")
  (->DefaultEngine (atom {}) (atom 0) (atom false) (atom {})))

