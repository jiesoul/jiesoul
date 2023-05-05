(ns frontend.routes.article
    (:require [clojure.string :as str]
              [frontend.http :as f-http]
              [frontend.routes.category :as category]
              [frontend.shared.buttons :refer [default-button delete-button
                                               edit-button new-button red-button]]
              [frontend.shared.css :as css]
              [frontend.shared.form-input :refer [file-input select-input
                                                  text-input text-input-backend
                                                  textarea]]
              [frontend.shared.layout :refer [layout-dash]]
              [frontend.shared.modals :as modals :refer [default-modal]]
              [frontend.shared.page :refer [page-dash]]
              [frontend.shared.tables :refer [css-list-table-tbody-tr
                                              table-dash td-dash th-dash]]
              [frontend.shared.toasts :as toasts]
              [frontend.state :as f-state]
              [frontend.util :as f-util]
              [re-frame.core :as re-frame]
              [reagent.core :as r]))

(def name-error (r/atom nil))

(re-frame/reg-event-db
 ::init
 (fn [db _]
   (assoc db :article nil)))

(re-frame/reg-sub
 ::new-modal-show?
 (fn [db]
   (get-in db [:article :new-modal-show?])))

(re-frame/reg-event-fx
 ::show-new-modal
 (fn [{:keys [db]} [_ show?]]
   {:db (-> db
            (assoc-in [:article :new-modal-show?] show?)
            (assoc-in [:modal :show?] show?))}))

(re-frame/reg-sub
 ::edit-modal-show?
 (fn [db]
   (get-in db [:article :edit-modal-show?])))

(re-frame/reg-event-db
 ::show-edit-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:article :edit-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::delete-modal-show?
 (fn [db]
   (get-in db [:article :delete-modal-show?])))

(re-frame/reg-event-db
 ::show-delete-modal
 (fn [db [_ show?]]
   (-> db
       (assoc-in [:article :delete-modal-show?] show?)
       (assoc :modal-backdrop-show? show?))))

(re-frame/reg-sub
 ::articles-list
 (fn [db]
   (get-in db [:article :list])))

(re-frame/reg-event-db
 ::query-articles-ok
 (fn [db [_ resp]]
   (assoc-in db [:article :list] (:data resp))))

(re-frame/reg-event-fx
 ::query-articles
 (fn [{:keys [db]} [_ data]]
   (f-util/clog "query articles: " data)
   (f-http/http-get db
                    (f-http/api-uri "/articles")
                    data
                    ::query-articles-ok)))

(re-frame/reg-event-fx
 ::add-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "add article ok: " resp)
   {:db (-> db
            (update-in [:toasts] conj {:content "保存成功" :type :info}))}))

(re-frame/reg-event-fx
 ::add-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "add article: " article)
   (f-http/http-post db
                     (f-http/api-uri "/articles")
                     {:article article}
                     ::add-article-ok)))

(re-frame/reg-event-db
 ::get-article-ok
 (fn [db [_ resp]]
   (f-util/clog "get article ok: " resp)
   (assoc-in db [:article :current] (:article (:data resp)))))

(re-frame/reg-event-fx
 ::get-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Get a article")
   (f-http/http-get db
                    (f-http/api-uri "/articles/" id)
                    {}
                    ::get-article-ok)))

(re-frame/reg-sub
 ::article-current
 (fn [db]
   (get-in db [:article :current])))

(re-frame/reg-event-db
 ::reset-current
 (fn [db [_ k v]]
   (assoc-in db [:article :current k] v)))

(re-frame/reg-event-fx
 ::update-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "update article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:content "保存成功"
                                    :type :success}]]]}))

(re-frame/reg-event-fx
 ::update-article
 (fn [{:keys [db]} [_ article]]
   (f-util/clog "update article: " article)
   (f-http/http-put db
                    (f-http/api-uri "/articles/" (:id article))
                    {:article article}
                    ::update-article-ok
                    ::f-state/req-failed-message)))

(re-frame/reg-event-db
 ::clean-current
 (fn [db _]
   (assoc-in db [:article :current] nil)))

(re-frame/reg-event-fx
 ::delete-article-ok
 (fn [{:keys [db]} [_ resp]]
   (f-util/clog "delete article ok: " resp)
   {:db db
    :fx [[:dispatch [::toasts/push {:type :success
                                    :content "Delete success"}]]
         [:dispatch [::clean-current]]
         [:dispatch [::show-delete-modal false]]]}))

