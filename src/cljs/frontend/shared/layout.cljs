(ns frontend.shared.layout
  (:require [frontend.routes.login :refer [login]]
            [frontend.shared.footer :refer [footer-home]]
            [frontend.shared.header :refer [header-dash nav-home]]
            [frontend.shared.sidebar :refer [sidebar-dash]]
            [re-frame.core :as re-frame]
            [frontend.state :as f-state]))

(defn layout-dash
  [children]
  (let [token @(re-frame/subscribe [::f-state/token])]
    (if token 
      [:div {:class "flex h-screen bg-gray-50 font-boboto"}
       [sidebar-dash]
       [:div {:class "flex-1 flex flex-col w-full overflow-x-hidden"}
        [header-dash]
        [:main {:class "flex-1 overflow-x-hidden overflow-y-auto bg-gray-100"}
         [:div {:class "container mx-auto px-4 py-6"}
          children]]]]
      [login])))

(defn layout-home 
  [children]
  [:div {:class "flex flex-col mx-auto w-full"}
   [nav-home]
   children
   [footer-home]])
