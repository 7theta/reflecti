;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.charts
  (:require [reflecti.themes :as themes :refer [default-chart-options]]
            [utilis.map :refer [deep-merge]]
            [schema.core :as schema :include-macros true]
            [reagent.core :as r]
            [goog.style]
            [cljsjs.d3]
            [cljsjs.nvd3]))

;;; Forward Declarations

(declare chart-component area-chart-model bullet-chart-model
         horizontal-bar-chart-model line-chart-model
         vertical-bar-chart-model d3-tick-format)

(def str-or-num (schema/cond-pre schema/Num schema/Str))

;;; Public

(schema/defn area
  "Area charts depict a relationship between two axes while giving the impression
  of volume. Data points are connected by a line and the area below it is filled in.

  Draws an area chart in the DOM element provided in 'element-id'. Any data passed
  in through 'chart-data' will update the chart."
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [[str-or-num]]}]]
   (area element-id chart-data nil))
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [[str-or-num]]}]
    chart-options :- {schema/Keyword schema/Any}]
   [chart-component {:chart-fn area-chart-model
                     :element-id element-id
                     :data chart-data
                     :options (deep-merge default-chart-options chart-options)}]))

(schema/defn bullet
  "A bullet chart uses a rectangular row to represent measures comparing
  a primary measure to other measures providing a qualitative meaning.

  Draws a bullet chart in the DOM element provided in 'element-id'. Any
  data passed in through 'chart-data' will update the chart."
  ([element-id :- schema/Str
    chart-data :- {:title schema/Str
                   (schema/optional-key :subtitle) schema/Str
                   (schema/optional-key :ranges) [schema/Num]
                   :measures [schema/Num]
                   (schema/optional-key :markers) [schema/Num]}]
   (bullet element-id chart-data nil))
  ([element-id :- schema/Str
    chart-data :- {:title schema/Str
                   (schema/optional-key :subtitle) schema/Str
                   (schema/optional-key :ranges) [schema/Num]
                   :measures [schema/Num]
                   (schema/optional-key :markers) [schema/Num]}
    chart-options :- {schema/Keyword schema/Any}]
   [chart-component {:chart-fn bullet-chart-model
                     :element-id element-id
                     :data chart-data
                     :options (deep-merge default-chart-options chart-options)}]))

(schema/defn horizontal-bar
  "A horizontal bar chart uses rectangular rows to represent the proportional
  length of data.

  Draws a horizontal bar chart in the DOM element provided in 'element-id'. Any
  data passed in through 'chart-data' will update the chart."
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:label str-or-num :value str-or-num}]}]]
   (horizontal-bar element-id chart-data nil))
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:label str-or-num :value str-or-num}]}]
    chart-options :- {schema/Keyword schema/Any}]
   [chart-component {:chart-fn horizontal-bar-chart-model
                     :element-id element-id
                     :data chart-data
                     :options (deep-merge default-chart-options chart-options)}]))

(schema/defn line
  "Line charts depict a relationship between two axes. Data points are connected
  by a line and the area below it is filled in.

  Draws a line chart in the DOM element provided in 'element-id'. Any data passed
  in through 'chart-data' will update the chart."
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:x schema/Num :y schema/Num}]}]]
   (line element-id chart-data nil))
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:x schema/Num :y schema/Num}]}]
    chart-options :- {schema/Keyword schema/Any}]
   [chart-component {:chart-fn line-chart-model
                     :element-id element-id
                     :data chart-data
                     :options (deep-merge default-chart-options chart-options)}]))

(schema/defn vertical-bar
  "A vertical bar chart uses rectangular columns to represent the proportional
  length of data.

  Draws a vertical bar chart in the DOM element provided in 'element-id'. Any
  data passed in through 'chart-data' will update the chart."
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:label str-or-num :value str-or-num}]}]]
   (vertical-bar element-id chart-data nil))
  ([element-id :- schema/Str
    chart-data :- [{:key str-or-num :values [{:label str-or-num :value str-or-num}]}]
    chart-options :- {schema/Keyword schema/Any}]
   [chart-component {:chart-fn vertical-bar-chart-model
                     :element-id element-id
                     :data chart-data
                     :options (deep-merge default-chart-options chart-options)}]))

