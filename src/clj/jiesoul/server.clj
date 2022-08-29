(ns jiesoul.server
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [ring.adapter.jetty :as jetty]
            [jiesoul.router :as router]))

(defn app [db]
  (router/routes (:db db)))

(def system-config 
  {:adapter/jetty {:port 3000
                   :join? false
                   :handler (ig/ref :handler/run-app)}
   :handler/run-app {:db (ig/ref :database.sql/connection)}
   :database.sql/connection {:dbtype "sqlite" :dbname "resources/database/jiesoul.db"}})

(defmethod ig/init-key :adapter/jetty [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (-> opts (dissoc :handler) (assoc :join? false))]
   (println "server running in port" (:port opts)) 
    {:handler handler
     :server (jetty/run-jetty (fn [req] (@@handler req)) options)}))

(defmethod ig/init-key :handler/run-app [_ db]
  (app db))

(defmethod ig/init-key :database.sql/connection  [_ db-spec]
  (let [ds (jdbc/get-datasource db-spec)]
    (jdbc/with-options ds {:builder-fn rs/as-unqualified-maps})))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

(defmethod ig/suspend-key! :adapter/jetty [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :adapter/jetty [key opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
      old-impl)
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defn -main []
  (ig/init system-config))