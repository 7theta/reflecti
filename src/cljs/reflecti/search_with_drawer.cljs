;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.search-with-drawer
  (:require [reflecti.themes :as themes]
            [reflecti.search :as search]
            [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as mui]
            [cljs-react-material-ui.core :as ui]))

;;; Forward Declarations

(declare drawer)

;;; Public

(defn side-drawer-with-search
  "A side drawer displaying 'drawer-contents' controlled by boolean 'open-drawer?'.
  A callback function can be provided in 'on-drawer-open' for when the side drawer
  is requested to open or close.

  The floating search box uses a dropdown to display 'search-suggestions'. Each can
  be clicked and a callback function 'on-suggestion-click' will be fired. When text
  is entered in the search input box or when the search button is clicked or when
  the enter key is pressed, the 'on-search' callback function is called. Whenever
  the clear button is clicked, 'on-clear' will be called."
  ([] (side-drawer-with-search nil))
  ([{:keys [drawer-contents on-drawer-open open-drawer?
            search-suggestions on-search on-suggestion-click
            drawer-theme search-theme]
     :as custom-opts}]
   [mui/mui-theme-provider
    {:mui-theme (ui/get-mui-theme drawer-theme)}
    (drawer custom-opts)]))

;;; Implementation

(defn- drawer
  [{:keys [drawer-contents on-drawer-open open-drawer?
           search-suggestions on-search on-clear on-suggestion-click
           drawer-theme search-theme]}]
  (let [{:keys [header-style
                width
                style
                overlay-style]} (merge themes/default-drawer-theme drawer-theme)
        palette (:palette drawer-theme)
        show-drawer? (or open-drawer? false)]
    [:div
     ;; Side Drawer
     [mui/drawer {:docked false
                  :width width
                  :style style
                  :overlayStyle overlay-style
                  :open show-drawer?
                  :onRequestChange (fn [open? reason]
                                     (on-drawer-open open? reason))}
      ;; Offset results by search bar height
      [:div {:style (merge {:height 64
                            :width "100%"
                            :background-color (:primary1Color palette)}
                           header-style)}]
      ;; Search results
      drawer-contents]
     ;; Search Bar
     (search/search-bar {:search-suggestions search-suggestions
                         :on-search on-search
                         :on-clear on-clear
                         :on-suggestion-click on-suggestion-click
                         :theme search-theme})]))
