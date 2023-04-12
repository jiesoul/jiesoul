(ns frontend.shared.layout
  (:require [frontend.shared.header :refer [header-dash nav-home]]
            [frontend.shared.sidebar :refer [sidebar-dash]]
            [frontend.shared.footer :refer [footer-home]]))

(defn layout-dash
  [children]
  [:div {:class "flex h-screen bg-gray-200 font-boboto"}
   [sidebar-dash]
   [:div {:class "flex-1 flex flex-col w-full overflow-x-hidden"}
    [header-dash]
    [:main {:class "flex-1 overflow-x-hidden overflow-y-auto bg-gray-200"}
     [:div {:class "container mx-auto px-6 py-8"}
      children]]]])

(defn layout-home 
  [children]
  [:div {:class "flex flex-col mx-auto w-full"}
   [nav-home]
   children
   [footer-home]])
