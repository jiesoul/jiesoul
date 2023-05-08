(ns frontend.main
  (:require [clojure.spec.alpha :as s]
            [day8.re-frame.http-fx]
            [frontend.routes.article :as article]
            [frontend.routes.article-comment :as article-comment]
            [frontend.routes.category :as category]
            [frontend.routes.dashboard :as dashboard]
            [frontend.routes.index :as f-index]
            [frontend.routes.login :as f-login]
            [frontend.routes.tag :as tag]
            [frontend.routes.user :as user]
            [frontend.shared.toasts :as toasts]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent-dev-tools.core :as dev-tools]
            [reagent.dom :as rdom]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:current-route nil
    :toasts (vec [])
    :toasts-sum 0
    :error nil
    :token nil
    :debug true
    :login-status nil
    :login-user nil
    :modal {:show? false}
    :category nil
    :tag nil
    :article nil
    :comment nil
    :user nil
    :blog nil}))

(re-frame/reg-event-fx
 ::f-state/load-localstore
 (fn [cofx _]
   (let [defaults (:local-store cofx)]
     {:db (assoc (:db cofx) :defaults defaults)})))

(re-frame/reg-event-fx
 ::f-state/req-failed-message
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (f-util/clog "resp failed: " response)
   {:db db
    :fx [[:dispatch [::toasts/push {:content (:message response)
                                    :type :error}]]]}))

(re-frame/reg-event-db
 ::f-state/init
 (fn [db [_ k]]
   (assoc db k nil)))

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
     (cond-> (assoc db :current-route (assoc new-match :controllers controllers))
       (= "/" new-match) (-> (assoc :login-status nil)
                             (assoc :user nil))))))

(re-frame/reg-fx
 ::navigate!
 (fn [route]
   (apply rfe/push-state route)))

(def routes
  ["/"
   ["" {:name ::f-state/home
        :view f-index/home-page
        :link-text "Home"
        :controllers [{:start (fn [& params] (js/console.log (str "Entering home page, params:" params)))
                       :stop (fn [& params] (js/console.log (str "Leaving home page, params: " params)))}]}]

   ["login" {:name ::f-state/login
             :view f-login/login
             :link-text "Login"
             :controllers [{:start (fn [& params] (js/console.log (str "Entering login, params: " params)))
                            :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["dashboard" {:name ::f-state/dashboard
                 :view dashboard/index
                 :link-text "dashboard"
                 :controllers [{:start (fn [& params] 
                                         (re-frame/dispatch [::f-state/init :dashboard])
                                         (js/console.log (str "Entering dashboard, params: " params)))
                                :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]

   ["categories" {:name ::f-state/categories
                  :view category/index
                  :link-text "categories"
                  :controllers [{:start (fn [& params] 
                                          (re-frame/dispatch [::f-state/init :category])
                                          (js/console.log (str "Entering categories, params: " params)))
                                 :stop (fn [& params] 
                                         (re-frame/dispatch [::f-state/init :category])
                                         (js/console.log (str "Leaving categories, params: " params)))}]}]

   ["tags" {:name ::f-state/tags
            :view tag/index
            :link-text "tags"
            :controllers [{:start (fn [& params] 
                                    (re-frame/dispatch [::f-state/init :tag])
                                    (js/console.log (str "Entering tags, params: " params)))
                           :stop (fn [& params] 
                                   (re-frame/dispatch [::f-state/init :tag])
                                   (js/console.log (str "Leaving tags, params: " params)))}]}] 
   
   ["articles"

    ["" {:name ::f-state/articles
         :view article/index
         :link-text "articles"
         :controllers [{:start (fn [& params]
                                 (re-frame/dispatch [::f-state/init :article])
                                 (js/console.log (str "Entering articles, params: " params)))
                        :stop (fn [& params] (js/console.log (str "Leaving articles, params: " params)))}]}]
    
    ["/comments" {:name ::f-state/articles-comments
                          :view article-comment/index
                          :link-text "articles-comments"
                          :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                                         :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]]
   
   
   ["users" {:name ::f-state/users
             :view user/index
             :link-text "users"
             :controllers [{:start (fn [& params] (js/console.log (str "Entering dashboard, params: " params)))
                            :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]])

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
  (rdom/render [router-component {:router router}]
               (.getElementById js/document "root")))

(defn ^:export init! []
  (js/console.log "Enter init!")
  (re-frame/dispatch-sync [::initialize-db])
  (dev-tools/start! {:state-atom re-frame.db/app-db})
  (dev-setup)
  (start))

(defn ^:dev/after-load reload []
  (.reload router))

;; (.go js/window.history -1)
