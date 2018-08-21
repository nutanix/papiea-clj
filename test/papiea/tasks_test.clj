(ns papiea.tasks-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]
            [papiea.engine :as e]
            [papiea.tasks :as t]))

(defn add [entity]
  (assoc entity :status (:spec entity)))

(defn fail [& _]
  (throw+ "not implemnted"))
(comment
  (def transformers {[:test :tasks] {:add-fn add
                                     :add-tasked? true
                                     :del-fn fail
                                     :change-fn fail
                                     :status-fn identity}})

  (-> (e/refresh-status transformers)
      (e/refresh-specs transformers))

  (def e (e/new-papiea-engine))
  (e/start-engine e transformers 5000)
  (e/stop-engine e)

  (def r (e/change-spec! e [:test :tasks] {:metadata {:uuid "11"}
                                           :spec {:name "test"}}))

  (println(t/get-task @r))

  (-> (e/refresh-status transformers)
      (e/refresh-specs transformers))

  (-> (e/refresh-status transformers)
      (e/refresh-specs transformers)
      (e/handleable-diffs (keys transformers)))

  (e/handle-diffs transformers))
