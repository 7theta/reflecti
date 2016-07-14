;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.date-time-picker
  (:require [clojure.string :as str]
            [reflecti.themes :as themes]
            [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as mui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :refer [device-access-time
                                                  content-clear
                                                  action-date-range]]
            [cljs-time.core :as time]
            [cljs-time.coerce :as coerce]
            [cljs-time.format :as fmt]))

;;; Forward Declarations

(declare time-picker-component)

;; Preset date times
(def ^:private date-times
  [{:display-text "Today" :time (fn [] {:from (time/today-at-midnight)
                                       :to (time/minus (time/plus (time/today-at-midnight) (time/days 1)) (time/millis 1))})}

   {:display-text "This week" :time (fn [] {:from (time/date-time (-> (time/day-of-week (time/now)) (time/days) (time/ago)))
                                           :to (-> (time/day-of-week (time/now)) (time/days) (time/from-now))})}
   {:display-text "This month" :time (fn [] {:from (time/first-day-of-the-month (time/now))
                                            :to (time/minus (time/plus (time/last-day-of-the-month (time/now))
                                                                       (time/days 1))
                                                            (time/millis 1))})}
   {:display-text "This year" :time (fn [] {:from (time/date-time
                                                  (time/year (time/now))
                                                  01 01)
                                           :to (time/minus (time/date-time
                                                            (time/year (-> 1 (time/years) (time/from-now))))
                                                           (time/millis 1))})}
   {:display-text "Week to date" :time (fn [] {:from (-> 1 (time/weeks) (time/ago))})}
   {:display-text "Month to date" :time (fn [] {:from (-> 1 (time/months) (time/ago))})}
   {:display-text "Year to date" :time (fn [] {:from (-> 1 (time/years) (time/ago))})}
   {:display-text "Yesterday" :time (fn [] {:from (time/yesterday)})}
   {:display-text "Day before yesterday" :time (fn [] {:from (time/date-time
                                                             (time/year (-> 2 (time/days) (time/ago)))
                                                             (time/month (-> 2 (time/days) (time/ago)))
                                                             (time/day (-> 2 (time/days) (time/ago))))
                                                      :to (time/minus (time/date-time
                                                                       (time/year (-> 1 (time/days) (time/ago)))
                                                                       (time/month (-> 1 (time/days) (time/ago)))
                                                                       (time/day (-> 1 (time/days) (time/ago))))
                                                                      (time/millis 1))})}
   {:display-text "This day last week" :time (fn [] {:from (time/date-time
                                                           (time/year (-> 7 (time/days) (time/ago)))
                                                           (time/month (-> 7 (time/days) (time/ago)))
                                                           (time/day (-> 7 (time/days) (time/ago))))
                                                    :to (time/minus (time/date-time
                                                                     (time/year (-> 7 (time/days) (time/ago)))
                                                                     (time/month (-> 7 (time/days) (time/ago)))
                                                                     (time/day (-> 6 (time/days) (time/ago))))
                                                                    (time/millis 1))})}
   {:display-text "Previous week" :time (fn [] {:from (time/minus (-> (time/day-of-week (time/now)) (time/days) (time/ago))
                                                                 (time/days 7))
                                               :to (time/minus (-> (- 6 (time/day-of-week (time/now)))
                                                                   (time/days) (time/from-now))
                                                               (time/days 7))})}
   {:display-text "Previous month" :time (fn [] {:from (time/first-day-of-the-month (-> 1 (time/months) (time/ago)))
                                                :to (time/minus (time/plus (time/last-day-of-the-month (-> 1 (time/months)
                                                                                                           (time/ago)))
                                                                           (time/days 1))
                                                                (time/millis 1))})}
   {:display-text "Previous year" :time (fn [] {:from (time/date-time
                                                      (time/year (-> 1 (time/years) (time/ago)))
                                                      01 01)
                                               :to (time/minus (time/date-time (time/year (time/now))) (time/millis 1))})}
   {:display-text "Last 15 minutes" :time (fn [] {:from (-> 15 (time/minutes) (time/ago))})}
   {:display-text "Last 30 minutes" :time (fn [] {:from (-> 30 (time/minutes) (time/ago))})}
   {:display-text "Last 1 hour" :time (fn [] {:from (-> 1 (time/hours) (time/ago))})}
   {:display-text "Last 4 hours" :time (fn [] {:from (-> 4 (time/hours) (time/ago))})}
   {:display-text "Last 12 hours" :time (fn [] {:from (-> 12 (time/hours) (time/ago))})}
   {:display-text "Last 24 hours" :time (fn [] {:from (-> 24 (time/hours) (time/ago))})}
   {:display-text "Last 7 days" :time (fn [] {:from (-> 7 (time/days) (time/ago))})}
   {:display-text "Last 30 days" :time (fn [] {:from (-> 30 (time/days) (time/ago))})}
   {:display-text "Last 60 days" :time (fn [] {:from (-> 60 (time/days) (time/ago))})}
   {:display-text "Last 90 days" :time (fn [] {:from (-> 90 (time/days) (time/ago))})}
   {:display-text "Last 6 months" :time (fn [] {:from (-> 6 (time/months) (time/ago))})}
   {:display-text "Last year" :time (fn [] {:from (-> 1 (time/years) (time/ago))})}
   {:display-text "Last 2 years" :time (fn [] {:from (-> 2 (time/years) (time/ago))})}
   {:display-text "Last 5 years" :time (fn [] {:from (-> 5 (time/years) (time/ago))})}
   {:display-text "Epoch" :time (fn [] {:from (time/epoch)})}])

