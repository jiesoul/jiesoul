(ns frontend.shared.header 
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
            [frontend.state :as f-state]))

(defn header []
  (fn []
    (let []
      [:div])))

(defn nav []
  (fn []
    [:nav {:class "sticky inset-0 z-10 block h-max w-full max-w-full rounded-none border border-white/80 bg-white bg-opacity-80 py-2 px-4 text-white shadow-md backdrop-blur-2xl backdrop-saturate-200 lg:px-8 lg:py-4"}
     [:div {:class "flex items-center text-gray-900"}
      [:a {:class "mr-4 block cursor-pointer py-1.5 font-sans text-base font-medium leading-relaxed text-inherit antialiased"}
       "site"]
      [:ul {:class "ml-auto mr-8 hidden items-center gap-6 lg:flex"}
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-200 hover:text-white mr-4"
             :href "#"}
         "Blog"]]]]]))