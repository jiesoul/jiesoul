(ns frontend.shared.footer
  (:require [frontend.state :as f-state]
            [re-frame.core :as re-frame]))

(defn footer-home []
  [:footer {:class "w-full bg-white p-8"}
   [:div {:class "flex flex-row flex-wrap items-center justify-center 
                  gap-y-6 gap-x-12 bg-white text-center md:justify-between"}
    [:ul {:class "flex flex-wrap items-center gap-y-2 gap-x-8"}
     [:li
      [:a {:class "block font-sans text-base font-normal leading-relaxed 
                   text-blue-gray-900 antialiased transition-colors 
                   hover:text-pink-500 focus:text-pink-500"
           :href "#"}
       "About Me"]]

     [:li
      [:a {:class "block font-sans text-base font-normal leading-relaxed 
                   text-blue-gray-900 antialiased transition-colors 
                   hover:text-pink-500 focus:text-pink-500"
           :href "#"}
       "License"]]

     [:li
      [:a {:class "block font-sans text-base font-normal leading-relaxed 
                   text-blue-gray-900 antialiased transition-colors 
                   hover:text-pink-500 focus:text-pink-500"
           :href "#"}
       "Contact Me"]]]]
       
       [:hr {:class "my-8 border-blue-gray-50 m-6"}]
       [:p {:class "block text-center font-sans text-base font-normal 
                    leading-relaxed text-blue-gray-900 antialiased"}
        "© 2023 "]])