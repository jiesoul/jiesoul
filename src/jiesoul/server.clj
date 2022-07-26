(ns jiesoul.server
  (:require [integrant.core :as ig]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]))

(def system-config 
  {:jiesoul/jetty {:port 3000
                   :join? false
                   :handler #ig/ref :jiesoul/handler}})

(defmethod ig/init-key :jiesoul/jetty [_ {:keys [port :join? handler] :as opts}]
  (let [handler (atom (delay handler))
        options (-> opts (disssoc :handler) (assoc :join? false))]
   (println "server is running in port " port)
    {:handler handler
     :server (jetty/run-jetty (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! jiesoul/jetty [_ server]
  (.stop server))

(defmethod ig/init-key :jiesoul/handler [_ _]
  (ring/ring-handler 
   (ring/router 
    ["/ping" {:get {:handler (fn [_] {:status 200 :body "pong!"})}}])))

(defmethod ig/suspend-key! :jiesoul/jetty [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :jiesoul/jetty [keys opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
      old-impl)
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defn -main []
  (ig/init system-config))