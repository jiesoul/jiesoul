(ns frontend.main
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent.dom :as r-dom]
            [day8.re-frame.http-fx]
            [reagent-dev-tools.core :as dev-tools]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [frontend.util :as f-util]
            [frontend.state :as f-state]
            [frontend.routes.index :as f-index]
            [frontend.routes.login :as f-login]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:current-route nil
    :token nil 
    :debug true
    :login-status nil 
    :username nil 
    :product-groups nil 
    :products nil 
    :product nil}))

(re-frame/reg-event-fx
 ::f-state/navigate 
 (fn [_ [_ & route]]
   {::navigate! route}))

(re-frame/reg-event-db
 ::f-state/navigated
 (fn [db [_ new-match]]
   (let [old-match (:current-route db)
         new-path (:path new-match)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (js/console.log (str "new-path: " new-path))
     (cond-> (assoc db :current-routes (assoc new-match :controllers controllers))
             (= "/" new-match) (-> (assoc :login-status nil)
                                   (assoc :user nil))))))

(re-frame/reg-event-fx
 ::f-state/logout 
 (fn [cofx [_]]
   (let [db (:db cofx)]
     {:db (-> db 
              (assoc-in [:login] nil)
              (assoc-in [:username] nil)
              (assoc-in [:login-status] nil)
              (assoc-in [:token] nil))
      :fx [[:dispatch [::f-state/navigate ::f-state/login]]]})))

(defn home-page []
  (let [token @(re-frame/subscribe [::f-state/token])]
    (f-util/clog "Enter home-page")
    [f-index/landing-page]
    [:div
     [:p "welcome to frontend"]
     ]))

(re-frame/reg-fx 
 ::navigate!
 (fn [route]
   (apply rfe/push-state route)))

(defn href 
  "Return relative url for given route. Url can be used in HTML links"
  ([k] (href k nil nil))
  ([k params] (href k params nil))
  ([k params query]
   (rfe/href k params query)))


(def routes-dev 
  ["/"
   [""
    {:name ::f-state/home
     :view home-page
     :link-text "Home"
     :controllers
     [{:start (fn [& params] (js/console.log (str "Entering home page, params:" params)))
       :stop (fn [& params] (js/console.log (str "Leaving home page, params: " params)))}]}]
   ["login"
    {:name ::f-state/login 
     :view f-login/login
     :link-text "Login"
     :controllers [{:start (fn [& params] (js/console.log (str "Entering login, params: " params)))
                    :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]])

(def routes routes-dev)

(defn on-navigate [new-match]
  (f-util/clog "on-navigate, new-match" new-match)
  (when new-match
    (re-frame/dispatch [::f-state/navigated new-match])))

(def router 
  (rf/router 
   routes 
   {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:user-fragment true}))

(defn router-component [_]
  (f-util/clog "Enter router-component")
  (let [current-route @(re-frame/subscribe [::f-state/current-route])
        path-params (:path-params current-route)
        _ (f-util/clog "router-component, path-params" path-params)]
    [:div 
     (when current-route
       [(-> current-route :data :view) current-route])]))


;; Setup

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load start []
  (js/console.log "Enter start")
  (re-frame/clear-subscription-cache!)
  (init-routes!)
  (r-dom/render [router-component {:router router}]
                (.getElementById js/document "root")))

(defn ^:export init! []
  (js/console.log "Enter init")
  (re-frame/dispatch-sync [::initialize-db])
  (dev-tools/start! {:state-atom re-frame.db/app-db})
  (dev-setup)
  (start))

(comment
  (+ 1 2)
  )
