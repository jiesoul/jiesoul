(ns frontend.shared.sidebar 
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.shared.svg :as svg]))

(def articles-nav-show? (r/atom true))

(def css-sidebar-li-a-top "flex items-center px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25")
(def css-sidebar-li-a-second "flex items-center w-full p-2 text-gray-100 transition duration-75 
                              rounded-lg pl-11 group hover:bg-gray-700 dark:text-white dark:hover:bg-gray-100")

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
         [:li>a {:class css-sidebar-li-a-top
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/dashboard])}
          [:span {:class "mx-3"} "Dashboard"]] 
         
         [:li>a {:class css-sidebar-li-a-top
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/categories])}
          [:span {:class "mx-3"} "Categories"]]
         
         [:li>a {:class css-sidebar-li-a-top
                 :href "#"
                 :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/tags])}
          [:span {:class "mx-3"} "Tags"]]

         [:li 
          [:button {:type "button"
                    :class "flex items-center w-full px-6 py-2 mt-4 text-gray-100 bg-gray-700 bg-opacity-25"
                    :on-click #(swap! articles-nav-show? not)}
           [:span {:class "mx-3"
                   :sidebartoggleitem "true"} "Articles"]
           (svg/chevron-up)]
          [:ul {:class "py-2 space-y-2"
                :hidden @articles-nav-show?} 
           [:li>a {:href "#"
                   :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/articles])
                   :class css-sidebar-li-a-second}
            "Article"] 
           [:li>a {:href "#"
                   :on-click #(re-frame/dispatch [::f-state/navigate ::f-state/articles-comments])
                   :class css-sidebar-li-a-second}
            "Comment"]]]
         
         [:li>a {:class css-sidebar-li-a-top
                 :href "/"}
          [:span {:class "mx-3"} "Users"]]
         
         [:li>a {:class css-sidebar-li-a-top
                 :href "/"}
          [:span {:class "mx-3"} "Users Tokens"]]
         
         [:li>a {:class css-sidebar-li-a-top
                 :href "/"}
          [:span {:class "mx-3"} "Api Tokens"]]

         ]]])))