;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.time-picker-example.core
  (:require [reflecti.core :as reflecti]
            [reflecti.themes :as themes]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as mui]))

(defn time-picker-example []
  (let [center-text {:text-align "center"}
        page-style {:position "relative"
                    :text-align "center"
                    :width "50%"
                    :margin "0 auto"}
        custom-opts {:on-time-change (fn [from to]
                                       (js/console.log "Time update: " from to))
                     :theme themes/default-date-time-theme}]
    [mui/mui-theme-provider
     {:mui-theme (ui/get-mui-theme)}
     [:div {:style page-style}
      [:h3 {:style center-text} "Pick a date and time."]
      (reflecti/date-time-picker custom-opts)]]))