;;; Implementation

(defn- chart-div
  [{:keys [element-id options]}]
  [:div {:id element-id
         :style {:width (-> options :style :width)
                 :height (-> options :style :height)
                 :background-color (-> options :palette :canvas-color)
                 :box-shadow (-> options :style :box-shadow)}}
   [:svg]])

(defn- render-chart
  [this]
  (let [{:keys [chart-fn element-id data type options]} (r/props this)]
    (if (ffirst (js->clj (.. js/d3 (select (str "#" element-id " svg g")))))
      (.. js/d3 (select (str "#" element-id " svg"))
          (call (chart-fn element-id data options)))
      (.addGraph js/nv (chart-fn element-id data options)))))

(defn- chart-component
  [props]
  (r/create-class {:reagent-render (fn [props] [chart-div props])
                   :component-did-mount render-chart
                   :component-did-update render-chart}))

(defn- area-chart-model
  [element-id chart-data chart-options]
  (fn []
    (let [chart (.. js/nv -models stackedAreaChart
                    (margin (clj->js (-> chart-options :style :margin)))
                    (x (fn [d] (first (js->clj d))))
                    (y (fn [d] (second (js->clj d))))
                    (useInteractiveGuideline true)
                    (clipEdge true)
                    (color (clj->js (-> chart-options :palette :chart-colors)))
                    (width (-> chart-options :style :width))
                    (height (-> chart-options :style :height)))]
      (.. chart -xAxis
          (axisLabel (-> chart-options :x-axis :label))
          (tickFormat (d3-tick-format (-> chart-options :x-axis))))
      (.. chart -yAxis
          (axisLabel (-> chart-options :y-axis :label))
          (tickFormat (d3-tick-format (-> chart-options :y-axis))))
      (when-let [{:keys [right-align margin]} (:legend chart-options)]
        (doto (.-legend chart)
          (.rightAlign right-align)
          (.margin (clj->js margin))))
      (when-let [{:keys [content-generator className class-name]}
                 (:tooltip chart-options)]
        (cond-> (.-tooltip (.-interactiveLayer chart))
          content-generator (.contentGenerator content-generator)
          (or className class-name) (.classes (or className class-name))))
      (.. js/d3 (select (str "#" element-id " svg"))
          (datum (clj->js chart-data))
          (call chart))
      (goog.style/installStyles (themes/area-styles element-id chart-options))
      chart)))

(defn- bullet-chart-model
  [element-id chart-data chart-options]
  (fn []
    (let [chart (.. js/nv -models bulletChart
                    (margin (clj->js (-> chart-options :style :margin)))
                    (color (clj->js (-> chart-options :palette :chart-colors)))
                    (width (-> chart-options :style :width))
                    (height (-> chart-options :style :height)))]
      (when-let [{:keys [content-generator className class-name]} (:tooltip chart-options)]
        (cond-> (.-tooltip (.-interactiveLayer chart))
          content-generator (.contentGenerator content-generator)
          (or className class-name) (.classes (or className class-name))))
      (.. js/d3 (select (str "#" element-id " svg"))
          (datum (clj->js chart-data))
          (call chart))
      (goog.style/installStyles (themes/bullet-styles element-id chart-options))
      chart)))

