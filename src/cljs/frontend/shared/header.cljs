(ns frontend.shared.header 
  (:require [cljs.pprint]
            [frontend.shared.css :as css]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [frontend.util :as f-util]))

(def css-user-dropdown-li-a "block px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white")
(def user-dropdown-show? (r/atom true))

(def nav-home-link "block py-2 pl-3 pr-4 text-gray-900 rounded hover:bg-gray-100 md:hover:bg-transparent 
                    md:border-0 md:hover:text-blue-700 md:p-0 dark:text-white md:dark:hover:text-blue-500 
                    dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:bg-transparent")

(def nav-home-link-current "block py-2 pl-3 pr-4 text-white border-b bg-blue-700 rounded md:bg-transparent 
                            md:text-blue-700 md:p-0 dark:text-white md:dark:text-blue-500")

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
  (let [current-route @(re-frame/subscribe [::f-state/current-route])
        link-text (get-in current-route [:data :link-text])]
    [:nav {:class "w-full shadow bg-white border-gray-200 dark:bg-gray-900 z-20 fixed"}
     [:div {:class "max-w-5xl flex items-center justify-between mx-auto p-4"}
      [:a {:href (f-util/href ::f-state/home)
           :class "flex items-center"}
       [:span {:class "self-center text-2xl font-semibold whitespace-nowrap dark:text-white"}
        "Jiesoul"]] 
      [:div {:class ""
             :id "navbar-default"}
       [:ul {:class "flex flex-row font-medium mt-0 mr-6 space-x-8 text-xl"}
        [:li 
         [:a {:class (if (= "Home" link-text) nav-home-link-current nav-home-link)
              :href (f-util/href ::f-state/home)}
          "主页"]]
        [:li 
         [:a {:class (if (= "Archive" link-text) nav-home-link-current nav-home-link)
              :href (f-util/href ::f-state/archive)}
          "归档"]]
        [:li 
         [:a {:class (if (= "About" link-text) nav-home-link-current nav-home-link)
              :href (f-util/href ::f-state/about)}
          "关于"]]]]
      ]]))