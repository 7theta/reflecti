(ns reflecti.formal.ui.antd
  (:require [reflecti.formal :as formal]
            [reflecti.ant-design :as antd]
            [utilis.fn :refer [fsafe]]
            [reagent.core :as r]))

;;; Declarations

(declare antd-input-form-item antd-delete-confirm-icon-button)

;;; API

(defn input
  [props]
  [formal/input (assoc props :ui ::ui)])

;;; Inputs

(formal/definput ::ui :formal/text-input
  (fn [{:keys [on-blur
              on-focus
              default-value
              placeholder
              disabled?
              style
              on-change]
       :as props}]
    [antd-input-form-item props
     [antd/input
      (merge
       (when disabled? {:disabled true})
       {:default-value default-value
        :style (:input style)
        :on-change #(on-change (.-value (.-currentTarget %)))
        :on-focus on-focus
        :on-blur on-blur
        :placeholder placeholder})]]))

(formal/definput ::ui :formal/integer-input
  (fn [{:keys [min-value
              max-value
              disabled?
              default-value
              style
              on-focus
              on-blur
              on-change]
       :as props}]
    [antd-input-form-item props
     [antd/input-number
      (merge
       (when disabled? {:disabled true})
       (when min-value {:min min-value})
       (when max-value {:max max-value})
       {:default-value default-value
        :style (:input style)
        :on-focus on-focus
        :on-blur on-blur
        :on-change on-change})]]))

(formal/definput ::ui :formal/keys
  (fn [{:keys [submit? on-submit hide-submit?
              delete? on-delete
              container style]
       :as props}]
    (let [delete-button (when delete?
                          [:div {:style {:width "100%"
                                         :display "flex"
                                         :flex-direction "row"
                                         :justify-content "flex-end"}}
                           [antd-delete-confirm-icon-button
                            {:modal-body [:p "Click 'confirm' to delete."]
                             :modal-props {:title "Confirm Delete"}
                             :on-confirm (fn [_] ((fsafe on-delete)))}]])
          children (r/children (r/current-component))]
      (vec
       (concat
        (into
         [:div
          {:style
           (merge
            {:position "relative"}
            (:container style))}
          (if container
            (into
             [container
              (assoc props :delete-button delete-button)]
             children)
            delete-button)]
         (when-not container children))
        [[:div
          {:style
           {:display "flex"
            :flex-direction "row"
            :justify-content "center"}}
          (when-not hide-submit?
            [antd/button
             (merge
              (when-not submit? {:disabled true})
              {:style {:margin-top 8 :width 200}
               :html-type "submit"
               :type "primary"
               :on-click on-submit})
             "Submit"])]])))))

(formal/definput ::ui :formal/collection
  (fn [{:keys [on-add
              add-button-disabled?
              add-button-label]}]
    (into
     [:div
      {:style
       {:width "100%"
        :height "100%"}}
      [antd/button
       (merge
        (when add-button-disabled? {:disabled true})
        {:type "primary"
         :on-click on-add})
       add-button-label]]
     (r/children (r/current-component)))))

(formal/definput ::ui :formal/cascade
  (fn [{:keys [default-value
              placeholder
              options
              value
              on-change]
       :as props}]
    [antd-input-form-item props
     [antd/cascader
      {:style {:min-width 300}
       :allow-clear false
       :show-search false
       :expand-trigger "hover"
       :placeholder placeholder
       :disabled false
       :default-value default-value
       :options options
       :value value
       :on-change #(on-change (js->clj %))}]]))

(formal/definput ::ui :formal/select
  (fn [{:keys [value style on-change options] :as props}]
    [antd-input-form-item props
     (into
      [antd/select
       {:value value
        :style (merge {:min-width 200} (:select style))
        :on-select (fn [idx _] (on-change idx))}]
      (map-indexed
       (fn [idx {:keys [option-str option]}]
         [antd/select-option
          {:value idx
           :style (:select-option style)}
          option-str])
       options))]))

;;; Private

(defn- antd-input-form-item
  [{:keys [invalid-input?
           missing-input?
           invalid-input-message
           missing-input-message
           required?
           label
           style] :as props}]
  (into
   [antd/form-item
    (merge
     (when invalid-input?
       {:validate-status "error"
        :help invalid-input-message})
     (when missing-input?
       {:validate-status "error"
        :help missing-input-message})
     {:label label
      :required required?
      :colon false
      :style (merge
              {:margin-bottom 8}
              (:form-item style))})]
   (r/children (r/current-component))))

(defn- antd-delete-confirm-icon-button
  [{:keys [modal-props modal-body on-confirm style icon-props icon-style]
    :as props}]
  (let [this (r/current-component)
        {:keys [show-modal?] :or {show-modal? false}} (r/state this)
        click-handler (fn [e]
                        (r/set-state this {:show-modal? true})
                        (.blur (.-currentTarget e)))]
    [:div
     [antd/modal
      (merge
       {:visible (boolean show-modal?)
        :ok-text "Confirm"
        :cancel-text "Cancel"
        :title "Confirm"
        :on-ok (fn []
                 (r/set-state this {:show-modal? false})
                 ((fsafe on-confirm)))
        :on-cancel #(r/set-state this {:show-modal? false})}
       modal-props)
      (or modal-body [:p "Confirm action"])]
     [antd/button
      {:on-click (fn [e]
                   (.blur (.-currentTarget e))
                   (r/set-state this {:show-modal? true}))
       :style style
       :shape "circle"}
      [antd/icon
       (merge
        {:type "delete"
         :style (merge {:color "#999999"
                        :font-size 16
                        :margin-top 4
                        :padding 0}
                       icon-style)}
        icon-props)]]]))