;;; Public

(defn date-time-picker
  ([] [time-picker-component nil])
  ([custom-opts] [time-picker-component custom-opts]))

;;; Implementation

(defn- date-range-picker
  "A date picker component."
  [to|from? date-range text-field-style]
  (let [picked-date (r/atom nil)]
    (fn [to|from? date-range text-field-style]
      [mui/date-picker {:id (str "date-range-" (name to|from?))
                        :autoOk true
                        :firstDayOfWeek 0
                        :mode "landscape"
                        :hintText (str/capitalize (name to|from?))
                        :textFieldStyle text-field-style
                        :style {:float "left"
                                :padding-right "10px"}
                        :value @picked-date
                        :onChange (fn [_ date]
                                    (swap! date-range assoc to|from? (time/date-time (coerce/to-date date)))
                                    (reset! picked-date date))}])))

(defn- time-range-picker
  "A time picker component."
  [to|from? time-range text-field-style]
  (let [picked-time (r/atom nil)]
    (fn [to|from? time-range text-field-style]
      [mui/time-picker {:id (str "time-range-" (name to|from?))
                        :autoOk true
                        :hintText (str/capitalize (name to|from?))
                        :textFieldStyle text-field-style
                        :style {:float "left"
                                :padding-right "10px"}
                        :value @picked-time
                        :onChange (fn [_ time]
                                    (swap! time-range assoc to|from? (time/date-time (coerce/to-date time)))
                                    (reset! picked-time time))}])))

(defn- time-range-dialog
  "A dialog with a dropdown for preset times, date pickers for to and from ranges,
  and time pickers for to and from ranges. "
  [open? time-range date-range dialog-style]
  (let [{:keys [text-field-style
                width
                presets-style
                range-picker-style]} dialog-style
        close-button [mui/flat-button {:label "Close"
                                       :primary true
                                       :rippleColor "none"
                                       :onTouchTap (fn [] (reset! open? false))}]
        preset-value (r/atom nil)
        preset-date (r/atom nil)]
    (fn [open? time-range date-range time-change-fn dialog-style]
      [mui/dialog {:actions #js [(r/as-element close-button)]
                   :title "Set Time Range"
                   :modal true
                   :contentStyle {:maxWidth width
                                  :width width}
                   :open @open?}
       ;; Preset date times dropdown
       [:div {:style {:width (or (:width presets-style) "50%")}}
        [mui/subheader {:style {:padding-left 0}} "Presets"]
        [mui/select-field {:value @preset-value
                           :hintText "Select"
                           :maxHeight (or (:height presets-style) 200)
                           :onChange (fn [_ _ val] (reset! preset-value val))}
         ;; Menu items from preset time ranges
         (doall
          (map-indexed
           (fn [idx preset-dt]
             [mui/menu-item
              {:primaryText (:display-text preset-dt)
               :disableFocusRipple true
               :value idx
               :key idx
               :onTouchTap (fn []
                             (let [from (:from  ((-> preset-dt :time)))
                                   to (or (:to ((-> preset-dt :time))) (time/now))]
                               (reset! preset-date (:display-text preset-dt))
                               (swap! date-range assoc
                                      :from (time/date-time
                                             (time/year from)
                                             (time/month from)
                                             (time/day from))
                                      :to (time/date-time
                                           (time/year to)
                                           (time/month to)
                                           (time/day to)))
                               (swap! time-range assoc
                                      :from (time/date-time
                                             (time/year from)
                                             (time/month from)
                                             (time/day from)
                                             (time/hour from)
                                             (time/minute from))
                                      :to (time/date-time
                                           (time/year to)
                                           (time/month to)
                                           (time/day to)
                                           (time/hour to)
                                           (time/minute to)))
                               (when time-change-fn (time-change-fn from to))))
               :innerDivStyle (:drop-down-item-style presets-style)}])
           date-times))]]
       [mui/divider {:style {:margin-top 10
                             :margin-bottom 10}}]
       ;; Custom date range pickers
       [:div {:style range-picker-style}
        [mui/subheader {:style {:padding-left 0}} "Set Date Range"]
        [date-range-picker :from date-range text-field-style]
        [date-range-picker :to date-range text-field-style]]
       ;; Custom time range pickers
       [:div {:style range-picker-style}
        [mui/subheader {:style {:padding-left 0}} "Set Time Range"]
        [time-range-picker :from time-range text-field-style]
        [time-range-picker :to time-range text-field-style]]])))

