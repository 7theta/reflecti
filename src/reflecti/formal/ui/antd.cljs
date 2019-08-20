(ns reflecti.formal.ui.antd
  (:require [reflecti.formal :as formal]
            [reflecti.ant-design :as antd]
            [utilis.fn :refer [fsafe]]
            [utilis.types.number :refer [string->long]]
            [reagent.core :as r]
            [clojure.string :as st]))

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

(formal/definput ::ui :formal/boolean-input
  (fn [{:keys [disabled?
              default-value
              style
              on-change]
       :as props}]
    [antd-input-form-item props
     [antd/switch
      (merge
       (when disabled? {:disabled true})
       {:checked default-value
        :style (:input style)
        :on-change on-change})]]))

(formal/definput ::ui :formal/delete-button
  (fn [{:keys [on-delete]}]
    [antd-delete-confirm-icon-button
     {:modal-body [:p "Click 'confirm' to delete."]
      :modal-props {:title "Confirm Delete"}
      :on-confirm (fn [_] ((fsafe on-delete)))}]))

(formal/definput ::ui :formal/submit-button
  (fn [{:keys [submit? on-submit] :as props}]
    [antd/button
     (merge
      (when-not submit? {:disabled true})
      {:style {:margin-top 8
               :width "100%"}
       :html-type "submit"
       :type "primary"
       :on-click on-submit})
     (or (:submit-label (formal/spec-props props))
         "Submit")]))

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

(defn inner-auto-complete
  [{:keys [options option? on-change default-value on-focus on-blur]}]
  (let [this (r/current-component)
        {:keys [text]} (r/state this)]
    [antd/auto-complete
     {:default-value default-value
      :style {:min-width 200}
      :on-focus on-focus
      :on-blur on-blur
      :dataSource (cond->> options
                    (seq (st/trim (str text))) (cons {:value :input-text :text text}))
      :on-search #(r/set-state this {:text (when (not (option? %)) %)})
      :on-select (fn [value]
                   (if-let [idx (string->long value)]
                     (on-change idx)
                     (on-change {:option text :option-str text})))}]))

(formal/definput ::ui :formal/auto-complete
  (fn [{:keys [value default-value style on-change options on-focus on-blur] :as props}]
    (let [options (map-indexed
                   (fn [idx {:keys [option option-str]}]
                     {:value idx
                      :text option-str})
                   options)]
      [antd-input-form-item props
       [inner-auto-complete
        {:options options
         :option? (->> options (map :text) set)
         :on-focus on-focus
         :on-blur on-blur
         :default-value default-value
         :on-change on-change}]])))

(formal/definput ::ui :formal/tags
  (fn [{:keys [value disabled? default-value style on-change on-focus on-blur placeholder] :as props}]
    (let [this (r/current-component)
          {:keys [text]} (r/state this)
          ds (if text [(str text)] [])]
      (prn :ds ds :tags-value value)
      [antd-input-form-item props
       [:div {:style {:display "flex"
                      :flex-direction "row"
                      :align-items "center"
                      :justify-content "flex-start"}}
        (->> value
             (map-indexed
              (fn [idx tag]
                [antd/tag
                 {:key (str idx "-" tag)}
                 tag]))
             doall)
        [antd/auto-complete
         {:style {:min-width 200}
          :on-focus on-focus
          :on-blur on-blur
          :dataSource ds
          :on-search #(r/set-state this {:text %})
          :on-select (fn [tag]
                       (prn :value value :select tag)
                       (on-change (conj (vec value) tag))
                       (r/set-state this {:text ""}))}]]])))

;;; Private

(defn- antd-input-form-item
  [{:keys [invalid-input?
           missing-input?
           invalid-input-message
           missing-input-message
           required?
           label
           style
           form-item-props] :as props}]
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
              (:form-item style))}
     form-item-props)]
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
