(ns jiesoul.templates.app
  (:require [hiccup.page :as page]
            [clojure.edn :as edn]))

(defn js-script []
  (-> (slurp "public/js/mainfest.cdn")
      edn/read-string
      first 
      :output-name))

(defn template [data-page]
  (page/html5 {:class "h-full bg-gray-100"}
              [:head 
               [:meta {:charset "utf-8"}]
               (page/include-css "/css/app.css")
               [:script {:src (str "/js/" (js-script)) :defer true}]]
              [:body.font-sans.leading-none.text-grey-700.antialiased
               [:div {:id "app" :data-page data-page}]]))