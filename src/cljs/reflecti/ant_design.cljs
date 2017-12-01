(ns reflecti.ant-design
  (:require [reagent.core :refer [adapt-react-class as-element]]
            [cljsjs.antd]))

;;;
;;; General
;;;

(def button
  "https://ant.design/components/button/"
  (adapt-react-class js/antd.Button))

(def button-group
  "https://ant.design/components/button/"
  (adapt-react-class js/antd.Button.Group))

;; -

(def icon
  "https://ant.design/components/icon/"
  (adapt-react-class js/antd.Icon))


;;;
;;; Layout
;;;

(def col
  "https://ant.design/components/grid/#Col"
  (adapt-react-class js/antd.Col))

(def row
  "https://ant.design/components/grid/#Row"
  (adapt-react-class js/antd.Row))

;; -

(def layout
  "https://ant.design/components/layout/#Layout
    note: need to explicity add `:class-name \"ant-layout-has-sider\"`
          if a sider is being used"
  (adapt-react-class js/antd.Layout))

(def sider
  "https://ant.design/components/layout/#Layout.Sider"
  (adapt-react-class js/antd.Layout.Sider))

(def header
  "https://ant.design/components/layout/"
  (adapt-react-class js/antd.Layout.Header))

(def footer
  "https://ant.design/components/layout/"
  (adapt-react-class js/antd.Layout.Footer))

(def content
  "https://ant.design/components/layout/"
  (adapt-react-class js/antd.Layout.Content))


;;;
;;; Navigation
;;;

(def affix
  "https://ant.design/components/affix/"
  (adapt-react-class js/antd.Affix))

;; -

(def breadcrumb
  "https://ant.design/components/breadcrumb/"
  (adapt-react-class js/antd.Breadcrumb))

(def breadcrumb-item
  "https://ant.design/components/breadcrumb/"
  (adapt-react-class js/antd.Breadcrumb.Item))

;; -

(defn dropdown
  "https://ant.design/components/dropdown/#Dropdown"
  [props]
  [(adapt-react-class js/antd.Dropdown)
   (update props :overlay as-element)])

(defn dropdown-button
  "https://ant.design/components/dropdown/#Dropdown.Button"
  [props]
  [(adapt-react-class js/antd.Dropdown.Button)
   (update props :overlay as-element)])

;; -

(defn menu
  "https://ant.design/components/menu/#Menu"
  [props & children]
  (into
   [(adapt-react-class js/antd.Menu)
    (-> props
        (update :default-open-keys clj->js)
        (update :default-selected-keys clj->js)
        (update :selected-keys clj->js))]
   children))

(def menu-item
  "https://ant.design/components/menu/#Menu.Item"
  (adapt-react-class js/antd.Menu.Item))

(def sub-menu
  "https://ant.design/components/menu/#Menu.SubMenu"
  (adapt-react-class js/antd.Menu.SubMenu))

(def menu-item-group
  "https://ant.design/components/menu/#Menu.ItemGroup"
  (adapt-react-class js/antd.Menu.ItemGroup))

(def menu-divider
  "https://ant.design/components/menu/#Menu.Divider"
  (adapt-react-class js/antd.Menu.Divider))

;; -

(def pagination
  "https://ant.design/components/pagination/"
  (adapt-react-class js/antd.Pagination))

;; -

(def steps
  "https://ant.design/components/steps/#Steps"
  (adapt-react-class js/antd.Steps))

(def step
  "https://ant.design/components/steps/#Steps.Step"
  (adapt-react-class js/antd.Steps.Step))


;;;
;;; Data Entry
;;;

(def auto-complete
  "https://ant.design/components/auto-complete/"
  (adapt-react-class js/antd.AutoComplete))

;;-

(def cascader
  "https://ant.design/components/cascader/"
  (adapt-react-class js/antd.Cascader))

;; -

(def checkbox
  "https://ant.design/components/checkbox/#Checkbox"
  (adapt-react-class js/antd.Checkbox))

