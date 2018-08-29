(defproject ntnx/papiea "0.1.0-SNAPSHOT"
  :description ""
  :dependencies [[org.clojure/clojure "1.9.0"]           ;; clojure itself
                 [org.clojure/spec.alpha "0.2.168"]
                 [org.clojure/core.specs.alpha "0.2.36"]
                 ;;[mount "0.1.12"]                        ;; dependencies
                 
                 [metosin/compojure-api "2.0.0-alpha21"] ;; rest api routes
                 [metosin/spec-tools "0.7.1"]            ;; allowes for more complex specs

                 [ring/ring-defaults "0.3.2"]            ;; ring http middleware defaults

                 [org.clojure/core.memoize "0.7.1"]      ;; advanced memoization
                 [com.rpl/specter "1.1.1"]               ;; deep datastructure transformation
                 [slingshot "0.12.2"]                    ;; advanced error handling

                 ;;[kovacnica/clojure.network.ip "0.1.2"]  ;; library for handling ip spaces

                 [tracks "1.0.5"]                        ;; improved destructuring
                 [orchestra "2017.11.12-1"]              ;; better specs validation
                 ;;[com.grammarly/perseverance "0.1.3"]    ;; retry on exceptions
                 [com.novemberain/monger "3.1.0"]        ;; Mongo DB

                 [clj-http "3.9.0"]                      ;; http client 
                 [cheshire "5.8.0"]                      ;; json support
                 [com.clojure-goes-fast/clj-async-profiler "0.1.3"]
                 [com.climate/claypoole "1.1.4"]
                 ]
  :ring {:handler papiea.handler/app}
  :jvm-opts ["-Xmx1024m" "-server" "-XX:+UseSerialGC" "-XX:+UseStringDeduplication"] 
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.8.0"]
                                  [org.clojure/test.check "0.9.0"]
                                  [ring/ring-mock "0.3.2"]]
                   :plugins [[lein-ring "0.12.4"]]}})
