(ns ctmx.catan.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [ctmx.catan.config :as config]
    [ctmx.catan.env :refer [defaults]]
    [ctmx.catan.state :as state]

    ;; Edges
    [kit.edge.server.undertow]
    [ctmx.catan.web.handler]

    ;; Routes
    [ctmx.catan.web.routes.api]

    [ctmx.catan.web.routes.ui])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))

(defonce system (atom nil))

(defn stop-app []
  (state/dump-state)
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!))
  (shutdown-agents))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/prep)
       (ig/init)
       (reset! system))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& _]
  (start-app))
