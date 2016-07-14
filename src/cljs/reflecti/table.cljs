;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.table
  (:require [clojure.string :as str]
            [reflecti.themes :refer [default-table-options]]
            [reflecti.datasource :refer [data dispatch-sort Sortable DataSource]]
            [utilis.map :refer [deep-merge]]
            [reagent.core :as r]
            [schema.core :as schema :include-macros true]))

;;; Forward Declarations

(declare table-component)

;;; Public

(defn table
  "A collection of data ordered in rows and columns. Clicking a table header
  sorts the data by the sort method passed in through the 'data-source'."
  {:pre (satisfies? DataSource data-source)}
  ([data-source]
   (table data-source nil))
  ([data-source options]
   [table-component {:data-source data-source
                     :options (deep-merge default-table-options options)}]))

;;; Implementation

(defn sort-by-keys [m order]
  (let [order-map (apply hash-map (interleave order (range)))]
    (conj
     (sorted-map-by #(compare (order-map %1) (order-map %2)))
     (select-keys m order))))

(defn- ->header-maps
  [data labels order]
  (if (and labels order)
    (map (fn [h l]
           (merge h {:label (second l)}))
         (map-indexed #(hash-map :index % :header %2) (-> data first keys))
         (sort-by-keys labels order))
    (map-indexed #(hash-map :index % :header %2) (-> data first keys))))

(defn- format-header
  [header sort data-source]
  [:span (or (:label header)
             (-> header :header name (str/split #"-")
                 (->> (map str/capitalize)
                      (str/join " "))))
   [:span {:style {:visibility (if (= (:index @sort) (:index header))
                                 "visible"
                                 "hidden")}}
    (if (= :asc (:dir @sort)) " △" " ▽")]])

(defn- table-row
  [row-data {:keys [cells table]}]
  [:tr {:onMouseOver (fn [e] (aset (.. e -target -parentElement -style)
                                  "backgroundColor" (:hover-color cells)))
        :onMouseOut (fn [e] (aset (.. e -target -parentElement -style)
                                 "backgroundColor" (:background-color table)))}
   (doall (map (fn [value] ^{:key (gensym value)}
                 [:td {:style (merge {:text-align (if (number? value) "right" "left")}
                                     cells)}
                  value]) (mapv val row-data)))])

(defn- table-header
  [header-maps data-source sort options]
  [:tr (doall (map (fn [header]
                     ^{:key (gensym (:header header))}
                     [:th {:style options
                           :onClick (fn [_]
                                      (when (satisfies? Sortable data-source)
                                        (if (= (:index header) (:index @sort))
                                          (swap! sort assoc :dir (if (= :asc (:dir @sort)) :desc :asc))
                                          (swap! sort assoc :index (:index header)))
                                        (dispatch-sort data-source
                                                       (:header header)
                                                       (:dir @sort))))}
                      (format-header header sort data-source)]) header-maps))])

(defn- table-element
  [sort {:keys [data-source options]}]
  (let [data (-> data-source data deref)
        {:keys [headers table]} options
        order (:order headers)
        data (if order
               (map #(sort-by-keys % order) data)
               data)]
    [:table {:style table}
     [:thead [table-header
              (->header-maps data (:display-labels headers) order)
              data-source sort headers]]
     [:tbody (doall (map (fn [row] ^{:key (gensym (first row))}
                           [table-row (second row) options])
                         (map-indexed vector data)))]]))

(defn- table-component
  [props]
  (r/create-class {:get-initial-state (fn [_] {:sort (r/atom {:index nil :dir :asc})})
                   :reagent-render (fn [] (let [this (r/current-component)]
                                           [table-element (:sort (r/state this)) props]))}))
