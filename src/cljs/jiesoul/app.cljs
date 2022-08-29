(ns jiesoul.app
  (:require [jiesoul.pages.dashboard :as dashboard]
            [jiesoul.pages.login :refer [login]]
            [jiesoul.shared.layout :refer [layout]]
            [reagent.core :as r]
            [reagent.dom :as d]))

(def pages {"Dashboard/Index" dashboard/index
            "Auth/Login" login})

(defn init! []
  (createInertiaApp
   #js {:resolve (fn [name]
                   (let [^js comp (r/reactify-component (get pages name))]
                     (when-not (= name "Auth/Login")
                       (set! (.-layout comp) (fn [page] (r/as-element [layout page]))))
                     comp))
        :title (fn [title] (str title " | Ping CRM"))
        :setup (j/fn [^:js {:keys [el App props]}]
                 (d/render (r/as-element [:f> App props]) el))}))

(defn ^:dev/after-load reload []
  (.reload Inertia))