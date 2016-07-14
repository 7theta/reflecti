;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always reflecti.slider-example.core
  (:require [reflecti.slider-example.db :as db]
            [reflecti.core :as reflecti]
            [reflecti.themes :as themes]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as mui]))

(defn slider-example []
  (let [center-text {:text-align "center"}
        page-style {:position "relative"}
        custom-opts {:on-slide db/on-slide
                     :theme themes/default-slider-theme}]
    [mui/mui-theme-provider
     {:mui-theme (ui/get-mui-theme)}
     [:div {:style page-style}
      [:h3 {:style center-text} "Slider value: " @db/slider-value]
      (reflecti/slider custom-opts)]]))
