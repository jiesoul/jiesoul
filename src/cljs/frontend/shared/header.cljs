(ns frontend.shared.header 
  (:require [cljs.pprint]
            [frontend.shared.css :as css]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def css-user-dropdown-li-a "block px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white")
(def user-dropdown-show? (r/atom true))

(defn user-dropdown []
  [:div {:id "user-dropdown"
         :hidden @user-dropdown-show?
         :class "fixed z-20 right-10 bg-white divide-y divide-gray-100 rounded-lg shadow w-44 
                 dark:bg-gray-700 dark:divide-gray-600"}
   [:div {:class "px-4 py-3 text-sm text-gray-900 dark:text-white"}
    [:ul {:class "py-2 text-sm text-gray-700 dark:text-gray-200"}
     [:li>a {:class css-user-dropdown-li-a
             :href "#"}
      "Dashboard"]
     [:li>a {:class css-user-dropdown-li-a
             :href "#"}
      "Setting"]]
    [:div {:class "py-1"}
     [:a {:href "#"
          :class "block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 
                  dark:text-gray-200 dark:hover:text-white"
          :on-click #(re-frame/dispatch [::f-state/logout])}
      "Sign out"]]]])

(defn header-dash [] 
  (let [login-user @(re-frame/subscribe [::f-state/login-user])
        current-route @(re-frame/subscribe [::f-state/current-route])]
    [:header {:class "flex items-center justify-between px-6 py-4 bg-white border-b border-indigo-600"}
     [:div {:class "flex items-center"}
      [:div {:class "relative mx-4 lg:mx-0"}
       [:h5 {:class css/page-title} (get-in current-route [:data :link-text])]
       [:span {:class "absolute inset-y-0 left-0 flex items-center pl-1"}]]] 
     [:div {:class "flex items-center space-x-4"}
      (when login-user
        [:div {:class "font-medium dark:text-white"}
         [:a {:class ""
              :on-click #(swap! user-dropdown-show? not)} 
          (when login-user (:username login-user))]
         (user-dropdown)])]]))

(defn nav-home []
  (fn []
    [:nav {:class "bg-white border-gray-200 dark:bg-gray-900"}
     [:div {:class "max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4"}
      [:a {:href "/"
           :class "flex items-center"}
       [:span {:class "self-center text-2xl font-semibold whitespace-nowrap dark:text-white"}
        "Site"]]
      [:button {:data-collapse-toggle "navbar-default"
                :type "button"
                :class "inline-flex items-center p-2 ml-3 text-sm text-gray-500 rounded-lg md:hidden 
                        hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 
                        dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
                :aria-controls "navbar-default"
                :aria-expanded "false"}
       [:span {:class "sr-only"} "Open main menu"]]
      
      [:div {:class "hidden w-full md:block md:w-auto"
             :id "navbar-default"}
       [:ul {:class "flex flex-row font-medium mt-0 mr-6 space-x-8 text-xl"}
        [:li 
         [:a {:class "text-gray-900 dark:text-white hover:underline"
              :href "#"}
          "Blog"]]
        [:li 
         [:a {:class "text-gray-900 dark:text-white hover:underline"
              :href "#"}
          "Docs"]]
        [:li 
         [:a {:class "text-gray-900 dark:text-white hover:underline"
              :href "#"}
          "Examples"]]]]
      [:div
       [:a {:class "text-sm  text-blue-600 dark:text-blue-500 hover:underline"
            :href "#"
            :on-click (fn [e]
                        (.preventDefault e)
                        (re-frame/dispatch [::f-state/navigate ::f-state/login]))}
        "Login"]]]]))