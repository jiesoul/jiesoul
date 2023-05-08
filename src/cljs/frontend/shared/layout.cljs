(ns frontend.shared.layout
  (:require [frontend.routes.login :refer [login]]
            [frontend.shared.footer :refer [footer-home]]
            [frontend.shared.header :refer [header-dash nav-home]]
            [frontend.shared.modals :refer [modal modal-back]]
            [frontend.shared.sidebar :refer [sidebar-dash]]
            [frontend.shared.toasts :refer [timer-toasts toasts]]
            [frontend.state :as f-state]
            [re-frame.core :as re-frame]))

(defn layout-dash [children]
  (let [token @(re-frame/subscribe [::f-state/token])]
    (if token 
      [:div {:class "flex h-screen bg-gray-50 overflow-x-hidden"} 
       [sidebar-dash]
       [:div {:class "flex-1 flex flex-col w-full"} 
        [header-dash]
        [toasts] 
        [:main {:class "flex-1 bg-gray-100"} 
         [modal]
         [:div {:class "px-2 py-2 h-auto"}
          [:<> children]]]]
       [modal-back]]
      [login])))

(defn list-data [form table]
  [form]
  [table])

(defn admin-layout [main modal]
  (let [token @(re-frame/subscribe [::f-state/token])]
    (if token 
      [:div {:class "flex h-screen bg-gray-50 overflow-x-hidden"} 
       [sidebar-dash]
       [:div {:class "flex-1 flex flex-col w-full"} 
        [header-dash]
        [toasts]
        [:main {:class "flex-1 h-screen bg-gray-100"} 
         [modal]
         [:div {:class "px-2 py-2 h-screen"}
          [main]]]]
       [modal-back]]
      [login])))

(defn layout-home [children]
  [:div {:class "flex flex-col mx-auto w-full"}
   [nav-home]
   children
   [footer-home]])