(def checkbox-group
  "https://ant.design/components/checkbox/#Checkbox-Group"
  (adapt-react-class js/antd.Checkbox.Group))

;; -

(def date-picker
  "https://ant.design/components/date-picker/#DatePicker"
  (adapt-react-class js/antd.DatePicker))

(def month-picker
  "https://ant.design/components/date-picker/#MonthPicker"
  (adapt-react-class js/antd.DatePicker.MonthPicker))

(def range-picker
  "https://ant.design/components/date-picker/#RangePicker"
  (adapt-react-class js/antd.DatePicker.RangePicker))

;; -

(def form
  "https://ant.design/components/form/#Form"
  (adapt-react-class js/antd.Form))

(defn form-item
  "https://ant.design/components/form/#Form.Item"
  [props & children]
  (into
   [(adapt-react-class js/antd.Form.Item)
    (-> props
        (update :help as-element)
        (update :extra as-element))]
   children))

;; -

(def input-number
  "https://ant.design/components/input-number/"
  (adapt-react-class js/antd.InputNumber))

;; -

(defn input
  "https://ant.design/components/input/#Input"
  [{:keys [prefix suffix addon-before addon-after] :as props}]
  [(adapt-react-class js/antd.Input)
   (merge props
          (when prefix
            {:prefix (cond-> prefix
                       (not (string? prefix)) as-element)})
          (when suffix
            {:suffix (cond-> suffix
                       (not (string? suffix)) as-element)})
          (when addon-before
            {:addonBefore (cond-> addon-before
                            (not (string? addon-before)) as-element)})
          (when addon-after
            {:addonAfter (cond-> addon-after
                           (not (string? addon-after)) as-element)}))])

(def input-text-area
  "https://ant.design/components/input/#Input.TextArea"
  (adapt-react-class js/antd.Input.TextArea))

(def input-search
  "https://ant.design/components/input/#Input.Search"
  (adapt-react-class js/antd.Input.Search))

(def input-group
  "https://ant.design/components/input/#Input.Group"
  (adapt-react-class js/antd.Input.Group))

;; -

(def mention
  "https://ant.design/components/mention/#Mention"
  (adapt-react-class js/antd.Mention))

;; -

(defn rate
  "https://ant.design/components/rate/"
  [{:keys [character] :as props}]
  [(adapt-react-class js/antd.Rate)
   (merge props
          (when character
            {:character (as-element character)}))])

;; -

(def radio
  "https://ant.design/components/radio/#Radio"
  (adapt-react-class js/antd.Radio))

(def radio-group
  "https://ant.design/components/radio/#RadioGroup"
  (adapt-react-class js/antd.Radio.Group))

;; -

(defn select
  "https://ant.design/components/select/"
  [{:keys [placeholder] :as props} & children]
  (into
   [(adapt-react-class js/antd.Select)
    (merge props
           (when placeholder
             {:placeholder (cond-> placeholder
                             (not (string? placeholder)) as-element)}))]
   children))

(def select-option
  "https://ant.design/components/select/#Option-props"
  (adapt-react-class js/antd.Select.Option))

(def select-option-group
  "https://ant.design/components/select/#OptGroup-props"
  (adapt-react-class js/antd.Select.OptGroup))

;; -

(def slider
  "https://ant.design/components/slider/"
  (adapt-react-class js/antd.Slider))

;; -

(defn switch
  "https://ant.design/components/switch/#Switch"
  [{:keys [checked-children un-checked-children] :as props}]
  [(adapt-react-class js/antd.Switch)
   (merge props
          (when checked-children
            {:checkedChildren (cond-> checked-children
                                (not (string? checked-children)) as-element)})
          (when un-checked-children
            {:unCheckedChildren (cond-> un-checked-children
                                  (not (string? un-checked-children)) as-element)}))])

;; TODO: Add TreeSelect

