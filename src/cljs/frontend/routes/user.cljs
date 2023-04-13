(ns frontend.routes.user 
  (:require [frontend.shared.layout :refer [layout-dash]]))

(defn index
  []
  (layout-dash
   [:<>]))