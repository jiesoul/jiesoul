(ns frontend.routes.category 
  (:require [frontend.shared.breadcrumb :refer [breadcrumb-dash]]
            [frontend.shared.buttons :refer [new-button query-button]]
            [frontend.shared.form-input :refer [text-input-backend]]
            [frontend.shared.layout :refer [layout-dash]]
            [frontend.shared.page :refer [page-backend]]
            [frontend.shared.tables :refer [table-dash]]
            [frontend.shared.modals :as  modals]
            [re-frame.core :as re-frame]))


(re-frame/reg-sub
 ::add-modal-show?
 (fn [db]
   (:add-modal-show? db)))

(re-frame/reg-event-db
 ::show-add-modal 
 (fn [db [_ show?]]
   (-> db 
       (assoc :add-modal-show? show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::update-modal-show?
 (fn [db]
   (:update-modal-show? db)))

(re-frame/reg-event-db
 ::show-update-modal
 (fn [db [_ show?]]
   (-> db
       (assoc :update-modal-show? show?)
       (assoc :modal-backdrop-show? show?))))

(def css-thead-tr-th "px-6 py-3 border-b border-gray-500 bg-gray-50
                      text-xs leading-4 font-medium text-gray-500 tracking-wider")

(def css-tbody-tr "bg-white border-b dark:bg-gray-800 dark:border-gray-700")
(def css-tbody-tr-td "px-6 py-4 whitespace-no-wrap border-b border-gray-200")

(defn add-form []
  [:form {:action "#"}
   [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
    (text-input-backend {:label "Name"
                         :name "name"
                         :on-change #()})
    (text-input-backend {:label "Description"
                         :name "descrtiption"
                         :on-chchange #()})]
   [:div {:class "flex items-center space-x-4"}
    [:button {:type "submit"
              :class "text-white bg-red-700 hover:bg-red-800 focus:ring-4 
                      focus:outline-none focus:ring-red-300 font-medium rounded-lg 
                      text-sm px-5 py-2.5 text-center dark:bg-red-600 
                      dark:hover:bg-red-700 dark:focus:ring-red-800"}
     "Add"]]])

(defn udpate-form []
  [:form {:action "#"}
   [:div {:class "grid gap-4 mb-4 sm:grid-cols-2"}
    (text-input-backend {:label "Name"
                         :name "name"
                         :on-change #()})
    (text-input-backend {:label "Description"
                         :name "descrtiption"
                         :on-chchange #()})]
   [:div {:class "flex items-center space-x-4"}
    [:button {:type "submit"
              :class "text-white bg-red-700 hover:bg-red-800 focus:ring-4 
                      focus:outline-none focus:ring-red-300 font-medium rounded-lg 
                      text-sm px-5 py-2.5 text-center dark:bg-red-600 
                      dark:hover:bg-red-700 dark:focus:ring-red-800"}
     "Update"]]])

(defn index [] 
  (let [add-modal-show? @(re-frame/subscribe [::add-modal-show?])
        update-modal-show? @(re-frame/subscribe [::update-modal-show?])] 
    (layout-dash
     [:<>
      (breadcrumb-dash ["Categories"])
      [:div {:class "flex-1 flex-col mt-4 border border-white-500 px-4 bg-white h-96"}
       [:div {:class "-my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"} 
        [:form {:class "w-full"} 
         [:div {:class "grid gap-6 mb-6 md:grid-cols-2 max-w-lg"} 
          (text-input-backend {:label "name"
                               :type "text"
                               :id "name"})
          (text-input-backend {:label "description"
                               :type "text"
                               :id "description"})]
         [:div {:class "felx inline-flex justify-center items-center w-full"}
          (query-button {} "Query")
          (new-button {:on-click #(re-frame/dispatch [::show-add-modal true])} "New")]] 
        (modals/modal add-modal-show? 
                      {:id "add-category"
                       :title "Add Category"
                       :on-close #(re-frame/dispatch [::show-add-modal false])} 
                      [add-form])
        (modals/modal update-modal-show?
                      {:id "update-category"
                       :title "Update Category"
                       :on-close #(re-frame/dispatch [::show-update-modal false])}
                      [add-form])
        [:div {:class "flex-1 h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]
        (table-dash
         [:tr
          [:th {:class css-thead-tr-th} "Name"]
          [:th {:class css-thead-tr-th} "status"]]
         [:tr {:class css-tbody-tr}
          [:td {:class css-tbody-tr-td}
           [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} "jiesoul"]]
          [:td {:class css-tbody-tr-td}
           [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} "ssssss"]]]
         (page-backend {}))]]])))