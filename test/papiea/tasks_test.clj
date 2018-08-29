(ns papiea.tasks-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]
            [papiea.engine :as e]
            [papiea.staged :as staged]
            [papiea.db.spec :as spdb]
            [papiea.db.status :as stdb]
            [papiea.tasks :as task]
            [papiea.tasks :as t]))

(require '[clj-async-profiler.core :as prof])

(set! *warn-on-reflection* true)

(defn add [entity]
  (assoc entity :status (:spec entity)))

(defn del [entity] {:metadata (:metadata entity)})

(defn fail [& _]
  (throw+ "not implemnted"))

(defn inc-version [living-objects id]
  (swap! living-objects assoc id (inc(get @living-objects id 0))))

(defn generate-test
  ([max-ops] (generate-test max-ops [:add :change :dell] #{} 0 0))
  ([max-ops ops] (generate-test max-ops ops #{} 0 0))
  ([max-ops ops living-objects max-id min-spec-version ]
   (let [living-objects (atom (into {} (map (fn[o] [o min-spec-version]) living-objects)))
         max-id (atom max-id)
         gen-add (fn[id spec_version name ] {:metadata {:uuid (str id)
                                                       :spec_version spec_version}
                                            :spec {:name (str name \- id)}})
         gen-del (fn[id _] {:metadata {:uuid (str id)}})
         
         gen-change (fn[id spec new-name] (gen-add id spec (str new-name \- (rand-int (* 2 max-ops)))))]
     (reduce (fn[o op-i]
               (let [op (rand-nth ops)]
                 (conj
                  o
                  (condp = op
                    :add (let [id (swap! max-id inc)]
                           (inc-version living-objects id)
                           (gen-add id 1 "added"))
                    :change  (if (empty? @living-objects)
                               (let [id (swap! max-id inc)] ;; if cant change, add
                                 (println "Cant change!!")
                                 (inc-version living-objects id)
                                 (gen-add id 1 "added"))
                               (let [[id spec-version] (rand-nth (vec @living-objects))]
                                 (inc-version living-objects id)
                                 (gen-change id spec-version "changed")))
                    :del (if (empty? @living-objects)
                           (let [id (swap! max-id inc)] ;; if cant change, add
                             (println "Cant del!!")
                             (inc-version living-objects id)
                             (gen-add id 1 "added"))
                           (let [id (rand-nth (vec @living-objects))]
                             (swap! living-objects dissoc id)
                             (gen-del id "del")))
                    ))
                 
                 ))
             []
             (range max-ops))

     ))

  )


(defn cleanup []
  (do(println "Clean start - removing all previously known entities")
     (spdb/clear-entities)
     (stdb/clear-entities)
     (task/clear-tasks)
     (staged/clear-stages))
  )

(comment
  (cleanup)

  (def creates (generate-test 1000 [:add]))

  (def tests  (generate-test 2000 [:change] (into #{} (map inc (range 1000))) 1000 2)
    )

  (drop 100 tests)
  ;; -L1234:*:54321

  (prof/profile-for 2 {}) ; Run profiler for 10 seconds


  (do
    (prof/start {:interval 99})

    (cleanup)
    
    (

     (
      (doseq [test creates]
        (e/change-spec! e [:test :tasks] test))
      
      (time (doseq [test tests]
              ;;(println test)
              (e/change-spec! e [:test :tasks] test)))


      ))

    (e/change-spec! e [:test :tasks] (first (rest tests)))
    
    #_(def r(let [a (doall (time(map (fn[test] (e/change-spec! e [:test :tasks] test)) tests)))]
              ;;(doall(map (fn[x] (deref x 5 :timeout)) a))
              ))
    
    (prof/stop {}))

  (prof/serve-files 54321)

  (time
   (dotimes [r 1]
     (doseq [i (range 1000)]
       (spdb/get-entity [:test :tasks] (str i))
       (stdb/get-entity [:test :tasks] (str i)))))

  (time
   (dotimes [r 1]
     (spdb/get-entities [:test]))))

(comment
  (def transformers {[:test :tasks] {:add-fn add
                                     :add-tasked? true
                                     :del-fn del
                                     :del-tasked? true
                                     :change-fn add
                                     :change-tasked? true
                                     :status-fn identity}})

  (time(count(get-in (-> (e/refresh-status transformers)
                         (e/refresh-specs transformers))
                     [:test :tasks])))

  (def e (e/new-papiea-engine))
  (e/start-engine e transformers 1000)
  (e/stop-engine e)

  (count @(:change-watch e))
  

  (papiea.core/current)
  
  (dotimes [i 100]
    (e/change-spec! e [:test :tasks] {:metadata {:uuid (str i)}
                                      :spec {:name (str "test " i)}})
    )

  (dotimes [i 100]
    (e/change-spec! e [:test :tasks] {:metadata {:uuid (str i)}})
    )

  
  (def r (e/change-spec! e [:test :tasks] {:metadata {:uuid "17"}
                                           :spec {:name "test"}}))

  (println(t/get-task @r))

  (-> (e/refresh-status transformers)
      (e/refresh-specs transformers))

  (-> (e/refresh-status transformers)
      (e/refresh-specs transformers)
      (e/handleable-diffs (keys transformers)))

  (e/handle-diffs transformers))

(- 2212 2061)

;;{
;; "transformers": {["nusim", "images"] {"add-fn" "http:/"}},
;; "timeout": 5000,
;;}
