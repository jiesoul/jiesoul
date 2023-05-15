(ns frontend.routes.category 
  (:require [clojure.string :as str]
            [frontend.http :as f-http]
            [frontend.routes.category :as category]
            [frontend.shared.buttons :refer [btn css-delete css-edit
                                             delete-button edit-button new-button
                                             red-button]]
            [frontend.shared.css :as css]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-admin]]
            [frontend.shared.modals :as  modals]
            [frontend.shared.tables :refer [table-admin]]
            [frontend.shared.toasts :as toasts]
            [frontend.state :as f-state]
            [frontend.util :as f-util]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-sub
 ::f-state/current-route-categories
 (fn [db _]
   (get-in db [:current-route :categories])))

(re-frame/reg-event-db
 ::get-all-categories-ok
 (fn [db [_ resp]]
   (assoc-in db [:current-route :categories] (:list (:data resp)))))

(re-frame/reg-event-fx
 ::get-all-categories
 (fn [{:keys [db]} [_ _]]
   (f-http/http-get db
                    (f-http/api-uri "/categories")
                    {:page 1 :page-size 10000}
                    ::get-all-categories-ok)))

(re-frame/reg-event-fx
 ::query-categories-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db 
    :fx [[:dispatch [::f-state/init-current-route-result (:data resp)]]]}))

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
   {:db db
    :fx [[:dispatch [::toasts/push {:content "添加成功" :type :info}]]]}))

(re-frame/reg-event-fx 
 ::add-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "add category: " category) 
   (f-http/http-post db 
                     (f-http/api-uri "/categories") 
                     {:category category} 
                     ::add-category-ok)))

(re-frame/reg-event-fx
 ::get-category-ok
 (fn [{:keys [db]} [_ resp]]
   {:db db 
    :fx [[:dispatch [::f-state/init-current-route-edit (:data resp)]]]}))

(re-frame/reg-event-fx
 ::get-category
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a Category: " id)
   (f-http/http-get db
                    (f-http/api-uri "/categories/" id) 
                    {} 
                    ::get-category-ok)))

(re-frame/reg-event-fx
 ::update-category-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "update category ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "保存成功" :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-category
 (fn [{:keys [db]} [_ category]]
   (f-util/clog "update category: " category)
   (f-http/http-patch db
                      (f-http/api-uri "/categories/" (:id category))
                      {:category category}
                      ::update-category-ok)))

(re-frame/reg-event-fx
 ::delete-category-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete category ok: " resp)
   {:db db 
    :fx [[:dispatch [::toasts/push {:type :success :content "Delete success"}]]
         [:dispatch [::f-state/clean-current-route-edit]]
         [:dispatch [::f-state/show-delete-modal false]]]}))

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

(defn new-form []
  (let [edit (r/atom {:name ""
                      :description ""})
        name (r/cursor edit [:name])
        description (r/cursor edit [:description])]
    [:form 
     [:div {:class "grid gap-4 mb-6 sm:grid-cols-2"} 
      [:div 
       (text-input-backend {:label "Name："
                            :name "name"
                            :required true 
                            :default-value ""
                            :on-blur #(check-name (f-util/get-value %))
                            :on-change #(reset! name (f-util/get-value %))})
       (when @name-error
         [:p {:class "mt-2 text-sm text-red-600 dark:text-red-500"}
          [:span {:class "font-medium"}]
          @name-error])]
      [:div 
       (text-input-backend {:label "Description"
                            :name "descrtiption" 
                            :default-value ""
                            :on-change #(reset! description (f-util/get-value %))})]] 
     [:div {:class "flex justify-center items-center space-x-4 mt-4"} 
      [new-button {:on-click #(re-frame/dispatch [::add-category @edit])}
       "Save"]]]))

(defn edit-form []
  (let [current @(re-frame/subscribe [::f-state/current-route-edit]) 
        edit (r/atom current)
        name (r/cursor edit [:name])
        description (r/cursor edit [:description])]
    [:form
     [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
      (text-input-backend {:label "Name"
                           :name "name"
                           :default-value (:name current)
                           :on-change #(reset! name (f-util/get-value %))})
      (text-input-backend {:label "Description"
                           :name "descrtiption"
                           :default-value (:description current)
                           :on-change #(reset! description (f-util/get-value %))})] 
     [:div {:class "flex justify-center items-center space-x-4"} 
      [new-button {:on-click #(re-frame/dispatch [::update-category @edit])}
       "Save"]]]))

(defn delete-form []
  (let [current (re-frame/subscribe [::f-state/current-route-edit]) 
        name (r/cursor current [:name])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @name "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(re-frame/dispatch [::delete-category (:id @current)])}
       "Delete"]]]))

(defn modals []
  ;; modals
  (let [new-modal-show? @(re-frame/subscribe [::f-state/new-modal-show?])
        update-modal-show? @(re-frame/subscribe [::f-state/edit-modal-show?])
        delete-modal-show? @(re-frame/subscribe [::f-state/delete-modal-show?])
        _ (f-util/clog "modal show state: " update-modal-show?)] 
    [:div
     [modals/modal  {:id "add-category"
                     :show? new-modal-show?
                     :title "Add Category"
                     :on-close #(re-frame/dispatch [::f-state/show-new-modal false])}
      [new-form]]
     [modals/modal  {:id "update-category"
                     :show? update-modal-show?
                     :title "Update Category"
                     :on-close #(do
                                  (re-frame/dispatch [::f-state/clean-current-route-edit])
                                  (re-frame/dispatch [::f-state/show-edit-modal false]))}
      [edit-form]]
     [modals/modal  {:id "delete-category"
                     :show? delete-modal-show?
                     :title "Delete Category"
                     :on-close #(do
                                  (re-frame/dispatch [::f-state/clean-current-route-edit])
                                  (re-frame/dispatch [::f-state/show-delete-modal false]))}
      [delete-form]]]))

(defn query-form []
  ;; page query form
  (let [q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    [:<> 
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
        [btn {:on-click #(re-frame/dispatch [::query-categories @q-data])
                :class css/buton-purple} "Query"]
        [btn {:on-click #(re-frame/dispatch [::f-state/show-new-modal true])
                :class css/button-green} "New"]]]]
     [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]]))

(defn action-fn [d]
  [:div 
   [edit-button {:class css-edit
                 :on-click #(do
                              (re-frame/dispatch [::get-category (:id d)])
                              (re-frame/dispatch [::f-state/show-edit-modal true]))}
    "Edit"]
   [:span " | "]
   [delete-button {:on-click #(do
                                (re-frame/dispatch [::get-category (:id d)])
                                (re-frame/dispatch [::f-state/show-delete-modal true]))}
    "Del"]])

(def columns [{:key :name :title "Name"}
              {:key :description :title "Description"}
              {:key :operation :title "Actions" :render action-fn}])



(defn index [] 
  (let [{:keys [list total opts]} @(re-frame/subscribe [::f-state/current-route-result]) 
        pagination (assoc opts :total total :query-params opts :url ::query-categories) 
        data-sources list]
    [layout-admin 
     [modals]
     [query-form]
     [table-admin {:columns columns
                   :datasources data-sources
                   :pagination pagination}]]))