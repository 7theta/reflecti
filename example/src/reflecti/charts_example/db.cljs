;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.charts-example.db
  (:require [reagent.core :as r]))

(declare default-db)

(defn generate-bullet-data
  ([] (generate-bullet-data nil))
  ([data]
   (merge {:title "Numbers"
           :subtitle "(per number)"
           :ranges [(+ (rand-int (- 151 120)) 150) (+ (rand-int (- 226 210)) 225) (+ (rand-int (- 301 290)) 300)]
           :measures [(+ (rand-int (- 231 200)) 230)]
           :markers [(+ (rand-int (- 221 200)) 220)]}
          data)))

(defn generate-h-bar-data
  ([] (generate-h-bar-data nil))
  ([data]
   (or data
       [{:key "Series 1"
         :values (mapv (fn [k v] {:label k :value v})
                       ["A" "B" "C" "D"
                        "E" "F" "G" "H"]
                       (repeatedly 8 #(rand-int 30)))}
        {:key "Series 2"
         :values (mapv (fn [k v] {:label k :value v})
                       ["A" "B" "C" "D"
                        "E" "F" "G" "H"]
                       (repeatedly 8 #(rand-int 30)))}])))

(defn generate-v-bar-data
  ([] (generate-v-bar-data nil))
  ([data]
   (or data
       [{:key "Series 1"
         :values (mapv (fn [k v] {:label k :value v})
                       ["A" "B" "C" "D" "E" "F"
                        "G" "H" "I" "J" "K"
                        "L" "M" "N" "O"]
                       (repeatedly 15 #(rand-int 30)))}])))

(defn generate-area-data
  ([] (generate-area-data nil))
  ([data]
   (or data
       (mapv (fn [k v] {:key k :values v})
             (range 1 8)
             (repeat 8 (mapv (fn [a b] [a b]) (range 1 8) (take 8 (iterate #(+ % (rand-int 8)) 0))))))))

(defn generate-line-data
  ([] (generate-line-data nil))
  ([data]
   (or data
       [{:key "Series 1"
         :values (mapv (fn [x y] {:x x :y y})
                       (range 1 20)
                       (take 20 (iterate #(+ % (rand-int 20)) 0)))}
        {:key "Series 2"
         :values (mapv (fn [x y] {:x x :y y})
                       (range 1 20)
                       (take 20 (iterate #(+ % (rand-int 20)) 0)))}])))

(defn update-data
  [type]
  (case type
    :bullet (swap! default-db assoc :bullet-data (generate-bullet-data))
    :line (swap! default-db assoc :line-data (generate-line-data))
    :area (swap! default-db assoc :area-data (generate-area-data))
    :h-bar (swap! default-db assoc :h-bar-data (generate-h-bar-data))
    :v-bar (swap! default-db assoc :v-bar-data (generate-v-bar-data))))

(def default-db
  (r/atom {:bullet-data (generate-bullet-data)
           :h-bar-data (generate-h-bar-data)
           :v-bar-data (generate-v-bar-data)
           :line-data (generate-line-data)
           :area-data (generate-area-data)}))
