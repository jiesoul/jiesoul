(ns frontend.shared.tables 
  (:require [frontend.shared.buttons :refer [btn]]
            [frontend.shared.page :refer [page-dash]]
            [reagent.core :as r]))

(def css-list "relative shadow-md sm:rounded-lg w-full")
(def css-list-table "w-full p-1 border-collapse border border-gray-100 text-sm text-center text-gray-500 dark:text-gray-400")
(def css-list-table-thead "text-base text-gray-700 dark:text-gray-400")
(def css-list-table-thead-tr "")
(def css-list-table-thead-tr-th "p-1 border border-gray-500 bg-gray-50 text-base leading-4 
                                 font-blod text-gray-500 tracking-wider")

(def css-list-table-tbody "bg-white")
(def css-list-table-tbody-tr "bg-white dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-200")
(def css-list-table-tbody-tr-td "px-2 py-2 border whitespace-no-wrap border-gray-200")

(defn th-dash [children]
  [:th {:class css-list-table-thead-tr-th} children])

(defn tbody-tr [children]
  [:tr {:class css-list-table-tbody-tr} children])

(defn td-dash [children]
  [:td {:class css-list-table-tbody-tr-td} children])

(defn table-dash [thead tbody & page]
  [:div {:class css-list}
   [:table {:class css-list-table}
    [:thead {:class css-list-table-thead}
     thead]
    [:tbody {:class css-list-table-tbody}
     tbody]]
   page])

(defn table-admin [{:keys [columns datasources pagination]}]
  [:div {:class css-list}
   [:table {:class css-list-table}
    [:thead {:class css-list-table-thead} 
     [:tr
      (for [{:keys [data-index title key]} columns]
        [:th {:class css-list-table-thead-tr-th
              :data-index data-index
              :key key} title])]]
    [:tbody {:class css-list-table-tbody}
     (for [ds datasources]
       [:tr {:class css-list-table-tbody-tr}
        (for [{:keys [key format]} columns]
          [:td {:class css-list-table-tbody-tr-td} 
           (if (= key :works)
             [:div 
              (for [{:keys [class title on-click]} (:works ds)]
                [:<>
                 [btn {:class class
                       :on-click on-click} title]
                 [:span " | "]])]
             (key ds))])])]] 
   [page-dash pagination]])

