(ns frontend.routes.category 
  (:require [frontend.shared.breadcrumb :refer [breadcrumb-dash]]
            [frontend.shared.layout :refer [layout-dash]]))

(defn index 
  []
  (layout-dash
   [:<>
    (breadcrumb-dash ["Categories"]) 
    [:div {:class "flex flex-col mt-4 border border-white-500 px-4 bg-white h-96"}
     [:div {:class "-my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"} 
      [:form {:class "w-full max-w-lg w-full"} 
       [:div {:class "flex inline-flex"}
        [:div {:class "md:w-1/2"}
         [:div {:class "flex inline-flex md:items-center mb-6"} 
          [:label {:class "block text-gray-700 text-xl font-bold mb-2"
                   :for "name"} "name："] 
          [:input {:class "block bg-gray-200 text-gray-700 border 
                           border-red-500 rounded py-3 px-4 mb-3 leading-tight 
                           focus:outline-none focus:bg-white"
                   :type "text"
                   :id "name"}]]]
        
        [:div {:class "md:w-1/2 mx-6 px-6"}
         [:div {:class "flex inline-flex md:items-center mb-6"} 
          [:label {:class "block text-gray-700 text-xl font-bold mb-2"
                   :for "description"} "description："] 
          [:input {:class "block bg-gray-200 text-gray-700 border 
                           border-red-500 rounded py-3 px-4 mb-3 leading-tight 
                           focus:outline-none focus:bg-white"
                   :type "text"
                   :id "description"}]]]]
       [:div {:class "felx inline-flex justify-center items-center w-full"}
        [:button {:type "button"} "Query"]]
       ]
      [:hr {:class "h-px my-2 bg-gray-200 border-0 dark:bg-gray-700"}]
      [:div {:class "align-middle inline-block min-w-full shadow overflow-hidden sm:rounded-lg border-b border-gray-200"}
       [:table {:class "min-w-full text-center"}
        [:thead
         [:tr
          [:th {:class "px-6 py-3 border-b border-gray-500 bg-gray-50
                            text-xs leading-4 font-medium text-gray-500 tracking-wider"} "Name"]
          [:th {:class "px-6 py-3 border-b border-gray-500 bg-gray-50 
                            text-xs leading-4 font-medium text-gray-500 tracking-wider"} "status"]]]
        [:tbody {:class "bg-white"}
         [:tr
          [:td {:class "px-6 py-4 whitespace-no-wrap border-b border-gray-200"} 
           [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} "jiesoul"]]
          [:td {:class "px-6 py-4 whitespace-no-wrap border-b border-gray-200"}
           [:span {:class "px-2 inline-flex text-xs leading-5 font-semibold rounded-full text-green-800"} "ssssss"]]]]]]]]]))