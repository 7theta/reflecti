;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.slider
  (:require [reflecti.themes :as themes]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as mui]
            [cljs-react-material-ui.core :as ui]
            [cljs-time.core :as time]
            [cljs-time.coerce :as coerce]))

;;; Forward Declarations

(declare slider-component)

;;; Public

(defn range-slider
  "Slider that allows for precise control over some range."
  ([] [slider-component nil])
  ([custom-opts] [slider-component custom-opts]))

;;; Implementation

(defn- slider
  [{:keys [on-slide theme]}]
  (let [{:keys [style slider-style]} (or theme themes/default-slider-theme)]
    [mui/paper {:zDepth 3
                :style style}
     [mui/slider {:style slider-style
                  :tooltip "Time range"
                  :disableFocusRipple true
                  :onChange (fn [event value]
                              (when on-slide (on-slide event value)))}]]))


(defn- slider-component
  "Reagent wrapper for a range slider."
  [custom-opts]
  (r/create-class {:display-name "ReflectiTimeSlider"
                   :render (fn []
                             [mui/mui-theme-provider
                              {:mui-theme (ui/get-mui-theme (:theme custom-opts))}
                              (slider custom-opts)])}))
