;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always reflecti.example.core
  (:require [reflecti.table-example.core :refer [table-example]]
            [reflecti.charts-example.core :refer [chart-examples theme]]
            [reflecti.search-example.core :refer [search-example]]
            [reflecti.slider-example.core :refer [slider-example]]
            [reflecti.time-picker-example.core :refer [time-picker-example]]
            [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as mui]))

(enable-console-print!)

(defonce example (r/atom :charts))

(defn examples []
  (let [page-style {:width "850px"
                    :margin "10px auto"}
        button-panel-style {:display "flex"
                            :flex-direction "row"
                            :margin "auto"
                            :width "80%"}
        button-style {:margin "12px"
                      :padding-left "5px"
                      :padding-right "5px"}
        button-info [{:type :charts :display-text "Charts"}
                     {:type :table :display-text "Table"}
                     {:type :slider :display-text "Slider"}
                     {:type :search :display-text "Search+Drawer"}
                     {:type :picker :display-text "Date+Time Picker"}]]
    [mui/mui-theme-provider
     {:mui-theme (ui/get-mui-theme)}
     [:div {:style page-style}
      [:div {:style button-panel-style}
       (doall
        (map-indexed
         (fn [idx {:keys [type display-text]}]
           [mui/raised-button
            {:style button-style
             :key idx
             :onTouchTap #(reset! example type)}
            display-text])
         button-info))]
      (condp = @example
        :charts (chart-examples theme)
        :table (table-example)
        :search (search-example)
        :slider (slider-example)
        :picker (time-picker-example))]]))

(r/render-component [examples] (js/document.getElementById "app"))
