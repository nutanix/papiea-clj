(ns papiea.specs
  (:require [clojure.spec.alpha :as s]))

(s/def :papiea/entity map?)
(s/def :papiea.entity/kind string?)
(s/def :papiea.entity/name string?)
(s/def :papiea.entity/uuid string?)
(s/def :papiea.entity/status map?)
(s/def :papiea.entity/metadata (s/keys :req-un [:entity/uuid]))
(s/def :papiea.entity.list/status (s/keys :req-un [:entity/metadata :entity/status]))
(s/def :papiea.entity.list/statuses (s/coll-of :entity.list/status))

(s/def :papiea.entity/spec map?)
(s/def :papiea.entity.list/spec (s/keys :req-un [:papiea.entity/metadata :papiea.entity/spec]))
(s/def :papiea.entity.list/specs (s/coll-of :papiea.entity.list/spec))



(s/def :papiea.provider.ok/status #{:ok})
(s/def :papiea.provider.failed/status #{:failed})
(s/def :papiea.provider.failed/error string?)
(s/def :papiea.provider.failed/entity map?)
(s/def :papiea.provider.failed/cause any?)
(s/def :papiea.provider/success-response (s/keys :req-un [:papiea.provider.ok/status]))
(s/def :papiea.provider/failure-response (s/keys :req-un [:papiea.provider.failed/status
                                                        :papiea.provider.failed/error]
                                               :opt-un [:papiea.provider.failed/entity
                                                        :papiea.provider.failed/cause]))
(s/def :papiea.provider/response (s/or :success :papiea.provider/success-response
                                     :failure :papiea.provider/failure-response))

(s/def :papiea.provider/stage (s/or :keyword keyword?
                                  :string string?))


(s/def :papiea.recipe/version (s/or :keyword keyword? :string string?))
(s/def :papiea.recipe/stages (s/coll-of :papiea.provider/stage))
(s/def :papiea/recipe (s/keys :req-un [:papiea.recipe/version :papiea.recipe/stages]))

(s/def :papiea/api-fn (s/or :direct ifn? :rest string?))


(s/def :papiea.engine/prefix (s/or :keywords (s/coll-of keyword?)
                                   :strings  (s/coll-of string?)))

(s/def :papiea.engine/fn-or-staged (s/or :single-step :papiea/api-fn
                                         :staged (s/coll-of :papiea/api-fn :count 2)))

(s/def :papiea.engine/add-fn :papiea.engine/fn-or-staged)
(s/def :papiea.engine/del-fn :papiea.engine/fn-or-staged)
(s/def :papiea.engine/change-fn :papiea.engine/fn-or-staged)
(s/def :papiea.engine/status-fn :papiea.engine/fn-or-staged)

(s/def :papiea.engine/add-tasked? boolean?)
(s/def :papiea.engine/del-tasked? boolean?)
(s/def :papiea.engine/change-tasked? boolean?)


(s/def :papiea.engine/entity-crud (s/keys :req-un [:papiea.engine/add-fn
                                                   :papiea.engine/del-fn
                                                   :papiea.engine/change-fn
                                                   :papiea.engine/status-fn]
                                          :opt-un [:papiea.engine/add-tasked?
                                                   :papiea.engine/del-tasked?
                                                   :papiea.engine/change-tasked?]))

(s/def :papiea.engine/transformers (s/map-of :papiea.engine/prefix :papiea.engine/entity-crud))
(s/def :papiea.engine/timeout integer?)
(s/def :papiea.engine/start-engine (s/keys :req-un [:papiea.engine/transformers
                                                    :papiea.engine/timeout]
                                           :opt-un [:papiea.entity/uuid]))
(s/def :papiea.engine/stop-engine (s/keys :req-un [:papiea.entity/uuid]))

(s/def :papiea.engine/change-spec (s/keys :req-un [:papiea.entity/uuid :papiea.engine/prefix :papiea/entity]))
(s/def :papiea.engine/tasked-response (s/keys :req-un []))
(s/def :papiea.engine/synced-response (s/keys :req-un []))
(s/def :papiea.engine/change-spec-response (s/or :task :papiea.engine/tasked-response
                                                 :sync :papiea.engine/synced-response))


(s/def :papiea.task/time string?)
(s/def :papiea.task/status string?)
(s/def :papiea.task/start_time :papiea.task/time)
(s/def :papiea.task/creation_time :papiea.task/time)
(s/def :papiea.task/completion_time :papiea.task/time)
(s/def :papiea.task/last_update_time :papiea.task/time)
(s/def :papiea.task/logical_timestamp integer?)
(s/def :papiea.task/percentage_complete integer?)
(s/def :papiea.task/error_detail string?)
(s/def :papiea.task/uuid :papiea.entity/uuid)
(s/def :papiea.task/progress_message string?)
(s/def :papiea.task/operation_type string?)
(s/def :papiea.task/error_code string?)
(s/def :papiea.task/entity_reference (s/keys :req-un [:papiea.entity/kind :papiea.entity/name :papiea.entity/uuid]))
(s/def :papiea.task/subtask_reference (s/keys :req-un [:papiea.entity/kind :papiea.entity/name :papiea.entity/uuid]))
(s/def :papiea.task/parent_task_reference (s/keys :req-un [:papiea.entity/kind :papiea.entity/name :papiea.entity/uuid]))
(s/def :papiea.task/subtask_reference_list (s/coll-of :papiea.task/subtask_reference))
(s/def :papiea.task/entity_reference_list (s/coll-of :papiea.task/entity_reference))

(s/def :papiea.task/entity (s/keys :opt-un [:papiea.task/status
                                            :papiea.task/last_update_time
                                            :papiea.task/logical_timestamp
                                            :papiea.task/start_time
                                            :papiea.task/creation_time
                                            :papiea.task/completion_time
                                            :papiea.task/percentage_complete
                                            :papiea.task/parent_task_reference
                                            :papiea.task/error_detail
                                            :papiea.task/entity_reference_list
                                            :papiea.task/subtask_reference_list
                                            :papiea.task/progress_message
                                            :papiea.task/operation_type
                                            :papiea.task/error_code]
                                   :req-un [:papiea.task/uuid]))

(s/def :papiea.task/poll_timeout_seconds integer?)
(s/def :papiea.task/task_uuid_list (s/coll-of string?))
(s/def :papiea.task/poll-request (s/keys :req-un [:papiea.task/poll_timeout_seconds :papiea.task/task_uuid_list]))

(s/def :papiea.task/entities (s/coll-of :papiea.task/entity))
(s/def :papiea.task/has_poll_timed_out boolean?)
(s/def :papiea.task/poll-response (s/keys :req-un [:papiea.task/entities :papiea.task/has_poll_timed_out]))

(s/def :papiea.task/list-response (s/keys :req-un [:papiea.task/entities]))

(s/def :papiea.err/code integer?) 
(s/def :papiea.entity/kind string?) 
(s/def :papiea.err/message_list (s/coll-of string?)) 
(s/def :papiea.err/state string?)
(s/def :papiea.err/bad-request (s/keys :req-un [:papiea.err/code :papiea.entity/kind :papiea.err/message_list :papiea.err/state]))
