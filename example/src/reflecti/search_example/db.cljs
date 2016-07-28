;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.search-example.db
  (:require [reagent.core :as r]))

(defonce open? (r/atom false))

(defonce clicked-suggestion (r/atom "Pick a suggestion!"))

(defonce all-suggestions
  (r/atom
   [{:display-text "Apple"}
    {:display-text "Apricot"}
    {:display-text "Avocado"}
    {:display-text "Banana"}
    {:display-text "Bilberry"}
    {:display-text "Blackberry"}
    {:display-text "Blackcurrant"}
    {:display-text "Blueberry"}
    {:display-text "Boysenberry"}
    {:display-text "Blood Orange"}
    {:display-text "Cantaloupe"}
    {:display-text "Currant"}
    {:display-text "Cherry"}
    {:display-text "Cherimoya"}
    {:display-text "Cloudberry"}
    {:display-text "Coconut"}
    {:display-text "Cranberry"}
    {:display-text "Clementine"}
    {:display-text "Damson"}
    {:display-text "Date"}
    {:display-text "Dragonfruit"}
    {:display-text "Durian"}
    {:display-text "Elderberry"}
    {:display-text "Feijoa"}
    {:display-text "Fig"}
    {:display-text "Goji berry"}
    {:display-text "Gooseberry"}
    {:display-text "Grape"}
    {:display-text "Grapefruit"}
    {:display-text "Guava"}
    {:display-text "Honeydew"}
    {:display-text "Huckleberry"}
    {:display-text "Jabouticaba"}
    {:display-text "Jackfruit"}
    {:display-text "Jambul"}
    {:display-text "Jujube"}
    {:display-text "Juniper berry"}
    {:display-text "Kiwi fruit"}
    {:display-text "Kumquat"}
    {:display-text "Lemon"}
    {:display-text "Lime"}
    {:display-text "Loquat"}
    {:display-text "Lychee"}
    {:display-text "Nectarine"}
    {:display-text "Mango"}
    {:display-text "Marion berry"}
    {:display-text "Melon"}
    {:display-text "Miracle fruit"}
    {:display-text "Mulberry"}
    {:display-text "Mandarine"}
    {:display-text "Olive"}
    {:display-text "Orange"}
    {:display-text "Papaya"}
    {:display-text "Passionfruit"}
    {:display-text "Peach"}
    {:display-text "Pear"}
    {:display-text "Persimmon"}
    {:display-text "Physalis"}
    {:display-text "Plum"}
    {:display-text "Pineapple"}
    {:display-text "Pumpkin"}
    {:display-text "Pomegranate"}
    {:display-text "Pomelo"}
    {:display-text "Purple Mangosteen"}
    {:display-text "Quince"}
    {:display-text "Raspberry"}
    {:display-text "Raisin"}
    {:display-text "Rambutan"}
    {:display-text "Redcurrant"}
    {:display-text "Salal berry"}
    {:display-text "Satsuma"}
    {:display-text "Star fruit"}
    {:display-text "Strawberry"}
    {:display-text "Squash"}
    {:display-text "Salmonberry"}
    {:display-text "Tamarillo"}
    {:display-text "Tamarind"}
    {:display-text "Tomato"}
    {:display-text "Tangerine"}
    {:display-text "Ugli fruit"}
    {:display-text "Watermelon"}]))

(defonce search-suggestions (r/atom @all-suggestions))

(defn drawer-contents [] clicked-suggestion)

(defn on-drawer-open
  [bool _]
  (reset! open? bool))

(defn on-search
  [search-text]
  (reset! search-suggestions
          (filterv #(re-matches (re-pattern (str "(?i).*" search-text ".*"))
                                (:display-text %))
                   @all-suggestions)))

(defn on-clear []
  (reset! clicked-suggestion "")
  (reset! search-suggestions @all-suggestions))

(defn on-suggestion-click
  [suggestion]
  (reset! open? true)
  (reset! clicked-suggestion (:display-text suggestion)))
