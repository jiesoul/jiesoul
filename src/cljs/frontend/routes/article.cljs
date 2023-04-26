(ns frontend.routes.article
    (:require [clojure.string :as str]
            [frontend.http :as f-http]
            [frontend.shared.breadcrumb :refer [breadcrumb-dash]]
            [frontend.shared.buttons :refer [btn delete-button edit-button
                                             green-button red-button]]
            [frontend.shared.css :as css]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-dash]]
            [frontend.shared.modals :as modals]
            [frontend.shared.page :refer [page-dash]]
            [frontend.shared.tables :refer [table-dash td-dash th-dash]]
            [frontend.shared.toasts :as toasts]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-db
 ::init
 (fn [db _]
   (assoc db :article nil)))

(re-frame/reg-sub
 ::add-modal-show?
 (fn [db]
   (get-in db [:article :add-modal-show?])))

(re-frame/reg-event-db
 ::show-add-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:article :add-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::update-modal-show?
 (fn [db]
   (get-in db [:article :update-modal-show?])))

(re-frame/reg-event-db
 ::show-update-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:article :update-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::delete-modal-show?
 (fn [db]
   (get-in db [:article :delete-modal-show?])))

(re-frame/reg-event-db
 ::show-delete-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:article :delete-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::articles-list
 (fn [db]
   (get-in db [:article :list])))

(re-frame/reg-event-db
 ::query-articles-ok
 (fn [db [_ resp]]
   (assoc-in db [:article :list] (:data resp))))

(re-frame/reg-event-fx
 ::query-articles
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query articles: " data)
   (f-http/http-get db
                    (f-http/api-uri "/articles")
                    data
                    ::query-articles-ok)))

(re-frame/reg-event-fx
 ::add-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add article ok: " resp)
   {:db (-> db
            (update-in [:toasts] conj {:content "添加成功" :type :info}))}))

(re-frame/reg-event-fx
 ::add-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "add article: " article)
   (f-http/http-post db
                     (f-http/api-uri "/articles")
                     {:article article}
                     ::add-article-ok)))

(re-frame/reg-event-db
 ::get-article-ok
 (fn [db [_ resp]]
   (assoc-in db [:article :current] (:article (:data resp)))))

(re-frame/reg-event-fx
 ::get-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a article")
   (f-http/http-get db
                    (f-http/api-uri "/articles/" id)
                    {}
                    ::get-article-ok)))

(re-frame/reg-sub
 ::article-current
 (fn [db]
   (get-in db [:article :current])))

(re-frame/reg-event-db
 ::reset-current
 (fn [db [_ k v]]
   (assoc-in db [:article :current k] v)))

(re-frame/reg-event-fx
 ::update-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "update article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "保存成功"
                                    :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "update article: " article)
   (f-http/http-put db
                    (f-http/api-uri "/articles/" (:id article))
                    {:article article}
                    ::update-article-ok
                    ::f-state/req-failed-message)))

(re-frame/reg-event-db
 ::clean-current
 (fn [db _]
   (assoc-in db [:article :current] nil)))

(re-frame/reg-event-fx
 ::delete-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::clean-current]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete article")
   (f-http/http-delete db
                       (f-http/api-uri "/articles/" id)
                       {}
                       ::delete-article-ok)))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn add-form []
  (let [article (r/atom {})]
    [:form
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}

      [:div
       (text-input-backend {:label "Name"
                            :name "name"
                            :required true
                            :on-blur #(check-name (f-util/get-value %))
                            :on-change #(swap! article assoc :name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"}]
          @name-error])]
      [:div
       (text-input-backend {:label "Description"
                            :name "descrtiption"
                            :on-change #(swap! article assoc :description (f-util/get-value %))})]]
     [:div {:class "flex justify-center items-center space-x-4 mt-4"}
      [green-button {:on-click #(re-frame/dispatch [::add-article @article])}
       "Add"]]]))

(defn update-form []
  (let [current (re-frame/subscribe [::article-current])
        name (r/cursor current [:name])
        description (r/cursor current [:description])]
    [:form
     [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
      (text-input-backend {:label "Name"
                           :name "name"
                           :default-value @name
                           :on-change #(re-frame/dispatch [::reset-current :name (f-util/get-value %)])})
      (text-input-backend {:label "Description"
                           :name "descrtiption"
                           :default-value @description
                           :on-change #(re-frame/dispatch [::reset-current :description (f-util/get-value %)])})]
     [:div {:class "flex justify-center items-center space-x-4"}
      [green-button {:on-click #(re-frame/dispatch [::update-article @current])}
       "Update"]]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::article-current])
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-article (:id @current)]))}
       "Delete"]]]))

(defn index []
  (let [add-modal-show? @(re-frame/subscribe [::add-modal-show?])
        update-modal-show? @(re-frame/subscribe [::update-modal-show?])
        delete-modal-show? @(re-frame/subscribe [::delete-modal-show?])
        q-data (r/atom {:per-page 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    (layout-dash
     [:div {:class "flex-1 flex-col mt-2 border border-white-500 px-4 bg-white h-96"}
      ;; page title
      [:div
       [breadcrumb-dash ["Article"]]]

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
         (btn {:on-click #(re-frame/dispatch [::query-articles @q-data])
               :class css/buton-purple} "Query")
         (btn {:on-click #(re-frame/dispatch [::show-add-modal true])
               :class css/button-green} "New")]]]

      ;; modals
      [:div
       [modals/modal add-modal-show? {:id "add-article"
                                      :title "Add article"
                                      :on-close #(re-frame/dispatch [::show-add-modal false])}
        [add-form]]
       [modals/modal update-modal-show? {:id "update-article"
                                         :title "Update article"
                                         :on-close #(do
                                                      (re-frame/dispatch [::clean-current])
                                                      (re-frame/dispatch [::show-update-modal false]))}
        [update-form]]
       [modals/modal delete-modal-show? {:id "Delete-article"
                                         :title "Delete article"
                                         :on-close #(do
                                                      (re-frame/dispatch [::clean-current])
                                                      (re-frame/dispatch [::show-delete-modal false]))}
        [delete-form]]]
      ;; hr
      [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]

      ;; data table
      [:div
       (let [{:keys [articles query total]} @(re-frame/subscribe [::articles-list])
             page (:page query)
             per-page (:per-page query)]
         (table-dash
          [:tr
           [th-dash "Name"]
           [th-dash "Description"]
           [th-dash "操作"]]
          (for [c articles]
            [:tr {:class css/list-table-tbody-tr}
             [td-dash
              [:span {:class ""} (:name c)]]
             [td-dash
              [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} (:description c)]]
             [td-dash
              [:<>
               [edit-button {:on-click #(do (re-frame/dispatch [::get-article (:id c)])
                                            (re-frame/dispatch [::show-update-modal true]))}
                "Edit"]
               [:span " | "]
               [delete-button {:on-click #(do
                                            (re-frame/dispatch [::get-article (:id c)])
                                            (re-frame/dispatch [::show-delete-modal true]))}
                "Del"]]]])
          (page-dash {:page page
                      :per-page per-page
                      :total total
                      :query query
                      :url ::query-articles})))]])))
