(ns frontend.routes.category 
  (:require [frontend.http :as f-http]
            [frontend.routes.category :as category]
            [frontend.shared.breadcrumb :refer [breadcrumb-dash]]
            [frontend.shared.buttons :refer [btn]]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-dash]]
            [frontend.shared.modals :as  modals]
            [frontend.shared.msg :refer [resp-message]]
            [frontend.shared.page :refer [page-backend]]
            [frontend.shared.tables :refer [table-dash]] 
            [frontend.shared.css :as css]
            [frontend.util :as f-util]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-sub
 ::category-resp
 (fn [db]
   (get-in db [:category :resp])))

(re-frame/reg-event-db
 ::clean-category-resp
 (fn [db _]
   (assoc-in db [:category :resp] nil)))

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

(re-frame/reg-event-db
 ::add-category-ok 
 (fn [db [_ resp]]
   (f-util/clog "add category ok: " resp)
   (assoc-in db [:category :resp] {:status "ok" 
                                   :message "添加成功"})))

(re-frame/reg-event-db
 ::add-category-failed
 (fn [db [_ resp]]
   (f-util/clog "add category failed: " resp)
   (assoc-in db [:category :resp] (:response resp))))

(re-frame/reg-event-fx 
 ::add-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "add category: " category)
   (f-http/http-post db (f-http/api-uri "/categories") {:category category} ::add-category-ok ::add-category-failed)))

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
   (f-util/clog "query category")
   (f-http/http-get db (f-http/api-uri "/categories") {} ::query-categories-ok ::f-state/req-failed-message)))

(re-frame/reg-event-db
 ::get-category-ok
 (fn [db [_ resp]]
   (assoc-in db [:category :current] (:category (:data resp)))))

(re-frame/reg-event-fx
 ::get-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a Category")
   (f-http/http-get db (f-http/api-uri "/categories/" id) {} ::get-category-ok ::f-state/req-failed-message)))

(re-frame/reg-sub
 ::category-current
 (fn [db]
   (get-in db [:category :current])))

(re-frame/reg-event-db
 ::reset-current
 (fn [db [_ k v]]
   (assoc-in db [:category :current k] v)))

(re-frame/reg-event-db
 ::update-category-ok
 (fn [db [_ resp]]
   (f-util/clog "update category ok: " resp)
   (assoc-in db [:category :resp] {:status "ok"
                                   :message "保存成功"})))

(re-frame/reg-event-db
 ::update-category-failed
 (fn [db [_ resp]]
   (f-util/clog "update category failed: " resp)
   (assoc-in db [:category :resp] (:response resp))))

(re-frame/reg-event-fx
 ::update-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "update category: " category)
   (f-http/http-put db (f-http/api-uri "/categories/" (:id category)) {:category category} ::update-category-ok ::update-category-failed)))


(re-frame/reg-event-fx
 ::delete-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete Category")
   (f-http/http-delete db (f-http/api-uri "/categories/" id) {} ::delete-category-ok ::delete-category-failed)))

(defn check-name [d]
  (f-util/clog "check name")
  (let [v (f-util/get-value d)]
    (if (nil? v) 
      (reset! name-error "名称不能为空")
      (reset! name-error nil))))

(defn add-form []
  (let [category (r/atom {})
        resp-msg (re-frame/subscribe [::category-resp])]
    [:form 
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"}
      
      [:div 
       (text-input-backend {:label "Name"
                            :name "name"
                            :required true 
                            :on-change #(swap! category assoc :name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"} "Oops!"]
          @name-error])]
      [:div 
       (text-input-backend {:label "Description"
                            :name "descrtiption" 
                            :on-change #(swap! category assoc :description (f-util/get-value %))})]]  
     (resp-message @resp-msg)
     [:div {:class "flex justify-center items-center space-x-4 mt-4"} 
      [:button {:type "button"
                :class css/button-yellow
                :on-click #(re-frame/dispatch [::add-category @category])}
       "Add"]]]))

(defn update-form []
  (let [current (re-frame/subscribe [::category-current]) 
        resp-msg (re-frame/subscribe [::category-resp])
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
     (resp-message @resp-msg)
     [:div {:class "flex justify-center items-center space-x-4"} 
      [:button {:type "button"
                :class "text-white bg-red-700 hover:bg-red-800 focus:ring-4 
                      focus:outline-none focus:ring-red-300 font-medium rounded-lg 
                      text-sm px-5 py-2.5 text-center dark:bg-red-600 
                      dark:hover:bg-red-700 dark:focus:ring-red-800"
                :on-click #(re-frame/dispatch [::update-category @current])}
       "Update"]]]))

(defn index [] 
  (let [add-modal-show? @(re-frame/subscribe [::add-modal-show?])
        update-modal-show? @(re-frame/subscribe [::update-modal-show?])
        q-data (r/atom {:per-page 10 :page 1})] 
    (layout-dash
     [:<>
      
      [:div {:class "flex-1 flex-col mt-2 border border-white-500 px-4 bg-white h-96"}
       (breadcrumb-dash ["Category"])
       [:div {:class "my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"} 
        [:form {:class "w-full"} 
         [:div {:class "grid gap-6 mb-6 md:grid-cols-2 max-w-lg"} 
          (text-input-backend {:label "name"
                               :type "text"
                               :id "name"
                               :on-change #(swap! q-data assoc-in [:filter :name] (f-util/get-value %))})]
         [:div {:class "felx inline-flex justify-center items-center w-full"}
          (btn {:on-click #(re-frame/dispatch [::query-categories @q-data])
                :class css/buton-purple} "Query")
          (btn {:on-click #(re-frame/dispatch [::show-add-modal true])
                :class css/button-green} "New")]] 
        (modals/modal add-modal-show? 
                      {:id "add-category"
                       :title "Add Category"
                       :on-close #(do
                                    (re-frame/dispatch [::clean-category-resp])
                                    (re-frame/dispatch [::show-add-modal false]))} 
                      [add-form])
        (modals/modal update-modal-show?
                      {:id "update-category"
                       :title "Update Category"
                       :on-close #(do
                                    (re-frame/dispatch [::clean-category-resp])
                                    (re-frame/dispatch [::show-update-modal false]))}
                      [update-form])
        [:div {:class "flex-1 h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]
        (table-dash
         [:tr
          [:th {:class css/list-table-thead-tr-th} "Name"]
          [:th {:class css/list-table-thead-tr-th} "Description"]
          [:th {:class css/list-table-thead-tr-th} "操作"]]
         (let [{:keys [categories query]} @(re-frame/subscribe [::categories-list])]
           (for [c categories]
             [:tr {:class css/list-table-tbody-tr}
              [:td {:class css/list-table-tbody-tr-td}
               [:span {:class ""} (:name c)]]
              [:td {:class css/list-table-tbody-tr-td}
               [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} (:description c)]]
              [:td {:class css/list-table-tbody-tr-td}
               (btn {:on-click #(do (re-frame/dispatch [::get-category (:id c)])
                                    (re-frame/dispatch [::show-update-modal true]))
                     :class css/buton-purple} 
                    "Edit")
               (btn {:on-click #(do 
                                    (re-frame/dispatch [::delete-category (:id c)]))
                     :class css/button-yellow}
                    "Del")]]))
         (page-backend {}))]]])))