(ns frontend.routes.index
  (:require [frontend.routes.article :refer [articles-archive articles-home]]
            [frontend.shared.layout :refer [layout-home]]))

(defn home-page []  
  [layout-home
   [articles-home]])

(defn archive-page []
  [layout-home 
   [articles-archive]])
