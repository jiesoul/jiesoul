(ns frontend.routes.article-comment
    (:require [clojure.string :as str]
              [frontend.http :as f-http]
              [frontend.shared.buttons :refer [default-button delete-button
                                               edit-button new-button red-button]]
              [frontend.shared.form-input :refer [text-input-backend]]
              [frontend.shared.layout :refer [layout-admin]]
              [frontend.shared.modals :as modals]
              [frontend.shared.tables :refer [table-admin]]
              [frontend.shared.toasts :as toasts]
              [frontend.state :as f-state]
              [frontend.util :as f-util]
              [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-db
 ::query-articles-comments-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db
    :fx [[:dispatch [::f-state/init-current-route-result (:data resp)]]]}))

(re-frame/reg-event-fx
 ::query-articles-comments
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query articles-comments: " data)
   (f-http/http-get db
                    (f-http/api-uri "/admin/articles-comments")
                    data
                    ::query-articles-comments-ok)))

(re-frame/reg-event-fx
 ::add-article-comments-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add article-comments ok: " resp)
   {:db (-> db
            (update-in [:toasts] conj {:content "添加成功" :type :info}))}))

(re-frame/reg-event-fx
 ::add-article-comments
 (fn [{:keys [db]} [_ article-comments]]
   (f-util/clog "add article-comments: " article-comments)
   (f-http/http-post db
                     (f-http/api-uri "/article-commentss-comments")
                     {:article-comments article-comments}
                     ::add-article-comments-ok)))

(re-frame/reg-event-db
 ::get-article-comments-ok
 (fn [db [_ resp]]
   (assoc-in db [:article-comments :current] (:article-comments (:data resp)))))

(re-frame/reg-event-fx
 ::get-article-comments
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a article-comments")
   (f-http/http-get db
                    (f-http/api-uri "/admin/article-commentss-comments/" id)
                    {}
                    ::get-article-comments-ok)))

(re-frame/reg-sub
 ::article-comments-current
 (fn [db]
   (get-in db [:article-comments :current])))

(re-frame/reg-event-fx
 ::delete-article-comments-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete article-comments ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::clean-current]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article-comments
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete article-comments")
   (f-http/http-delete db
                       (f-http/api-uri "/admin/article-commentss-comments/" id)
                       {}
                       ::delete-article-comments-ok)))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn add-form []
  (let [article-comments (r/atom {})]
    [:form
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}

      [:div
       (text-input-backend {:label "Name"
                            :name "name"
                            :required true
                            :on-blur #(check-name (f-util/get-value %))
                            :on-change #(swap! article-comments assoc :name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"}]
          @name-error])]
      [:div
       (text-input-backend {:label "Description"
                            :name "descrtiption"
                            :on-change #(swap! article-comments assoc :description (f-util/get-value %))})]]
     [:div {:class "flex justify-center items-center space-x-4 mt-4"}
      [new-button {:on-click #(re-frame/dispatch [::add-article-comments @article-comments])}
       "Add"]]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::article-comments-current])
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-article-comments (:id @current)]))}
       "Delete"]]]))

(defn actions [d] 
  [:div
   [edit-button {:on-click #(do
                              (re-frame/dispatch [::get-article (:id d)])
                              (re-frame/dispatch [::f-state/show-edit-modal true]))}
    "Edit"] 
   [:span " | "]
   [delete-button {:on-click #(do
                                (re-frame/dispatch [::get-article (:id d)])
                                (re-frame/dispatch [::f-state/show-delete-modal true]))}
    "Del"]])

(def columns [{:key :id :title "ID"}
              {:key :title :title "Article Title"} 
              {:key :create-time :title "Create Time" :format f-util/format-time} 
              {:key :username :title "username"}
              {:key :user-email :title "Email"}
              {:key :operation :title "Actioin" :render actions}
              {:key :content :title "content"}])

(defn index []
  (let [{:keys [list total opts]} @(re-frame/subscribe [::f-state/current-route-result])
        delete-modal-show? @(re-frame/subscribe [::f-state/delete-modal-show?])
        pagination (assoc opts :total total :query-params opts :url ::query-articles-comments)
        data-sources list
        q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    
    [layout-admin 
      [:div 
       [modals/modal delete-modal-show? {:id "Delete-article-comments"
                                         :title "Delete article-comments"
                                         :on-close #(do
                                                      (re-frame/dispatch [::f-state/clean-current-route-edit])
                                                      (re-frame/dispatch [::f-state/show-delete-modal false]))}
        [delete-form]]]
     
      ;; page query form
     [:form
      [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
       [:div {:class "grid grid-cols-4 gap-3"}
        [:div {:class "max-w-10 flex"}
         (text-input-backend {:label "name"
                              :type "text"
                              :id "name"
                              :on-blur #(when-let [v (f-util/get-trim-value %)]
                                          (swap! filter str " name lk " v))})]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [default-button {:on-click #(re-frame/dispatch [::query-articles-comments @q-data])}
         "Query"]]]]

      ;; data table
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))
