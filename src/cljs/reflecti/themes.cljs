;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.themes)

(def default-font "Roboto, sans-serif")

(def dark-theme
  {:palette {:text-color "#FFF"
             :alternate-text-color "#303030"
             :disabled-color "#D3D3D3"
             :canvas-color "#303030"
             :chart-colors ["#880E4F" "#AD1457" "#C2185B" "#D81B60" "#E91E63"
                            "#EC407A" "#F06292" "#F48FB1" "#F8BBD0" "#FCE4EC"]
             :primary1Color "#0097A7"
             :accent1Color "#D70075"
             :pickerHeaderColor "#0097A7"
             :disabledColor "#D3D3D3"
             :font-family default-font}})

(def light-theme
  {:palette {:text-color "#212121"
             :alternate-text-color "#FFF"
             :disabled-color "#D3D3D3"
             :canvas-color "#FFF"
             :chart-colors ["#006064" "#00838F" "#0097A7" "#00ACC1" "#00BCD4"
                            "#26C6DA" "#4DD0E1" "#80DEEA" "#B2EBF2" "#E0F7FA"]
             :primary1Color "#0097A7"
             :accent1Color "#D70075"
             :pickerHeaderColor "#0097A7"
             :disabledColor "#D3D3D3"
             :font-family default-font}})

(def default-chart-options
  (merge {:style {:width 750
                  :height 420
                  :margin {:top 20 :left 20 :right 20 :bottom 20}
                  :box-shadow (str "0 2px 2px 0 rgba(0, 0, 0, .14), "
                                   "0 3px 1px -2px rgba(0, 0, 0, .2), "
                                   "0 1px 5px 0 rgba(0, 0, 0, .12)")}}
         {:x-axis {:label "x-axis"}}
         {:y-axis {:label "y-axis"}}
         dark-theme))

(def default-table-options
  {:table {:background-color "#303030"
           :color "#EFEFEF"
           :font-family default-font
           :font-size "13px"
           :border-collapse "collapse"
           :table-layout "fixed"
           :box-shadow (str "0 2px 2px 0 rgba(0, 0, 0, .14), "
                            "0 3px 1px -2px rgba(0, 0, 0, .2), "
                            "0 1px 5px 0 rgba(0, 0, 0, .12)")}
   :headers {:border-bottom-color "#6A6A6A"
             :border-bottom-width "thin"
             :border-bottom-style "solid"
             :padding "10px"
             :font-size "12px"
             :text-align "left"
             :color "#9D9D9D"}
   :cells {:border-bottom-color "#6A6A6A"
           :border-bottom-width "thin"
           :border-bottom-style "solid"
           :hover-color "#3E3E3E"
           :overflow "hidden"
           :padding "10px"}})

(def default-drawer-theme
  (let [icon-width 48
        search-bar-margin 8
        search-bar-height 48
        drop-down-item-style {:padding-left 16
                              :padding-top 0
                              :padding-bottom 0}]
    (merge light-theme
           {:header-style {:height (+ search-bar-height (* search-bar-margin 2))
                           :width "100%"}
            :width (+ 400 (* search-bar-margin 2))
            :style {:overflow-y "scroll"
                    :overflow-x "hidden"
                    :transition "none"}
            :overlay-style {:backgroundColor ""}})))

(def default-slider-theme
  (merge light-theme
         {:style {:width 400
                  :height 40
                  :zIndex 5
                  :margin "10px"
                  :padding "12px"
                  :position "absolute"
                  :top "60px"
                  :left "27%"}
          :slider-style {:margin-top "-24px"
                         :padding-left "10px"
                         :padding-right "10px"}}))

(def default-date-time-theme
  (let [teal "#27B99C"
        icon-width 48
        drop-down-item-style {:padding-left 16
                              :padding-top 0
                              :padding-bottom 0}]
    (merge light-theme
           {:date-picker {:select-color teal}
            :time-picker {:style {:height icon-width
                                  :zIndex 5
                                  :margin "10px"
                                  :padding "10px 15px 0 0"
                                  :display "inline-block"}
                          :icon-bar-style {:icon-style {:width "20px"
                                                        :height "20px"
                                                        :margin-top "9px"}
                                           :separator-style {:height 30
                                                             :top 5
                                                             :margin-left 0}}
                          :dialog-style {:text-field-style {:width 85
                                                            :height 40}
                                         :width "41%"
                                         :presets-style {:width "50%"
                                                         :height 200
                                                         :drop-down-item-style drop-down-item-style}
                                         :range-picker-style {:float "left"
                                                              :width "50%"}}}})))

