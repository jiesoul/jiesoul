(ns frontend.routes.user-token 
  (:require [frontend.http :as f-http]
            [frontend.shared.buttons :refer [btn delete-button edit-button]]
            [frontend.shared.css :as css]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-admin]]
            [frontend.shared.tables :refer [table-admin]]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))


(re-frame/reg-event-fx
 ::query-user-tokens-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [::f-state/init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-user-tokens
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query user tokens: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/users-tokens")
                    data
                    ::query-user-tokens-ok)))

(defn query-form []
  ;; page query form
  (let [q-data (r/atom {:page-size 10 :page 1 :filter "" :sort "create_time desc"})
        filter (r/cursor q-data [:filter])]
    [:<>
     [:form
      [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         (text-input-backend {:label "User"
                              :type "text"
                              :id "username"
                              :on-blur #(when-let [v (f-util/get-trim-value %)]
                                          (swap! filter str " username lk " v))})]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [btn {:on-click #(re-frame/dispatch [::query-user-tokens @q-data])
              :class css/buton-purple} "Query"]]]]
     [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]]))

(def columns [{:key :user-id :title "user-id"}
              {:key :create-time :title "Create Time" :format f-util/format-time}
              {:key :expires-time :title "Expires Time" :format f-util/format-time}])

(defn index []
  (let [{:keys [list total opts]} @(re-frame/subscribe [::f-state/current-route-result])
        pagination (assoc opts :total total :query-params opts :url ::query-user-tokens)
        data-sources list]
    [layout-admin
     [:div]
     [query-form]
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))