(ns frontend.routes.dashboard
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.routes.login :refer [login]]
            [frontend.shared.layout :refer [layout-dash]]))

(defn index [] 
  (let [token @(re-frame/subscribe [::f-state/token])]
    (if token
      (layout-dash
       [:<>
        [:h3 {:class "text-gray-700 text-3xl font-medium"} "Dashboard"]
        [:div {:class "mt-4"}
         [:div {:class "flex flex-wrap -mx-6"}
          [:div {:class "w-full px-6 sm:w-1/2 xl:w-1/3"}
           [:div {:class "flex items-center px-5 py-6 shadow-sm rounded-md bg-white"}
            [:div {:class "p-3 rounded-full bg-indigo-600 bg-opacity-75"}
             ]
            [:div {:class "mx-5"}
             [:h4 {:class "text-2xl font-semibold text-gray-700"} "8,282"]
             [:div {:class "text-gray-500"} "new Users"]]]]
          
          [:div {:class "w-full mt-6 px-6 sm:w-1/2 xl:w-1/3 sm:mt-0"}
           [:div {:class "flex items-center px-5 py-6 shadow-sm rounded-md bg-white"}
            [:div {:class "mx-5"}
             [:h4 {:class "text-2xl font-semibold text-gray-700"} "200,556"]
             [:div {:class "text-gray-500"} "Total Orders"]]]]
          
          [:div {:class "w-full mt-6 px-6 sm:w-1/2 xl:w-1/3 sm:mt-0"}
           [:div {:class "flex items-center px-5 py-6 shadow-sm rounded-md bg-white"}
            [:div {:class "mx-5"}
             [:h4 {:class "text-2xl font-semibold text-gray-700"} "154,556"]
             [:div {:class "text-gray-500"} "Total"]]]]]]
        
        [:div {:class "mt-8"}]

        [:div {:class "flex flex-col mt-8"}
         [:div {:class "-my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
          [:div {:class "align-middle inline-block min-w-full shadow overflow-hidden sm:rounded-lg border-b border-gray-200"}
           [:table {:class "min-w-full"}
            [:thead 
             [:tr 
              [:th {:class "px-6 py-3 border-b border-gray-200 bg-gray-50 text-left 
                            text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider"} "Name"]
              [:th {:class "px-6 py-3 border-b border-gray-200 bg-gray-50 text-left 
                            text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider"} "status"]]]
            [:tbody {:class "bg-white"}
             [:tr 
              [:td {:class "px-6 py-4 whitespace-no-wrap border-b border-gray-200"}
               [:div {:class "flex items-center"}
                [:div {:class "ml-4"}
                 [:div {:class "text-sm leading-5 font-medium text-gray-900"} "jiesoul"]]]]
              [:td {:class "px-6 py-4 whitespace-no-wrap border-b border-gray-200"} 
               [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800"} "ssssss"]]]]]]]]

        [:div {:class "mt-8"}
         
         [:p.mb-8.leading-normal "Hey there! Welcome to the " [:b "Clojure / ClojureScript Reagent"]
          " version of Ping CRM, a demo app designed to help illustrate how "
          [:a.text-indigo-500.underline.hover:text-orange-600
           {:href "https://inertiajs.com"} "Inertia.js"] " works."]
         [:p.mb-1.leading-normal "Full-stack source code of this demo available "
          [:a.text-indigo-500.underline.hover:text-orange-600
           {:href "https://github.com/prestancedesign/clojure-inertia-pingcrm-demo"}
           "here."]]
         [:p.mb-8.leading-normal "More information about the Inertia Clojure adapter "
          [:a.text-indigo-500.underline.hover:text-orange-600
           {:href "https://github.com/prestancedesign/inertia-clojure"} "here."]]]])
      [login])))