(defn tooltip-styles
  [chart-options]
  (str ".nvtooltip {background:" (-> chart-options :palette :canvas-color)
       ";border:none; border-radius:2px; color:" (-> chart-options :palette :text-color)
       ";box-shadow:0 2px 2px 0 rgba(0, 0, 0, .14),
                    0 3px 1px -2px rgba(0, 0, 0, .2),
                    0 1px 5px 0 rgba(0, 0, 0, .12);}"
       ".nvtooltip table td.legend-color-guide div {border:none;}"))

(defn- area-styles
  [element-id chart-options]
  (str
   (str "#" element-id " .nvd3 text{fill:" (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-x.nv-axis > g > g > g > line, "
        "g.nv-y.nv-axis > g.nvd3.nv-wrap.nv-axis > g > g.tick > line{opacity:0;}")
   (str "#" element-id " .nvd3 .nv-axis.nv-x path.domain {stroke-opacity: 1;}")
   (str "#" element-id " g.nv-y.nv-axis > g > g > path, "
        "g.nv-x.nv-axis > g.nv-zeroLine > line, "
        "g.nv-y.nv-axis > g.nv-zeroLine > line{stroke:"
        (-> chart-options :palette :text-color) ";}")
   (tooltip-styles chart-options)))

(defn- bullet-styles
  [element-id chart-options]
  (str
   (str "#" element-id " .nvd3 text{fill:" (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-titles > g > text.nv-subtitle{font-size:11px;}")
   (str "#" element-id " .nvd3.nv-bullet .nv-markerTriangle{stroke-width:1.0px;}")
   (str "#" element-id " .nvd3.nv-bullet .nv-measure{fill-opacity:1.0;}")
   (tooltip-styles chart-options)))

(defn- h-bar-styles
  [element-id chart-options]
  (str
   (str "#" element-id " .nvd3 text{fill:" (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-barsWrap > g > g > g > g > g > rect{fill-opacity:1.0;}")
   (str "#" element-id " g.nv-x.nv-axis > g > g > g > line, "
        "g.nv-y.nv-axis > g.nvd3.nv-wrap.nv-axis > g > g.tick > line{opacity:0;}")
   (str "#" element-id " g.nv-y.nv-axis > g > g > path, "
        "g.nv-x.nv-axis > g.nv-zeroLine > line, "
        "g.nv-y.nv-axis > g.nv-zeroLine > line{stroke:"
        (-> chart-options :palette :text-color) ";}")
   (tooltip-styles chart-options)))

(defn- line-styles
  [element-id chart-options]
  (str
   (str "#" element-id " .nvd3 text {fill:" (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-x.nv-axis > g > g > path{stroke-opacity:1.0;}")
   (str "#" element-id " g.nv-x.nv-axis > g.nv-zeroLine > line, "
        " g.nv-y.nv-axis.nvd3-svg > g > g > g.tick.zero > line"
        " g.nv-y.nv-axis > g.nv-zeroLine > line{stroke:"
        (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-x.nv-axis > g > g > g > line, g.nv-y.nv-axis > g > g > g > line, "
        " g.nv-y.nv-axis > g.nvd3.nv-wrap.nv-axis > g > g.tick > line{opacity:0;}")
   (str "#" element-id " .nvd3 .nv-groups path.nv-line{stroke-width:3.5;}")
   (str "#" element-id " g.nv-y.nv-axis > g > g > path, "
        " g.nv-x.nv-axis > g > g > path, g.nv-y.nv-axis > g > g > path{stroke:"
        (-> chart-options :palette :text-color) ";}")
   (tooltip-styles chart-options)))

(defn- v-bar-styles
  [element-id chart-options]
  (str
   (str "#" element-id " .nvd3 text{fill:" (-> chart-options :palette :text-color) ";}")
   (str "#" element-id " g.nv-barsWrap > g > g > g > g > g > rect{fill-opacity:1.0;}")
   (str "#" element-id " g.nv-y.nv-axis > g.nvd3.nv-wrap.nv-axis > g > g.tick > line{opacity:0;}")
   (str "#" element-id " g.nv-x.nv-axis > g > g > g > line{opacity:0;}")
   (str "#" element-id " g.nv-y.nv-axis > g > g > path, "
        "g.nv-x.nv-axis > g.nv-zeroLine > line, "
        "g.nv-y.nv-axis > g.nv-zeroLine > line{stroke:"
        (-> chart-options :palette :text-color) ";}")
   (tooltip-styles chart-options)))
