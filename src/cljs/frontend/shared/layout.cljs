(ns frontend.shared.layout
  (:require [frontend.routes.login :refer [login]]
            [frontend.shared.footer :refer [footer-home]]
            [frontend.shared.header :refer [header-dash nav-home]]
            [frontend.shared.sidebar :refer [sidebar-dash]]
            [frontend.shared.modals :refer [modal]]
            [re-frame.core :as re-frame]
            [frontend.state :as f-state]))

(defn layout-dash
  [children]
  (let [token @(re-frame/subscribe [::f-state/token])
        modal-backdrop-show? @(re-frame/subscribe [::f-state/modal-backdrop-show?])]
    (if token 
      [:div {:class "flex h-screen bg-gray-50 font-boboto"}
       [sidebar-dash]
       [:div {:class "flex-1 flex flex-col w-full overflow-x-hidden"}
        [header-dash]
        [:main {:class "flex-1 overflow-x-hidden overflow-y-auto bg-gray-100"} 
         [modal]
         [:div {:class "container mx-auto px-4 py-4"}
          children]]]
       [:div {:modal-backdrop true
              :class (str (if modal-backdrop-show? "" "hidden ") "bg-gray-900 bg-opacity-50 dark:bg-opacity-80 fixed inset-0 z-40")}]]
      [login])))

(defn layout-home 
  [children]
  [:div {:class "flex flex-col mx-auto w-full"}
   [nav-home]
   children
   [footer-home]])