(defn- time-pick
  "Floating time range picker with an adjustable time and date range."
  [{:keys [on-time-change format theme]}]
  (let [{:keys [palette time-picker]} (or theme themes/default-date-time-theme)
        {:keys [style icon-bar-style dialog-style]} time-picker
        {:keys [icon-style separator-style]} icon-bar-style
        show-dialog? (r/atom false)
        default-range? (r/atom true)
        time-range (r/atom {:from nil :to nil})
        date-range (r/atom {:from nil :to nil})
        dt-formatter (or format (fmt/formatter "yyyy/MM/dd'-'HH:mm"))]
    (fn []
      ;; Is the default range still showing?
      (reset! default-range? (not (or (and (:from @date-range) (:to @date-range))
                                      (and (:from @time-range) (:to @time-range)))))
      [mui/mui-theme-provider
       {:mui-theme (ui/get-mui-theme theme)}
       ;; Floating time range box
       [mui/paper {:zDepth 3
                   :style style}
        [:div {:style {:display "inline-block"
                       :margin-top "-10px"}}
         ;; 'X' button to clear custom time ranges
         [mui/icon-button {:tooltip "Clear time range"
                           :disableTouchRipple true
                           :iconStyle {:fill (if @default-range?
                                               (:disabledColor palette)
                                               (:primary1Color palette))}
                           :onFocus (fn [e]
                                      (reset! default-range? true)
                                      (swap! date-range assoc :from nil :to nil)
                                      (swap! time-range assoc :from nil :to nil)
                                      (when on-time-change (on-time-change (time/epoch) (time/now))))}
          (content-clear {:style icon-style})]
         [mui/toolbar-separator {:style separator-style}]
         ;; 'clock' button to open custom time range dialog
         [mui/icon-button {:tooltip "Set time range"
                           :disableTouchRipple true
                           :iconStyle {:fill (:primary1Color palette)}
                           :onFocus (fn [_] (reset! show-dialog? true))}
          (device-access-time {:style icon-style})]
         ;; From time
         [:div {:style {:overflow "hidden"
                        :display "inline-block"
                        :margin-bottom "2px"}}
          (if @default-range?
            "Epoch"
            (let [dt (time/date-time
                      (time/year (or (:from @date-range) (time/now)))
                      (time/month (or (:from @date-range) (time/now)))
                      (time/day (or (:from @date-range) (time/now)))
                      (time/hour (or (:from @time-range) (time/now)))
                      (time/minute (or (:from @time-range) (time/now))))]
              (when on-time-change (on-time-change dt nil))
              (fmt/unparse dt-formatter dt)))]
         [:div {:style {:font-size "30px"
                        :display "inline-block"
                        :padding-right "10px"
                        :padding-left "10px"}}
          "-"]
         ;; To time
         [:div {:style {:overflow "hidden"
                        :display "inline-block"
                        :margin-bottom "2px"}}
          (if @default-range?
            "Now"
            (let [dt (time/date-time
                      (time/year (or (:to @date-range) (time/now)))
                      (time/month (or (:to @date-range) (time/now)))
                      (time/day (or (:to @date-range) (time/now)))
                      (time/hour (or (:to @time-range) (time/now)))
                      (time/minute (or (:to @time-range) (time/now))))]
              (when on-time-change (on-time-change nil dt))
              (fmt/unparse dt-formatter dt)))]]
        ;; Dialog with custom time and date ranges
        [time-range-dialog show-dialog? time-range date-range dialog-style]]])))


(defn- time-picker-component
  [custom-opts]
  "Reagent wrapper around time range picker component."
  (r/create-class {:display-name "ReflectiTimePicker"
                   :render (fn [] (time-pick custom-opts))}))
