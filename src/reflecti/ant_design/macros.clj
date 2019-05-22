(ns reflecti.ant-design.macros
  (:require [inflections.core :refer [hyphenate]]
            [clojure.string :as st]))

(def components
  '[Affix
    Alert
    Anchor
    Anchor.Link
    AutoComplete
    AutoComplete.OptGroup
    AutoComplete.Option
    Avatar
    BackTop
    Badge
    Breadcrumb
    Breadcrumb.Item
    Button
    Button.Group
    Calendar
    Card
    Card.Grid
    Card.Meta
    Carousel
    Cascader
    Checkbox
    Checkbox.Group
    Col
    Collapse
    Collapse.Panel
    Comment
    ConfigProvider
    DatePicker
    DatePicker.MonthPicker
    DatePicker.RangePicker
    DatePicker.WeekPicker
    Divider
    Drawer
    Dropdown
    Dropdown.Button
    Empty
    Form
    Form.Item
    Icon
    Input
    Input.Group
    Input.Search
    Input.TextArea
    InputNumber
    Layout
    Layout.Content
    Layout.Footer
    Layout.Header
    Layout.Sider
    List
    List.Item
    List.Item.Meta
    LocaleProvider
    Mention
    Mention.Nav
    Menu
    Menu.Divider
    Menu.Item
    Menu.ItemGroup
    Menu.SubMenu
    #_Modal
    PageHeader
    Pagination
    Popconfirm
    Popover
    Progress
    Radio
    Radio.Button
    Radio.Group
    Rate
    Row
    Select
    Select.OptGroup
    Select.Option
    Skeleton
    Slider
    Spin
    Statistic
    Steps
    Steps.Step
    Switch
    Table
    Table.Column
    Table.ColumnGroup
    Tabs
    Tabs.TabPane
    Tag
    Tag.CheckableTag
    TimePicker
    Timeline
    Timeline.Item
    Tooltip
    Transfer
    Tree
    Tree.DirectoryTree
    Tree.TreeNode
    TreeSelect
    TreeSelect.TreeNode
    Typography
    Typography.Text
    Typography.Title
    Typography.Paragraph
    Upload
    Upload.Dragger])

(defn normalize-name
  [component]
  (-> component name hyphenate
      (st/replace #"\." "-")))

(def antd 'antd)

(defn define-component [antd-component]
  (let [component (name antd-component)]
    `(def ~((comp symbol normalize-name) component)
       (r/adapt-react-class
        (apply goog.object/getValueByKeys
               ~antd
               ~(st/split component #"\."))))))

(defmacro export-components []
  `(do ~@(map define-component components)))
