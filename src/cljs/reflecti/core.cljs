;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.core
  (:require [reflecti.charts :as charts]
            [reflecti.table :as table]
            [reflecti.datasource :refer [DataSource]]
            [reflecti.search-with-drawer :as search-with-drawer]
            [reflecti.search :as search]
            [reflecti.slider :as slider]
            [reflecti.date-time-picker :as picker]))

(defn area
  [element-id chart-data & [chart-options]]
  [charts/area element-id chart-data chart-options])

(defn bullet
  [element-id chart-data & [chart-options]]
  [charts/bullet element-id chart-data chart-options])

(defn horizontal-bar
  [element-id chart-data & [chart-options]]
  [charts/horizontal-bar element-id chart-data chart-options])

(defn line
  [element-id chart-data & [chart-options]]
  [charts/line element-id chart-data chart-options])

(defn vertical-bar
  [element-id chart-data & [chart-options]]
  [charts/vertical-bar element-id chart-data chart-options])

(defn table
  {:pre (satisfies? DataSource data-source)}
  [data-source & [table-options]]
  [table/table data-source table-options])

(defn search-drawer
  [custom-opts]
  [search-with-drawer/side-drawer-with-search custom-opts])

(defn search-bar
  [custom-opts]
  [search/search-bar custom-opts])

(defn slider
  [custom-opts]
  [slider/range-slider custom-opts])

(defn date-time-picker
  [custom-opts]
  [picker/date-time-picker custom-opts])
