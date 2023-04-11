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
    [:nav {:class "sticky inset-0 z-10 block h-max w-full max-w-full rounded-none border border-white/80 bg-white bg-opacity-80 py-2 px-4 text-blank shadow-md backdrop-blur-2xl backdrop-saturate-200 lg:px-8 lg:py-4"}
     [:div {:class "flex items-center text-gray-900"}
      [:a {:class "mr-4 block cursor-pointer py-1.5 font-sans text-base font-medium leading-relaxed text-inherit antialiased"}
       "Site"]
      [:ul {:class "ml-auto mr-8 hidden items-center gap-6 lg:flex"}
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
             :href "#"}
         "Blog"]]
       [:li {:class "block p-1 font-sans text-sm font-normal leading-normal text-inherit antialiased"}
        [:a {:class "block mt-4 lg:inline-block lg:mt-0 text-teal-400 hover:text-white mr-4"
             :href "#"}
         "Blog"]]]
      [:button {:class "middle none center hidden rounded-lg bg-gradient-to-tr from-green-600 to-green-400 
                        py-2 px-4 font-sans text-xs font-bold uppercase text-white shadow-md shadow-green-500/20 
                        transition-all hover:shadow-lg hover:shadow-green-500/40 active:opacity-[0.85] 
                        disabled:pointer-events-none disabled:opacity-50 disabled:shadow-none lg:inline-block"
                :type "button"
                :date-ripple-ligth true
                :on-click (fn [e]
                            (.preventDefault e)
                            (re-frame/dispatch [::f-state/navigate ::f-state/login]))}
       "Login"]]]))