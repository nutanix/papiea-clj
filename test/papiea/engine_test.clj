(ns papiea.engine-test
  (:require [clojure.test :refer :all]
            [papiea.engine :as c]))

(deftest merge-entities-status
  (testing "merging the same data should make no difference"
    (let [old [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "started"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}]]
      (is (= (into #{} old) (into #{} (c/merge-entities-status old old))))))

  (testing "merging data where status has changed value should be reflected in the merge"
    (let [old [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "started"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}]
          new [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "CHANGED"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}]]
      (is (= (into #{} new) (into #{} (c/merge-entities-status old new)))))
    )

  (testing "merging data where status has added a new item should be reflected in the merge"
    (let [old [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "started"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}]
          new [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "CHANGED"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}
               {:metadata {:uuid "44"}
                :status   {:name  "new item"
                           :state "stopped"}}]]
      (is (= (into #{} new) (into #{} (c/merge-entities-status old new)))))
    )

  (testing "merging data where status has removed an item. The resulting merge should have its status be nil"
    (let [old [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "started"}}
               {:metadata {:uuid "15"}
                :status   {:name  "another"
                           :state "started"}}]
          new [{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "CHANGED"}}]]
      (is (= #{{:metadata {:uuid "12"}
                :status   {:name  "test1"
                           :state "CHANGED"}}
               {:metadata {:uuid "15"}
                :status nil}} (into #{} (c/merge-entities-status old new)))))
    )

  )


