(ns jiesoul.core
  (:require [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [clojure.pprint]
            [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [ring.adapter.jetty :as jetty]
            [nrepl.server :as nrepl]
            [aero.core :as aero]
            [clojure.tools.reader.edn :as edn]
            [clojure.java.io :as io]
            [potpuri.core :as p]
            [jiesoul.webserver :as ws])
  (:gen-class))

(defmethod ig/init-key :backend/ds  [_ db-spec]
  (let [ds (jdbc/get-datasource db-spec)]
    (jdbc/with-options ds {:builder-fn rs/as-unqualified-maps})))

(defn env-value [key default]
  (some-> (or (System/getenv (name key)) default)))

(defmethod aero/reader `ig/ref [_ _ value] (ig/ref value))

(defmethod ig/init-key :backend/profile [_ profile]
  profile)

(defmethod ig/init-key :backend/env [_ {:keys [_profile data-dir] :as m}]
  (log/debug "Enter ig/init-key :backend/env")
  m)

(defmethod ig/halt-key! :backend/env [_ this]
  (log/debug "Enter ig/halt-key! :backend/env")
  this)

(defmethod ig/suspend-key! :backend/env [_ this]
  (log/debug "Enter ig/suspend-key! :backend/env")
  this)

(defmethod ig/resume-key :backend/env [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/env")
  old-impl)

(defmethod ig/init-key :backend/jetty [_ {:keys [port join? env]}]
  (log/debug "Enter ig/init-key :backend/jetty")
  (-> (ws/handler (ws/routes env))
   (jetty/run-jetty {:port port :join? join?})))

(defmethod ig/halt-key! :backend/jetty [_ server]
  (log/debug "Enter ig/halt-key! :backend/jetty")
  (.stop server))

(defmethod ig/init-key :backend/options [_ options]
  (log/debug"Enter ig/init-key :backend/options")
  options)

(defmethod ig/init-key :backend/nrepl [_ {:keys [bind port]}]
  (log/debug "Enter ig/init-key :backend/nrepl")
  (if (and bind port)
    (nrepl/start-server :bind bind :port port)
    nil))

(defmethod ig/halt-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/halt-key! :backend/nrepl")
  (if this 
    (nrepl/stop-server this)))

(defmethod ig/suspend-key! :backend/nrepl [_ this]
  (log/debug "Enter ig/suspend-key! :backend/nrepl")
  this)

(defmethod ig/resume-key :backend/nrepl [_ _ _ old-impl]
  (log/debug "Enter ig/resume-key :backend/nrepl")
  old-impl)

(defn read-config [profile]
  (let [local-config (let [file (io/file "config-local.edn")]
                           #_{:clj-kondo/ignore [:missing-else-branch]}
                           (if (.exists file) (edn/read-string (slurp file))))]
    (cond-> (aero/read-config (io/resource "config.edn"){:profile profile})
            local-config (p/deep-merge local-config))))

(defn system-config [myprofile]
  (log/debug "Enter system config read...")
  (let [profile (or myprofile (some-> (System/getenv "PROFILE") keyword) :dev)
        _ (log/info "IUsing profile " profile)
        config (read-config profile)]
    config))

(defn system-config-start []
  (system-config nil))

(defn -main []
  (log/info "System starting...")
  (let [config (system-config-start)
        _ (log/info "Config:" config)]
    (ig-repl/set-prep! (constantly config))
    (ig-repl/go)))

#_(conmmet
   (ig-repl/reset)
   (ig-repl/halt)
   (user/system)
   (keys (user/system))
   (:backend/env (user/system))
   (keys (:backend/env (user/system)))
   (keys @(:backend/env (user/system)))
   (type @(:backend/env (user/system)))
   (:users @(:backend/env (user/system)))
   (:USERS @(:backend/env (user/system)))
   (user/env)
   (user/profile)
   (System/getenv)
   (env-value :PATH :foo))