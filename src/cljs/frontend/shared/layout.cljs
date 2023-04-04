(ns frontend.shared.layout
  (:require [frontend.shared.bottom-header :refer [bottom-header]]
            [frontend.shared.flash-messages :refer [flash-messages]]
            [frontend.shared.top-header :refer [top-header]]
            ;; [frontend.shared.menu :refer [main-menu]]
            ))

(defn layout
  [children]
  [:div
   [:div {:class "md:flex md:flex-col"}
    [:div {:class "md:h-screen md:flex md:flex-col"}
     [:div {:class "md:flex md:flex-shrink-0"}
      [top-header]
      [:f> bottom-header]]
     [:div {:class "flex flex-grow overflow-hidden"}
      ;; [:f> main-menu {:class "flex-shrink-0 hidden w-56 p-12 overflow-y-auto bg-indigo-800 md:block"}]
      ;; To reset scroll region (https://inertiajs.com/pages#scroll-regions) add
      ;; `scroll-region="true"` to div below
      [:div {:class "w-full px-4 py-8 overflow-hidden overflow-y-auto md:p-12"}
       [:f> flash-messages]
       children]]]]])