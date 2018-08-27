(ns papiea.core
  (:require [papiea.specs]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-http.client :as rest]
            [clojure.spec.alpha :as s]
            [cheshire.core :as json]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn ->timer [] (java.util.Timer.))

(defn fixed-rate 
  ([f per] (fixed-rate f (->timer) 0 per))
  ([f timer per] (fixed-rate f timer 0 per))
  ([f timer dlay per] 
    (let [tt (proxy [java.util.TimerTask] [] (run [] (f)))]
      (.scheduleAtFixedRate timer tt dlay per)
      #(.cancel tt))))


(defn call-api
  "calls a function, be it a native clojure function or a string represeting a rest-api"
  [api-fn arg]
  (let [[method pfn] (s/conform :papiea/api-fn api-fn)]
    (condp = method
      :direct (pfn arg)
      :rest   (json/decode
               (try+ (:body (rest/post pfn {:content-type :json
                                            :insecure? true
                                            :body         (json/generate-string arg)}))
                     (catch Object o
                       (throw+ {:status :failed-api-call
                                :reason (str "problem executing POST: " api-fn)
                                :arg    arg
                                :cause (json/decode (:body o) keyword)
                                :http-response o})))
               keyword))))