(def time-picker
  "https://ant.design/components/time-picker/"
  (adapt-react-class js/antd.TimePicker))

;; TODO: Add Transfer

;; TODO: Add Upload


;;;
;;; Data Display
;;;

(def avatar (adapt-react-class js/antd.Avatar))

;; -

(def badge (adapt-react-class js/antd.Badge))

;; TODO: Add Calendar

(defn card
  "https://ant.design/components/card/#Card"
  [{:keys [title extra] :as props} & children]
  (into
   [(adapt-react-class js/antd.Card)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)})
           (when extra
             {:extra (cond-> extra
                       (not (string? extra)) as-element)}))]
   children))

(def card-grid
  "https://ant.design/components/card/#Card.Grid"
  (adapt-react-class js/antd.Card.Grid))

;; -

(def carousel
  "https://ant.design/components/carousel/"
  (adapt-react-class js/antd.Carousel))

;; -

(def collapse
  "https://ant.design/components/collapse/#Collapse"
  (adapt-react-class js/antd.Collapse))

(defn collapse-panel
  "https://ant.design/components/collapse/#Collapse.Panel"
  [{:keys [header] :as props} & children]
  (into
   [(adapt-react-class js/antd.Collapse.Panel)
    (merge props
           (when header
             {:header (cond-> header
                        (not (string? header)) as-element)}))]
   children))

;; -

(defn popover
  "https://ant.design/components/popover/"
  [{:keys [title content] :as props} & children]
  (into
   [(adapt-react-class js/antd.Popover)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)})
           (when content
             {:content (cond-> content
                         (not (string? content)) as-element)}))]
   children))

;; -

(defn  tooltip
  "https://ant.design/components/tooltip/"
  [{:keys [title] :as props}  & children]
  (into
   [(adapt-react-class js/antd.Tooltip)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)}))]
   children))

;; -

(def table
  "https://ant.design/components/table/#Table"
  (adapt-react-class js/antd.Table))

(defn table-column
  "https://ant.design/components/table/#Column"
  [{:keys [title] :as props}]
  [(adapt-react-class js/antd.Table.Column)
   (merge props
          (when title
            {:title (cond-> title
                      (not (string? title)) as-element)}))])

(defn table-column-group
  "https://ant.design/components/table/#ColumnGroup"
  [{:keys [title] :as props}]
  [(adapt-react-class js/antd.Table.ColumnGroup)
   (merge props
          (when title
            {:title (cond-> title
                      (not (string? title)) as-element)}))])

;; -

(defn tabs
  "https://ant.design/components/tabs/#Tabs"
  [{:keys [tab-bar-extra-content] :as props} & children]
  (into
   [(adapt-react-class js/antd.Tabs)
    (merge props
           (when tab-bar-extra-content
             {:tabBarExtraContent (cond-> tab-bar-extra-content
                                    (not (string? tab-bar-extra-content)) as-element)}))]
   children))

(defn tab-pane
  "https://ant.design/components/tabs/#Tabs.TabPane"
  [{:keys [tab] :as props} & children]
  (into
   [(adapt-react-class js/antd.Tabs.TabPane)
    (merge props
           (when tab
             {:tab (cond-> tab
                     (not (string? tab)) as-element)}))]
   children))

;; -

(def tag
  "https://ant.design/components/tag/#Tag"
  (adapt-react-class js/antd.Tag))

(def checkable-tag
  "https://ant.design/components/tag/#Tag.CheckableTag"
  (adapt-react-class js/antd.Tag.CheckableTag))

;; -

(defn timeline
  "https://ant.design/components/timeline/#Timeline"
  [{:keys [pending] :as props} & children]
  (into
   [(adapt-react-class js/antd.Timeline)
    (merge props
           (when pending
             {:pending (cond-> pending
                         (or (not (string? pending))
                             (not (boolean? pending))) as-element)}))]
   children))

