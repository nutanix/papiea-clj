(ns papiea.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [papiea.tasks :as task]
            [papiea.specs]
            [papiea.engine :as e]
            [clojure.spec.alpha :as s]
            [papiea.core :as c]
            [tracks.core :as t]
            [spec-tools.core :as st]
            [spec-tools.transform :as stt]))

(def spec-no-remove-keys
  (-> compojure.api.coercion.spec/default-options
      (assoc-in
        [:body :formats "application/json"]
        (st/type-transformer
         {:decoders stt/json-type-decoders}))
      compojure.api.coercion.spec/create-coercion))

(defonce engines (atom {}))

(use '[clojure.tools.nrepl.server :only (start-server stop-server)])
(defonce server (start-server :port 7888))


(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Papiea example"
                    :description ""}
             :tags [{:name "api", :description "some apis"}]}}}
    
    (context "/papiea/api/v1" []
      :tags ["engine"]
      :coercion spec-no-remove-keys
      
      (POST "/start-engine" []
            :summary "Starts a papiea engine"
            :description "If contains uuid, it starts the engine with that uuid with the given timeout and transformers. If does not contain uuid, creates a new one and starts it. Returns the created or given uuid"
            :body [req :papiea.engine/start-engine]
            :return :papiea.entity/uuid
            (let [{:keys [uuid transformers timeout]} req
                  [uuid engine] (if uuid [uuid (get @engines uuid)]
                                    (let [uuid (c/uuid)]
                                      [uuid (swap! engines assoc uuid (e/new-papiea-engine))]))]
              (e/start-engine engine timeout transformers)
              (ok uuid)))

      (POST "/stop-engine" []
            :summary "Stops a papiea engine"
            :body [req :papiea.engine/stop-engine]
            :return :papiea.entity/uuid
            (let [{:keys [uuid]} req]
              (if-let [engine (get @engines uuid)]
                (do (e/stop-engine engine)
                    (ok uuid))
                (bad-request "Engine not found!"))))

      (POST "/change-spec" []
            :summary "post a spec change to the engine"
            :body [req :papiea.engine/change-spec]
            :return :papiea.engine/change-spec-response
            (let [{:keys [uuid prefix entity]} req
                  e-id (or (-> entity :metadata :uuid) (do
                                                         (println "INFO: Papiea created a uuid for request" req)
                                                         (c/uuid)))
                  spec-ver (or (-> entity :metadata :uuid) 0)]
              (if-let [engine (get @engines uuid)]
                (ok (e/change-spec! engine prefix entity))
                (bad-request "Engine not found!"))
              )
            )

      ;; (s/describe :papiea.task/poll-request)
      ;; (s/describe :papiea.task/task_uuid_list)
      (context "/tasks" []
        :tags ["tasks"]
        ;;:allow-empty-input-on-decode? true
        (POST "/poll" []
          :body [req any? #_:papiea.task/poll-request]
          :return :papiea.task/poll-response
          (let [{:keys [poll_timeout_seconds task_uuid_list]} req
                now (System/currentTimeMillis)
                e (map (fn[x] [x (task/get-task x)]) task_uuid_list)]
            (let [errors (filter (fn[[x t]] (nil? t)) e)]
              (if (pos? (count errors))
                (bad-request {:api_version "1.0"
                              :code 400
                              :kind "task"
                              :message_list (map (fn[e] {:message (format "Invalud UUID %s" (first e))
                                                        :reason "INVALID_UUID"}) errors)
                              :state "ERROR"})
                (ok (loop []
                      (let [t (> (* 1000 poll_timeout_seconds)
                                 (- (System/currentTimeMillis) now))
                            e (map task/get-task task_uuid_list)
                            d (every? (fn[{:keys [status]}] (#{"COMPLETED" :completed} status)) e)]
                        (if d {:has_poll_timed_out t
                               :entities e}
                            (if t (do (Thread/sleep 100) (recur))
                                {:has_poll_timed_out t
                                 :entities e})))))))))

        (GET "/list" []
          :return :papiea.task/list-response
          (ok {:entities (task/get-all-tasks)}))

        (GET "/:id" [id]
          :return :papiea.task/entity
          (if-let [task (task/get-task id)]
            (ok (assoc task :api_version "1.0"))
            (bad-request {:api_version "1.0"
                          :code 400
                          :kind "task"
                          :message_list [{:message (format "Invalud UUID %s" id)
                                          :reason "INVALID_UUID"}]
                          :state "ERROR"})))))))



