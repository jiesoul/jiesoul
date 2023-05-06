(ns frontend.routes.category 
  (:require [clojure.string :as str]
            [frontend.http :as f-http]
            [frontend.routes.category :as category]
            [frontend.shared.buttons :refer [btn delete-button edit-button
                                             new-button red-button]]
            [frontend.shared.css :as css]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-dash]]
            [frontend.shared.modals :as  modals]
            [frontend.shared.page :refer [page-dash]]
            [frontend.shared.tables :refer [css-list-table-tbody-tr table-dash td-dash th-dash]]
            [frontend.shared.toasts :as toasts]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-db
 ::init
 (fn [db _]
   (assoc db :category nil)))

(re-frame/reg-sub
 ::add-modal-show?
 (fn [db]
   (get-in db [:category :add-modal-show?])))

(re-frame/reg-event-db
 ::show-add-modal 
 (fn [db [_ show?]]
   (-> db 
       (assoc-in [:category :add-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::update-modal-show?
 (fn [db]
   (get-in db [:category :update-modal-show?])))

(re-frame/reg-event-db
 ::show-update-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:category :update-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::delete-modal-show?
 (fn [db]
   (get-in db [:category :delete-modal-show?])))

(re-frame/reg-event-db
 ::show-delete-modal
 (fn [db [_ show?]]
   (f-util/clog "show delete modal: " show?)
   (-> db
       (assoc-in [:category :delete-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::categories-list
 (fn [db]
   (get-in db [:category :list])))

(re-frame/reg-event-db
 ::query-categories-ok
 (fn [db [_ resp]]
   (assoc-in db [:category :list] (:data resp))))

(re-frame/reg-event-fx
 ::query-categories
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query categories: " data)
   (f-http/http-get db
                    (f-http/api-uri "/categories")
                    data
                    ::query-categories-ok)))

(re-frame/reg-event-fx
 ::add-category-ok 
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add category ok: " resp) 
   {:db (-> db
            (update-in [:toasts] conj {:content "添加成功" :type :info}))}))

(re-frame/reg-event-fx 
 ::add-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "add category: " category) 
   (f-http/http-post db 
                     (f-http/api-uri "/categories") 
                     {:category category} 
                     ::add-category-ok)))

(re-frame/reg-event-db
 ::get-category-ok
 (fn [db [_ resp]]
   (assoc-in db [:category :current] (:category (:data resp)))))

(re-frame/reg-event-fx
 ::get-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a Category")
   (f-http/http-get db
                    (f-http/api-uri "/categories/" id) 
                    {} 
                    ::get-category-ok)))

(re-frame/reg-sub
 ::category-current
 (fn [db]
   (get-in db [:category :current])))

(re-frame/reg-event-db
 ::reset-current
 (fn [db [_ k v]]
   (assoc-in db [:category :current k] v)))

(re-frame/reg-event-fx
 ::update-category-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "update category ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "保存成功"
                                    :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "update category: " category)
   (f-http/http-put db 
                    (f-http/api-uri "/categories/" (:id category)) 
                    {:category category} 
                    ::update-category-ok 
                    ::f-state/req-failed-message)))

(re-frame/reg-event-db
 ::clean-current 
 (fn [db _]
   (assoc-in db [:category :current] nil)))

(re-frame/reg-event-fx
 ::delete-category-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete category ok: " resp)
   {:db db 
    :fx [[:dispatch [::toasts/push {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::clean-current]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete Category")
   (f-http/http-delete db 
                       (f-http/api-uri "/categories/" id) 
                       {} 
                       ::delete-category-ok)))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v)) 
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn add-form []
  (let [category (r/atom {})]
    [:form 
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}
      
      [:div 
       (text-input-backend {:label "Name："
                            :name "name"
                            :required true 
                            :on-blur #(check-name (f-util/get-value %))
                            :on-change #(swap! category assoc :name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"} ]
          @name-error])]
      [:div 
       (text-input-backend {:label "Description"
                            :name "descrtiption" 
                            :on-change #(swap! category assoc :description (f-util/get-value %))})]] 
     [:div {:class "flex justify-center items-center space-x-4 mt-4"} 
      [new-button {:on-click #(re-frame/dispatch [::add-category @category])}
       "Add"]]]))

(defn update-form []
  (let [current (re-frame/subscribe [::category-current]) 
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
      [new-button {:on-click #(re-frame/dispatch [::update-category @current])}
       "Update"]]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::category-current]) 
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do 
                                (re-frame/dispatch [::delete-category (:id @current)]))}
       "Delete"]]]))

(defn index [] 
  (let [add-modal-show? @(re-frame/subscribe [::add-modal-show?])
        update-modal-show? @(re-frame/subscribe [::update-modal-show?])
        delete-modal-show? @(re-frame/subscribe [::delete-modal-show?])
        q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])] 
    (layout-dash 
     [:div {:class css/main-container}
      ;; page title
      [:h4 {:class css/page-title} "Category"]
      
      ;; page query form
      [:form
       [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
        [:div {:class "grid grid-cols-4 gap-3"}
         [:div {:class "max-w-10 flex"}
          (text-input-backend {:label "name："
                               :type "text"
                               :id "name"
                               :on-blur #(when-let [v (f-util/get-trim-value %)]
                                           (swap! filter str " name lk " v))})]]
        [:div {:class "felx inline-flex justify-center items-center w-full"}
         (btn {:on-click #(re-frame/dispatch [::query-categories @q-data])
               :class css/buton-purple} "Query")
         (btn {:on-click #(re-frame/dispatch [::show-add-modal true])
               :class css/button-green} "New")]]]
      
      ;; modals
      [:div
       [modals/modal  {:id "add-category"
                       :show? add-modal-show?
                       :title "Add Category"
                       :on-close #(re-frame/dispatch [::show-add-modal false])}
        [add-form]]
       [modals/modal  {:id "update-category"
                       :show? update-modal-show?
                       :title "Update Category"
                       :on-close #(do
                                    (re-frame/dispatch [::clean-current])
                                    (re-frame/dispatch [::show-update-modal false]))}
        [update-form]]
       [modals/modal  {:id "delete-category"
                       :show? delete-modal-show?
                       :title "Delete Category"
                       :on-close #(do
                                    (re-frame/dispatch [::clean-current])
                                    (re-frame/dispatch [::show-delete-modal false]))}
        [delete-form]]]
      ;; hr
      [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]
      
      ;; data table
      [:div 
       (let [{:keys [categories opts total]} @(re-frame/subscribe [::categories-list])
             page (:page opts)
             page-size (:page-size opts)]
         (table-dash 
          [:tr
           [th-dash "Name"]
           [th-dash "Description"]
           [th-dash "操作"]] 
          (for [c categories]
            [:tr {:class css-list-table-tbody-tr}
             [td-dash (:name c)]
             [td-dash (:description c)]
             [td-dash
              [:<>
               [edit-button {:on-click #(do (re-frame/dispatch [::get-category (:id c)])
                                            (re-frame/dispatch [::show-update-modal true]))}
                "Edit"]
               [:span " | "]
               [delete-button {:on-click #(do
                                            (re-frame/dispatch [::get-category (:id c)])
                                            (re-frame/dispatch [::show-delete-modal true]))}
                "Del"]]]])
          (when (pos-int? total)
            [page-dash {:page page
                        :page-size page-size
                        :total total
                        :opts opts
                        :url ::query-categories}])))]])))