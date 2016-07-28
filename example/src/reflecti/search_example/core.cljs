;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always reflecti.search-example.core
  (:require [reflecti.search-example.db :as db]
            [reflecti.core :as reflecti]
            [reflecti.themes :as themes]
            [reagent.core :as r]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as mui]))

(defn search-example []
  (let [center-text {:text-align "center"}
        page-style {:position "relative"}
        custom-opts {:search-suggestions @db/search-suggestions
                     :drawer-contents @(db/drawer-contents)
                     :on-drawer-open db/on-drawer-open
                     :on-search db/on-search
                     :on-clear db/on-clear
                     :on-suggestion-click db/on-suggestion-click
                     :open-drawer? @db/open?
                     :drawer-theme themes/default-drawer-theme
                     :search-theme themes/light-theme}]
    [mui/mui-theme-provider
     {:mui-theme (ui/get-mui-theme)}
     [:div {:style page-style}
      [:h3 {:style center-text} "Search for a fruit, click a suggestion, and the side drawer will open."]
      [:h1 {:style center-text} "🍉🍎🍊🍌🍇🍑🍒🍍🍓"]
      (reflecti/search-drawer custom-opts)]]))
