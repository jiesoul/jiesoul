(ns frontend.shared.header 
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
            [frontend.state :as f-state]))

(defn header-dash []
  (fn []
    (let []
      [:header {:class "flex items-center justify-between px-6 py-4 bg-white border-b-4 border-indigo-600"}
       [:div {:class "flex items-center"}
        [:div {:class "relative mx-4 lg:mx-0"}
         [:span {:class "absolute inset-y-0 left-0 flex items-center pl-3"}]
         [:input {:class "w-32 pl-10 pr-4 rounded-md form-input sm:w-64 focus:border-indigo-600"
                  :type "text"
                  :placeholder "Search"}]]]
       [:div {:class "flex items-center"}
        [:a {:class ""} "jiesoul"]]])))

(defn nav-home []
  (fn []
    [:nav {:class "flex items-center justify-between flex-wrap bg-teal-500 p-6"}
     [:div {:class "flex items-center flex-shrink-0 text-white mr-6"}
      [:span {:class "font-semibold text-xl tracking-tight"}
       "Site"]]
     [:div {:class "block lg:hidden"}
      ]
     [:div {:class "w-full block flex-grow lg:flex lg:items-center lg:w-auto"}
      [:div {:class "ext-sm lg:flex-grow"}
       [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
            :href "#"}
        "Blog"]
       [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
            :href "#"}
        "Docs"]
       [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
            :href "#"}
        "Examples"]]]
     [:div
      [:button {:class "inline-block text-sm px-4 py-2 leading-none border rounded 
                        text-white border-white hover:border-transparent hover:text-teal-500 
                        hover:bg-white mt-4 lg:mt-0"
                :type "button"
                :date-ripple-ligth true
                :on-click (fn [e]
                            (.preventDefault e)
                            (re-frame/dispatch [::f-state/navigate ::f-state/login]))}
       "Login"]]]))