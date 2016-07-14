;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always reflecti.charts-example.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reflecti.core :as charts]
            [reflecti.themes :as themes]
            [reflecti.charts-example.db :as db]
            [utilis.map :refer [deep-merge]]
            [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as mui]
            [cljs-react-material-ui.core :as ui]))

(defonce app-state db/default-db)
(defonce theme (r/atom :dark))
(defonce data-ratoms {:area (reaction (:area-data @app-state))
                      :bullet (reaction (:bullet-data @app-state))
                      :line (reaction (:line-data @app-state))
                      :h-bar (reaction (:h-bar-data @app-state))
                      :v-bar (reaction (:v-bar-data @app-state))})

(defn chart-examples
  [curr-theme]
  (let [theme-opts (if (= @curr-theme :dark)
                     themes/dark-theme
                     themes/light-theme)
        page-style {:width "800px"
                    :margin-left "auto"
                    :margin-right "auto"
                    :margin-top "10px"}
        button-style {:margin-bottom "10px"
                      :padding-left "5px"
                      :padding-right "5px"}
        theme-switcher-style {:text-align "center"
                              :display "flex"
                              :flex-direction "row"
                              :margin "auto"
                              :width "100px"}
        bullet-opts {:style {:margin {:top 15
                                      :left 80
                                      :right 30
                                      :bottom 30}
                             :height 90}
                     :palette {:chart-colors ["#0097A7"]}}
        h-bar-opts {:style {:margin {:top 30
                                     :right 40
                                     :bottom 50
                                     :left 75}}
                    :palette {:chart-colors ["#0097A7" "#FF4081"]}}
        line-opts {:style {:margin {:top 30
                                    :right 40
                                    :bottom 50
                                    :left 75}}
                   :palette {:chart-colors ["#0097A7" "#FF4081"]}
                   :legend {:right-align false
                            :margin {:left 20
                                     :top 20
                                     :bottom 20
                                     :right 10}}}
        v-bar-opts {:style {:margin {:top 30
                                     :right 40
                                     :bottom 70
                                     :left 75}}
                    :palette {:chart-colors ["#0097A7"]}}
        area-opts {:style {:margin {:top 40
                                    :right 40
                                    :bottom 50
                                    :left 70}}
                   :x-axis {:label "x-axis"}
                   :y-axis {:label "y-axis"}}]
    [:div {:style page-style}
     [:div {:style theme-switcher-style}
      [mui/toggle
       {:style button-style
        :label (if (= @curr-theme :dark) "Dark" "Light")
        :onToggle (fn [] (if (= @curr-theme :dark)
                          (reset! theme :light)
                          (reset! theme :dark)))}]]
     [:h3 "Bullet Chart"]
     [mui/raised-button
      {:style button-style
       :onTouchTap (fn [] (db/update-data :bullet))} "Update Data"]
     (charts/bullet "bullet-chart"
                    @(:bullet data-ratoms)
                    (deep-merge theme-opts bullet-opts))
     [:h3 "Horizontal Bar Chart"]
     [mui/raised-button
      {:style button-style
       :onTouchTap (fn [] (db/update-data :h-bar))} "Update Data"]
     (charts/horizontal-bar "h-bar-chart"
                            @(:h-bar data-ratoms)
                            (deep-merge theme-opts h-bar-opts))
     [:h3 "Line Chart"]
     [mui/raised-button
      {:style button-style
       :onTouchTap (fn [] (db/update-data :line))} "Update Data"]
     (charts/line "line-chart"
                  @(:line data-ratoms)
                  (deep-merge theme-opts line-opts))
     [:h3 "Vertical Bar Chart"]
     [mui/raised-button
      {:style button-style
       :onTouchTap (fn [] (db/update-data :v-bar))} "Update Data"]
     (charts/vertical-bar "v-bar-chart"
                          @(:v-bar data-ratoms)
                          (deep-merge theme-opts v-bar-opts))
     [:h3 "Area Chart"]
     [mui/raised-button
      {:style button-style
       :onTouchTap (fn [] (db/update-data :area))} "Update Data"]
     (charts/area "area-chart"
                  @(:area data-ratoms)
                  (deep-merge theme-opts area-opts))]))
