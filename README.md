# 7theta/reflecti

> Latin word for `react` or `respond`.

## Usage

Include `reflecti` in your `project.clj` dependencies.

[![Current Version](https://img.shields.io/clojars/v/com.7theta/reflecti.svg)](https://clojars.org/com.7theta/reflecti)
[![Circle CI](https://circleci.com/gh/7theta/reflecti.svg?style=shield)](https://circleci.com/gh/7theta/reflecti)
[![Dependencies Status](https://jarkeeper.com/7theta/reflecti/status.svg)](https://jarkeeper.com/7theta/reflecti)

Add `reflecti.core` to your file's namespace `:require`

```clojure
(:require [reflecti.core :as reflecti])
```

Take a look in `examples/` to see `reflecti` in use.

- - -
### Charts
Each chart uses similar parameters: a string ID for the DOM element that the chart should be drawn to, the data to be drawn, and an options-map. See each chart type below for specifics about the data and options-map.

```clojure
(reflecti/chart-type "dom-element-id" data options-map)
```

#### Area Chart
```clojure
(reflecti/area "area-chart" data options-map)
```
##### Data
A vector of maps. Each map is a series (an area), `:key` is the series name (string or number), and `:values` should be a vector of vectors each representing the x and y values.
```clojure
[{:key "Series 1", :values [[1 0] [2 7] [3 14] [4 16] [5 19] [6 23] [7 28]]} 
 {:key "Series 2", :values [[1 0] [2 7] [3 14] [4 16] [5 19] [6 23] [7 28]]} 
 ...
 {:key "Series N", :values [[1 0] [2 7] [3 14] [4 16] [5 19] [6 23] [7 28]]}]
```

#### Bullet Chart
```clojure
(reflecti/bullet "bullet-chart" data options-map)
```
##### Data
A map specifying the title, subtitle and various data points. 

`:ranges` are the shades behind the bullet, represents some relative measurement (ex: bad/okay/good, min/median/max)

`:measures` are the bullets

`:markers` are triangles on the bullet, represents some milestones (ex: previous-goal/goal)
```clojure
{:title "Numbers" 
 :subtitle "(per number)"
 :ranges [161 239 305]
 :measures [247]
 :markers [221]}
```

#### Horizontal and Vertical Bar Charts
```clojure
(reflecti/horizontal-bar "h-bar-chart" data options-map)
(reflecti/vertical-bar "v-bar-chart" data options-map)
```
##### Data
A vector of maps. Each map is a series (a bar), `:key` is the series name (string or number), and `:values` should be a vector of maps containing a label and value.
```clojure
[{:key "Series 1", 
  :values [{:label "A", :value 25} {:label "B", :value 2} {:label "C", :value 28} ...]} 
{:key "Series 2", 
 :values [{:label "A", :value 26} {:label "B", :value 18} {:label "C", :value 7} ...]}
 ...
{:key "Series N", 
 :values [{:label "A", :value 16} {:label "B", :value 8} {:label "C", :value 27} ...]}]
```

#### Line Chart
```clojure
(reflecti/line "line-chart" data options-map)
```
##### Data
A vector of maps. Each map is a series (a line), `:key` is the series name (string or number), and `:values` should be a vector of maps containing an x and y value for each point in the line to plot.
```clojure
[{:key "Series 1", :values [{:x 1, :y 0} {:x 2, :y 2} {:x 3, :y 17} ...]} 
 {:key "Series 2", :values [{:x 1, :y 0} {:x 2, :y 4} {:x 3, :y 8} ...]}
 ...
 {:key "Series N", :values [{:x 1, :y 0} {:x 2, :y 4} {:x 3, :y 8} ...]}]
```
##### General Chart Options
```clojure
{;; map containing custom styles
 :style {:width 200
         :height 200
         :margin {:top 20 :left 20 :right 20 :bottom 20}
         :box-shadow "give it that paper look, please."}}
 ;; palette options for changing chart colours, text colours, and background colour of the chart
 :palette {:text-color "#FFF"
           :alternate-text-color "#303"
           :canvas-color "#303"
           :chart-colors [big list of colours in here]}
 ;; customize the x and y axis
 :x-axis {:label "Time"
          ;; https://github.com/d3/d3-time-format/blob/master/README.md#d3-time-format
          :time-format "some d3 time format"
          ;; https://github.com/d3/d3-axis/blob/master/README.md#axis_tickFormat
          :format "some d3 tick format"}
 :y-axis {:label "Volume"}
 ;; customize the legend
 :legend {:right-align false
          :margin {:top 20 :left 20 :right 20 :bottom 20}}
 ;; http://nvd3-community.github.io/nvd3/examples/documentation.html#tooltip
 :tooltip #(pretend-im-a-tooltip-content-generator-fn)}
```

- - -
### Table
A table requires a data-source of some kind. You can make it whatever you want it to be... text, numbers, images, reflecti charts! The reflecti table is sortable, which means you need to tell the table how to sort its data-source (how else would it know that you want your cat images sorted by whisker count?). 

#### Usage
Use a `defrecord` along with the `defprotocol`s `DataSource` and `Sortable` to sort this out. There are tools for making one such record found in `reflecti.datasource`.
```clojure
(ns reflecti.datasource-example
  (:require [reflecti.datasource :refer [data dispatch-sort
                                         DataSource Sortable]]
            [reagent.core :as r]))
;; data must be held in a reagent atom
;; https://github.com/reagent-project/reagent
(defrecord SomeDataSource [data-ratom]
  DataSource
  (data [this] data-ratom)

  Sortable
  (dispatch-sort [this sort-key direction]
    (reset! data-ratom (your-magic-sorting-fn))))
    
(defonce data-source (SomeDataSource. (r/atom (cat-pictures-or-whatever)))

(reflecti/table data-source options-map)
```
Your data r/atom should be a vector of maps. Each map represents a row, each keyword in the map represents a column, and the respective value is the data inside the cell at it's row.
```clojure
(r/atom [{:name "Mittens" :age 10 :whisker-count 23.3}
         {:name "Princess" :age 3 :whisker-count 18}
         ...
         {:name "Fluffy McFluffPants" :age 4 :whisker-count 0}}])
```

#### Table Options
```clojure
;; style options for the header, also custom display-labels and order

;; if no display-labels are specified, reflecti will turn the keys in 
;; the data-source into a string by removing the dashes/colon, inserting
;; the relevant spaces, and capitalizing what's left.
{:headers {:display-labels {:one "1"
                            :five "55555"
                            :two "22"
                            :four "4444"
                            :three "333"
                            :six "666666"}
           :order [:one
                   :two
                   :three
                   :four
                   :five
                   :six]
           :border-bottom-color "#DFDFDF"}
 ;; style options for the table
 :table {:background-color "#FFF"
         :color "#212121"}
;; style options for the cells
 :cells {:border-bottom-color "#DFDFDF"
         :hover-color "#EDEDED"}}
```

- - -
###Slider

```clojure
(reflecti/slider custom-opts)
```
`custom-opts` should be a map determining any customizations you want applied to the slider.

#####Slider Custom Opts
```clojure
{;; function that determines what to do when the slider is slid.
 ;; event is what caused the slider to slide (mouse or keyboard)
 ;; value is what the value of the slider is after it's been slid
 :on-slide (fn [event value] (time-travel value))
 ;; style options for the slider
 :theme {:style ;; style for container around slider
         :slider-style ;; style for slider itself
}}
```

- - -
###Date-Time Picker

```clojure
(reflecti/date-time-picker custom-opts)
```
`custom-opts` should be a map determining any customizations you want applied to the date-time picker.

#####Date-Time Picker Custom Opts
```clojure
{;; function determining what to when the From or To time has changed, times are goog.date.DateTime
 ;; cljs-time is recommended https://github.com/andrewmcveigh/cljs-time)
 :on-time-change (fn [from to]
                      (when from
                        (prn "From time: " from))
                      (when to
                        (prn "To time: " to)))
 ;; what format you would like the date-time to be displayed in
 :format "an-awesome-format-of-course"
 ;; style options for the date-time picker
 :theme {:time-picker {:style { ;; style options for floating date-time picker
                              }
                       :icon-bar-style {:icon-style ;; style of the icons
                                        :separator-style ;; style of the separator
                                       }
                       ;; style options for dialog popover
                       :dialog-style {:text-field-style { ;; style for input text fields
                                                        }
                                      :presets-style { ;; style for preset times dropdown
                                                     }
                                      :range-picker-style { ;; style for time and date range pickers
                                                          }}}}}
```

- - -
###Search

A floating search box with suggestions as a drop down.

```clojure
(reflecti/search-bar custom-opts)
```
`custom-opts` should be a map determining any customizations you want applied.

#####Search Custom Opts
```clojure
{

 ;; collection of maps, each containing at a minimum a key
 ;; ':display-text' to be used when displaying the suggestion in
 ;; either the search bar or suggestions dropdown.
 :search-suggestions [{:display-text "Suggestion 1"}]
             
 ;; called with the text present in the search bar every time it
 ;; changes or a search request is made.
 :on-search (fn [search-text] ...)
 
 ;; function to call when search box's clear button is pressed
 :on-clear (fn [] ...)
 
 ;; function to call when a suggestion is clicked.
 ;; 'suggestion' is the exact map that was selected as provided in
 ;; 'search-suggestions'.
 :on-suggestion-click (fn [suggestion] ...)
 
 ;; 'suggestions-pane' is a reagent component that can be provided to
 ;; customize the suggestions dropdown on the search-bar. If no component
 ;; is provided, a default is used.
 :suggestions-pane (fn [{:keys [suggestions on-selection]}] [:div suggestions])
 
 ;; style options
 :theme themes/light-theme
 
 }
```

- - -
###Search with Side Drawer

A floating search box with suggestions as a drop down that also comes with a slide out drawer.

```clojure
(reflecti/search-drawer custom-opts)
```
`custom-opts` should be a map determining any customizations you want applied.

#####Search Custom Opts
```clojure
{:drawer-contents ;; contents of the drawer
 :open-drawer? ;; boolean controlling whether the drawer is open/closed
 ;; function to call when the drawer is requested to be opened/closed
 ;;   opened? is a boolean representing whether the drawer was requested to be opened or closed
 ;;   reason is why the drawer was requested to be opened/closed (click, keyboard, etc.)
 :on-drawer-open (fn [opened? reason] ...)
 :search-suggestions ;; array of maps containing all search suggestions, 
                     ;; key :display-text will be displayed in the drop down
 ;; function to call when the search button is pressed,
 ;; also called whenever a new character is typed into the search input
 ;;   search-text is the current text entered in the search input
 :on-search (fn [search-text] ...)
 ;; function to call when search box's clear button is pressed
 :on-clear (fn [] ...)
 ;; function to call when a suggestion is clicked.
 ;; 'suggestion' is the exact map that was selected as provided in
 ;; 'search-suggestions'.
 :on-suggestion-click (fn [suggestion] ...)
 :drawer-theme ;; style options for the drawer
 :search-theme ;; style options for the search box
 }
```
- - -
## Copyright and License

Copyright © 2016 7theta