(re-frame/reg-event-fx
 ::delete-article
 (fn [{:keys [db]} [_ id]]
   (f-util/clog "Delete article")
   (f-http/http-delete db
                       (f-http/api-uri "/articles/" id)
                       {}
                       ::delete-article-ok)))

(defn check-name [v]
  (f-util/clog "check name")
  (if (or (nil? v) (str/blank? v))
    (reset! name-error "名称不能为空")
    (reset! name-error nil)))

(defn edit-layout [title children]
  (layout-dash
   [:section {:class "bg-white dark:bg-gray-900 overflow-y-auto"}
    [:div {:class "py-2 px-2 mx-auto max-w-2xl lg:py-8"}
     [:h2 {:class "mb-2 text-xl font-bold text-gray-900 dark:text-white"}
      title]
     children]]))

(defn new-form [] 
  (let [login-user @(re-frame/subscribe [::f-state/login-user])
        article (r/atom {:title ""
                         :author (:username login-user)
                         :like_count 0
                         :read_count 0
                         :comment_count 0
                         :summary ""
                         :detail {:content_md ""}})]
    [:form
     [:div {:class "flex-l flex-col"} 
      [text-input {:class "pt-2"
                   :name "title"
                   :placeholder "Title"
                   :required ""
                   :on-change #(swap! article assoc :title (f-util/get-value %))}]
      
      [textarea {:class "pt-4"
                 :placeholder "Summary"
                 :name "summary"
                 :required ""
                 :on-change #(swap! article assoc :summary (f-util/get-value %))}] 
      
      ;;  [file-input {:class "pt-4"
      ;;               :help "md"}]
      [textarea {:class "pt-4"
                 :placeholder "Content"
                 :rows 8
                 :name "content"
                 :on-change #(swap! article assoc-in [:detail :content_md] (f-util/get-trim-value %))}]
      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [new-button {:on-click #(re-frame/dispatch [::add-article @article])}
        "Save"]]]]))

(defn edit-form []
  (let [login-user @(re-frame/subscribe [::f-state/login-user])
        origin-article @(re-frame/subscribe [::article-current])
        article (r/atom origin-article)
        title (r/cursor article [:title])
        summary (r/cursor article [:summary])
        content (r/cursor article [:detail :content-md])]
    [:form
     [:div {:class "flex-l flex-col"}
      [text-input {:class "pt-2"
                   :placeholder "Title"
                   :name "title"
                   :required ""
                   :default-value @title
                   :on-change #(swap! article assoc :title (f-util/get-value %))}]

      [textarea {:class "pt-4"
                 :placeholder "Summary"
                 :name "summary"
                 :required ""
                 :default-value @summary
                 :on-change #(swap! article assoc :summary (f-util/get-value %))}]

      [textarea {:class "pt-4"
                 :placeholder "Content"
                 :rows 8
                 :name "content"
                 :default-value @content
                 :on-change #(swap! article assoc-in [:detail :content_md] (f-util/get-trim-value %))}]
      [:div {:class "flex justify-center items-center space-x-4 mt-4"}
       [new-button {:on-click #(re-frame/dispatch [::update-article @article])}
        "Save"]]]]))

(defn push []
  (let [login-user @(re-frame/subscribe [::f-state/login-user])
        article (r/atom {:title ""
                         :author (:username login-user)
                         :like-count 0
                         :read-count 0
                         :summary ""
                         :detail {:content-md ""}})
        {:keys [categories]} @(re-frame/subscribe [::category/categories-list])]
    (edit-layout "Push Article"
     [:form
      [:div {:class "flex-l flex-col"}
       [text-input {:class "pt-4"
                    :label "Title"
                    :name "title"
                    :required ""
                    :on-change #(swap! article assoc :title (f-util/get-value %))}]

       [text-input {:class "pt-4"
                    :label "Tags"
                    :name "tags"
                    :required ""
                    :on-change #(swap! article assoc :tags (f-util/get-value %))}]

       [select-input {:class "pt-4"
                      :label "Category"
                      :required ""
                      :name "category"}
        [:option "select category"]
        (for [c categories]
          [:option {:value (:id c)} (:name c)])]
       [:div {:class "flex justify-center items-center space-x-4 mt-4"}
        [new-button {:on-click #(re-frame/dispatch [::add-article @article])}
         "Add"]]]])))

(defn delete-form []
  (let [current (re-frame/subscribe [::article-current])
        title (r/cursor current [:title])]
    [:form
     [:div {:class "p-4 mb-4 text-blue-800 border border-red-300 rounded-lg 
                    bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800"}
      [:div {:class "flex items-center"}
       (str "You confirm delete the " @title "? ")]]
     [:div {:class "flex justify-center items-center space-x-4"}
      [red-button {:on-click #(do
                                (re-frame/dispatch [::delete-article (:id @current)]))}
       "Delete"]]]))

(defn index []
  (let [new-modal-show? @(re-frame/subscribe [::new-modal-show?])
        edit-modal-show? @(re-frame/subscribe [::edit-modal-show?])
        delete-modal-show? @(re-frame/subscribe [::delete-modal-show?])
        q-data (r/atom {:page-size 10 :page 1 :filter "" :sort ""})
        filter (r/cursor q-data [:filter])]
    (layout-dash
     [:div {:class css/main-container}
      ;; page title
      [:h4 {:class css/page-title} "Articles"]

      ;; page query form
      [:form
       [:div {:class "flex-1 flex-col my-2 py-2 overflow-x-auto sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"}
        [:div {:class "grid grid-cols-4 gap-3"}
         [:div {:class "max-w-10 flex"}
          (text-input-backend {:label "Title："
                               :type "text"
                               :id "title"
                               :on-blur #(when-let [v (f-util/get-trim-value %)]
                                           (swap! filter str " name lk " v))})]]
        [:div {:class "flex inline-flex justify-center items-center w-full"}
         [default-button {:on-click #(re-frame/dispatch [::query-articles @q-data])}
          "Query"]
         [new-button {:on-click #(re-frame/dispatch [::show-new-modal true])} 
          "New"]]]]

      ;; modals
      [:div 
       
       [modals/modal  {:id "new-article"
                       :title "New article"
                       :show? new-modal-show?
                       :on-close #(do
                                    (re-frame/dispatch [::clean-current])
                                    (re-frame/dispatch [::show-new-modal false]))}
        [new-form]]
       [modals/modal  {:id "update-article"
                       :title "Update article"
                       :show? edit-modal-show?
                       :on-close #(do
                                    (re-frame/dispatch [::clean-current])
                                    (re-frame/dispatch [::show-edit-modal false]))}
        [edit-form]]
       [modals/modal  {:id "Delete-article"
                       :title "Delete article"
                       :show? delete-modal-show?
                       :on-close #(do
                                    (re-frame/dispatch [::clean-current])
                                    (re-frame/dispatch [::show-delete-modal false]))}
        [delete-form]]]
      ;; hr
      [:div {:class "h-px my-4 bg-blue-500 border-0 dark:bg-blue-700"}]

      ;; data table
      [:div
       (let [{:keys [articles opts total]} @(re-frame/subscribe [::articles-list])
             page (:page opts)
             page-size (:page-size opts)]
         (table-dash
          [:tr
           [th-dash "ID"]
           [th-dash "Name"]
           [th-dash "Author"]
           [th-dash "Like-count"]
           [th-dash "Read-count"]
           [th-dash "Comment-count"]
           [th-dash "Create-time"]
           [th-dash "Top"]
           [th-dash "操作"]]
          (for [{:keys [id] :as c} articles]
            [:tr {:class css-list-table-tbody-tr}
             [td-dash id]
             [td-dash (:title c)]
             [td-dash (:author c)]
             [td-dash (:like-count c)]
             [td-dash (:read-count c)]
             [td-dash (:comment-count c)]
             [td-dash (:create-time c)]
             [td-dash (:top-flag c)]
             [td-dash
              [:<>
               [edit-button {:on-click #(do 
                                          (re-frame/dispatch [::get-article id])
                                          (re-frame/dispatch [::show-edit-modal true]))
                             :target "_blank"}
                "Edit"]
               [:span " | "]
               [delete-button {:on-click #(do
                                            (re-frame/dispatch [::get-article (:id c)])
                                            (re-frame/dispatch [::show-delete-modal true]))}
                "Del"]]]]) 
          [page-dash {:page page
                      :page-size page-size
                      :total total
                      :opts opts
                      :url ::query-articles}]))]])))


