(ns papiea.specs
  (:require [clojure.spec.alpha :as s]))

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

(s/def :papiea/api-fn (s/or :direct fn? :rest string?))
