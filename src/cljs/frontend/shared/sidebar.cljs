(ns frontend.shared.sidebar 
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [frontend.state :as f-state]))

(def articles-h (r/atom true))

(defn active? [uri path]
  (if (= path uri) true false))

(defn sidebar-dash []
  (let [current-route @(re-frame/subscribe [::f-state/current-route])
        path (:path current-route)]
    (fn []
      [:<>
       [:div {:class "fixed inset-0 z-20 transition-opacity bg-black opacity-50 lg:hidden"}]
       
       [:aside {:id "sidebar-dash"
                :class "fixed inset-y-0 left-0 z-30 w-64 overflow-y-auto transition duration-300
                  transform bg-gray-900 lg:translate-x-0 lg:static lg:inset-0"
                :aria-label "Sidebar"}
        [:div {:class "flex items-center justify-center mt-8"}
         [:div {:class "flex items-center"}
          [:span {:class "mx-2 text-2xl font-semibold text-white"} "Dashboard"]]] 
        
        [:ul {:class "mt-10"} 
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/dashboard])
                 :active #(active? "/dashboard" path)}
          [:span {:class "mx-3"} "Dashboard"]] 
         
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/categories])}
          [:span {:class "mx-3"} "Categories"]]
         
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/tags])}
          [:span {:class "mx-3"} "Tags"]]

         [:li 
          [:button {:type "button"
                    :class "flex items-center w-full px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                    :on-click #(swap! articles-h not)}
           [:span {:class "mx-3"
                   :sidebartoggleitem "true"} "Articles"]
           [:svg {:sidebartoggleitem "true"
                  :class "w-6 h-6"
                  :fill "currentColor"
                  :view-box "0 0 20 20"
                  :xmlns "http://www.w3.org/2000/svg"}
            [:path {:fill-rule "evenodd"
                    :d "M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 
                    1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                    :clip-rule "evenodd"}]]]
          [:ul {:class "py-2 space-y-2"
                :hidden @articles-h} 
           [:li>a {:href "#"
                   :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/articles])
                   :class "flex items-center w-full p-2 text-gray-100 transition duration-75 
                           rounded-lg pl-11 group hover:bg-gray-700 dark:text-white dark:hover:bg-gray-100"}
            "Article"] 
           [:li>a {:href "#"
                   :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/articles-comments])
                   :class "flex items-center w-full p-2 text-gray-100 transition duration-75 
                           rounded-lg pl-11 group hover:bg-gray-700 dark:text-white dark:hover:bg-gray-100"}
            "Comment"]]]
         
         
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "/"}
          [:span {:class "mx-3"} "Users"]]
         
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "/"}
          [:span {:class "mx-3"} "Users Tokens"]]
         
         [:li>a {:class "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                 :href "/"}
          [:span {:class "mx-3"} "Api Tokens"]]

         ]]])))