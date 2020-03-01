(ns reflecti.ant-design
  (:refer-clojure :exclude [comment empty list])
  (:require-macros [reflecti.ant-design.macros :refer [export-components]])
  (:require [reflecti.font-awesome :as fa]
            ["antd" :as antd]
            [reagent.core :as r]
            [inflections.core :refer [camel-case-keys]]
            [utilis.map :refer [compact]]))

(export-components)

(defn modal
  [{:keys [visible closable mask-closable width on-ok on-cancel title]} & body]
  (-> [(r/adapt-react-class antd/Modal)
       (-> {:visible visible
            :closable closable
            :mask-closable mask-closable
            :destroy-on-close true
            :width width
            :on-cancel on-cancel
            :title title}
           compact
           (assoc :footer nil))]
      (into body)
      (into (when (and on-ok on-cancel)
              [[:div {:style {:display "flex"
                              :flex-direction "row-reverse"}}
                [button {:type "primary"
                         :on-click on-ok
                         :style {:margin-left "8px"}} "OK"]
                [button {:on-click on-cancel} "Cancel"]]]))))

(defn modal-dialog
  [body on-ok & {:keys [icon on-cancel width mask-closable]
                 :or {mask-closable true}}]
  (antd/Modal.confirm
   (-> (merge
        {:title (r/as-element body)
         :mask-closable mask-closable
         :icon (r/as-element icon)
         :on-ok on-ok}
        (when on-cancel
          {:on-cancel on-cancel})
        (when width
          {:width width}))
       (camel-case-keys :lower)
       clj->js)))

(defn modal-warning
  [body on-ok & {:keys [on-cancel width]}]
  (modal-dialog [:div {:style {:display "flex"
                               :flex-direction "row"}}
                 [:div {:style {:margin-left "20px"
                                :float "left"}} body]]
                on-ok :on-cancel on-cancel :width width
                :icon [fa/icon {:type :solid
                                :name "exclamation-triangle"
                                :classes ["fa-3x"]
                                :style {:color "red"
                                        :float "left"}}]))

(defn modal-confirm
  [body on-ok & {:keys [on-cancel width]}]
  (modal-dialog [:div {:style {:display "flex"
                               :flex-direction "row"}}
                 [:div {:style {:margin-left "20px"
                                :float "left"}} body]]
                on-ok :on-cancel on-cancel :width width
                :icon [fa/icon {:type :solid
                                :name "question-circle"
                                :classes ["fa-3x"]
                                :style {:color "orange"
                                        :float "left"}}]))

(defn message-success
  [content duration on-close]
  (antd/message.success
   (cond-> content
     (not (string? content)) r/as-element)
   duration on-close))

(defn message-error
  [content duration on-close]
  (antd/message.error
   (cond-> content
     (not (string? content)) r/as-element)
   duration on-close))

(defn message-info
  [content duration on-close]
  (antd/message.info
   (cond-> content
     (not (string? content)) r/as-element)
   duration on-close))

(defn message-warning
  [content duration on-close]
  (antd/message.warning
   (cond-> content
     (not (string? content)) r/as-element)
   duration on-close))

(defn message-loading
  [content duration on-close]
  (antd/message.loading
   (cond-> content
     (not (string? content)) r/as-element)
   duration on-close))

(defn notification-success
  [config]
  (antd/notification.success
   (-> config
       (assoc :icon (r/as-element [fa/icon {:type :light
                                            :name "check-circle"
                                            :style {:color "green"
                                                    :float "left"
                                                    :font-size 36}}]))
       (camel-case-keys :lower)
       clj->js)))

(defn notification-error
  [config]
  (antd/notification.error
   (-> config
       (assoc :icon (r/as-element [fa/icon {:type :light
                                            :name "exclamation-circle"
                                            :style {:color "red"
                                                    :float "left"
                                                    :font-size 36}}]))
       (camel-case-keys :lower)
       clj->js)))

(defn notification-info
  [config]
  (antd/notification.info
   (-> config
       (assoc :icon (r/as-element [fa/icon {:type :light
                                            :name "info-circle"
                                            :style {:color "#1890ff"
                                                    :float "left"
                                                    :font-size 36}}]))
       (camel-case-keys :lower)
       clj->js)))

(defn notification-warning
  [config]
  (antd/notification.warning
   (-> config
       (assoc :icon (r/as-element [fa/icon {:type :light
                                            :name "exclamation-triangle"
                                            :style {:color "orange"
                                                    :float "left"
                                                    :font-size 36}}]))
       (camel-case-keys :lower)
       clj->js)))