(defn- horizontal-bar-chart-model
  [element-id chart-data chart-options]
  (fn [] (let [chart (.. js/nv -models multiBarHorizontalChart
                        (margin (clj->js (-> chart-options :style :margin)))
                        (x (fn [d] (.. d -label)))
                        (y (fn [d] (.. d -value)))
                        (showValues false)
                        (showControls false)
                        (color (clj->js (-> chart-options :palette :chart-colors)))
                        (width (-> chart-options :style :width))
                        (height (-> chart-options :style :height)))]
          (.. chart -xAxis
              (axisLabel (-> chart-options :x-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :x-axis))))
          (.. chart -yAxis
              (axisLabel (-> chart-options :y-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :y-axis))))
          (when-let [{:keys [right-align margin]} (:legend chart-options)]
            (doto (.-legend chart)
              (.rightAlign right-align)
              (.margin (clj->js margin))))
          (when-let [{:keys [content-generator className class-name]}
                     (:tooltip chart-options)]
            (cond-> (.-tooltip (.-interactiveLayer chart))
              content-generator (.contentGenerator content-generator)
              (or className class-name) (.classes (or className class-name))))
          (.. js/d3 (select (str "#" element-id " svg"))
              (datum (clj->js chart-data))
              (call chart))
          (goog.style/installStyles (themes/h-bar-styles element-id chart-options))
          chart)))

(defn- line-chart-model
  [element-id chart-data chart-options]
  (fn [] (let [chart (.. js/nv -models lineChart
                        (margin (clj->js (-> chart-options :style :margin)))
                        (useInteractiveGuideline true)
                        (showLegend true)
                        (showYAxis true)
                        (showXAxis true)
                        (color (clj->js (-> chart-options :palette :chart-colors)))
                        (width (-> chart-options :style :width))
                        (height (-> chart-options :style :height)))]
          (.. chart -xAxis
              (axisLabel (-> chart-options :x-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :x-axis))))
          (.. chart -yAxis
              (axisLabel (-> chart-options :y-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :y-axis))))
          (when-let [{:keys [right-align margin]} (:legend chart-options)]
            (doto (.-legend chart)
              (.rightAlign right-align)
              (.margin (clj->js margin))))
          (when-let [{:keys [content-generator className class-name]}
                     (:tooltip chart-options)]
            (cond-> (.-tooltip (.-interactiveLayer chart))
              content-generator (.contentGenerator content-generator)
              (or className class-name) (.classes (or className class-name))))
          (.. js/d3 (select (str "#" element-id " svg"))
              (datum (clj->js chart-data))
              (call chart))
          (goog.style/installStyles (themes/line-styles element-id chart-options))
          chart)))

(defn- vertical-bar-chart-model
  [element-id chart-data chart-options]
  (fn [] (let [chart (.. js/nv -models discreteBarChart
                        (margin (clj->js (-> chart-options :style :margin)))
                        (x (fn [d] (.. d -label)))
                        (y (fn [d] (.. d -value)))
                        (staggerLabels true)
                        (showValues false)
                        (color (clj->js (-> chart-options :palette :chart-colors)))
                        (width (-> chart-options :style :width))
                        (height (-> chart-options :style :height)))]
          (.. chart -xAxis
              (axisLabel (-> chart-options :x-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :x-axis))))
          (.. chart -yAxis
              (axisLabel (-> chart-options :y-axis :label))
              (tickFormat (d3-tick-format (-> chart-options :y-axis))))
          (when-let [{:keys [right-align margin]} (:legend chart-options)]
            (doto (.-legend chart)
              (.rightAlign right-align)
              (.margin (clj->js margin))))
          (when-let [{:keys [content-generator className class-name]}
                     (:tooltip chart-options)]
            (cond-> (.-tooltip (.-interactiveLayer chart))
              content-generator (.contentGenerator content-generator)
              (or className class-name) (.classes (or className class-name))))
          (.. js/d3 (select (str "#" element-id " svg"))
              (datum (clj->js chart-data))
              (call chart))
          (goog.style/installStyles (themes/v-bar-styles element-id chart-options))
          chart)))

(defn- d3-tick-format
  [axis-options]
  (or (when-let [time-format (:time-format axis-options)]
        (fn [d] ((.format (.-time js/d3) time-format) (js/Date. d))))
      (if-let [format (:format axis-options)]
        (.format js/d3 format)
        #(identity %))))