(defn timeline-item
  "https://ant.design/components/timeline/#Timeline.Item"
  [{:keys [dot] :as props} & children]
  (into
   [(adapt-react-class js/antd.Timeline.Item)
    (merge props (when dot {:dot (cond-> dot (not (string? dot)) as-element)}))]
   children))

;; -

(def tree
  "https://ant.design/components/tree/#Tree-props"
  (adapt-react-class js/antd.Tree))

(defn tree-node
  "https://ant.design/components/tree/#TreeNode-props"
  [{:keys [title] :as props} & children]
  (into
   [(adapt-react-class js/antd.Tree.TreeNode)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)}))]
   children))

;;;
;;; Feedback
;;;

(defn alert
  "https://ant.design/components/alert/"
  [{:keys [close-text message description] :as props}]
  [(adapt-react-class js/antd.Alert)
   (merge props
          (when close-text
            {:closeText (cond-> close-text
                          (not (string? close-text)) as-element)})
          (when message
            {:message (cond-> message
                        (not (string? message)) as-element)})
          (when description
            {:description (cond-> description
                            (not (string? description)) as-element)}))])

;; -

(defn modal
  "https://ant.design/components/modal/"
  [{:keys [title footer] :as props} & children]
  (into
   [(adapt-react-class js/antd.Modal)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)})
           (when footer
             {:footer (cond-> footer
                        (not (string? footer)) as-element)}))]
   children))

;; -

(defn message-success
  "https://ant.design/components/message/"
  [content duration on-close]
  (.success js/antd.message
            (cond-> content
              (not (string? content)) as-element)
            duration on-close))

(defn message-error
  "https://ant.design/components/message/"
  [content duration on-close]
  (.error js/antd.message
          (cond-> content
            (not (string? content)) as-element)
          duration on-close))

(defn message-info
  "https://ant.design/components/message/"
  [content duration on-close]
  (.info js/antd.message
         (cond-> content
           (not (string? content)) as-element)
         duration on-close))

(defn message-warning
  "https://ant.design/components/message/"
  [content duration on-close]
  (.warning js/antd.message
            (cond-> content
              (not (string? content)) as-element)
            duration on-close))

(defn message-loading
  "https://ant.design/components/message/"
  [content duration on-close]
  (.loading js/antd.message
            (cond-> content
              (not (string? content)) as-element)
            duration on-close))

;; -

(defn notification-success
  "https://ant.design/components/notification/"
  [config]
  (.success js/antd.notification (clj->js config)))

(defn notification-error
  "https://ant.design/components/notification/"
  [config]
  (.error js/antd.notification (clj->js config)))

(defn notification-info
  "https://ant.design/components/notification/"
  [config]
  (.info js/antd.notification (clj->js config)))

(defn notification-warning
  "https://ant.design/components/notification/"
  [config]
  (.warning js/antd.notification (clj->js config)))

;; TODO: Support notification-close notification-destroy

;; -

(def progress
  "https://ant.design/components/progress/"
  (adapt-react-class js/antd.Progress))

;; -

(defn pop-confirm
  "https://ant.design/components/popconfirm/"
  [{:keys [title] :as props} & children]
  (into
   [(adapt-react-class js/antd.Popconfirm)
    (merge props
           (when title
             {:title (cond-> title
                       (not (string? title)) as-element)}))]
   children))

;; -

(def spin
  "https://ant.design/components/spin/"
  (adapt-react-class js/antd.Spin))


;;;
;;; Other
;;;

(def anchor
  "https://ant.design/components/anchor/"
  (adapt-react-class js/antd.Anchor))

;; -

(def back-top
  "https://ant.design/components/back-top/"
  (adapt-react-class js/antd.BackTop))

;; -

(def locales
  "https://ant.design/components/locale-provider/"
  (.-locales js/window.antd))

(def locale-provider
  "https://ant.design/components/locale-provider/"
  (adapt-react-class (.-LocaleProvider js/window.antd)))