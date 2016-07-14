;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always reflecti.table-example.core
  (:require [reflecti.table-example.db :as db]
            [reflecti.core :as reflecti]
            [reflecti.datasource-local :refer [LocalDataSource]]))

(defonce tall-data-source (LocalDataSource. db/tall-db))
(defonce wide-data-source (LocalDataSource. db/wide-db))

(defn table-example []
  (let [table-layout-on-page {:style {:float "left"
                                      :margin "5px"}}
        tall-table-opts {:table {:background-color "#FFF"
                                 :color "#212121"}
                         :headers {:border-bottom-color "#DFDFDF"}
                         :cells {:border-bottom-color "#DFDFDF"
                                 :hover-color "#EDEDED"}}
        wide-table-opts {:headers {:display-labels {:country "Country"
                                                    :capital-city "Capital City"
                                                    :main-language "Main Language"
                                                    :currency "Currency"
                                                    :population "Population"
                                                    :gdp "GDP"}
                                   :order [:country
                                           :capital-city
                                           :currency
                                           :population
                                           :main-language
                                           :gdp]}}]
    [:div
     [:div table-layout-on-page
      [:h3 "Tall Table"]
      (reflecti/table tall-data-source tall-table-opts)]
     [:div table-layout-on-page
      [:h3 "Wide Table"]
      (reflecti/table wide-data-source wide-table-opts)]]))
