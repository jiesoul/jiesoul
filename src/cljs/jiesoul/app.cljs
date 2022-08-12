(ns cljs.jiesoul.app
  (:require ["@inertiajs/inertia" :refer [Inertia]]
            ["@inertiajs/inertia-react" :refer [createInertiaApp]]
            ["@inertiajs/progress" :refer [InertiaProgress]]
            [applied-science.js-interop :as j]
            [reagent.core :as r]
            [reagent.dom :as d]
            [pingcrm.shared.layout :refer [layout]]
            [jiesoul.pages.login :refer [login]]))

(.init InertiaProgress)

(def pages {"Auth/Login" login})

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