;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.table-example.db
  (:require [reagent.core :as r]))

(def tall-db
  (r/atom [{:letter "A" :count 14810  :frequency 8.12}
           {:letter "B" :count 2715  :frequency 1.49}
           {:letter "C" :count 4943  :frequency 2.71}
           {:letter "D" :count 7874  :frequency 4.32}
           {:letter "E" :count 21912 :frequency 12.02}
           {:letter "F" :count 4200  :frequency 2.30}
           {:letter "G" :count 3693  :frequency 2.03}
           {:letter "H" :count 10795 :frequency 5.92}
           {:letter "I" :count 13318 :frequency 7.31}
           {:letter "J" :count 188   :frequency 0.10}
           {:letter "K" :count 1257  :frequency 0.69}
           {:letter "L" :count 7253  :frequency 3.98}
           {:letter "M" :count 4761  :frequency 2.61}
           {:letter "N" :count 12666 :frequency 6.95}
           {:letter "O" :count 14003 :frequency 7.68}
           {:letter "P" :count 3316  :frequency 1.82}
           {:letter "Q" :count 205   :frequency 0.11}
           {:letter "R" :count 10977 :frequency 6.02}
           {:letter "S" :count 11450 :frequency 6.28}
           {:letter "T" :count 16587 :frequency 9.10}
           {:letter "U" :count 5246  :frequency 2.88}
           {:letter "V" :count 2019  :frequency 1.11}
           {:letter "W" :count 3819  :frequency 2.09}
           {:letter "X" :count 315   :frequency 0.17}
           {:letter "Y" :count 3853  :frequency 2.11}
           {:letter "Z" :count 128   :frequency 0.07}]))

(def wide-db
  (r/atom [{:country "Canada"
            :capital-city "Ottawa"
            :main-language "English"
            :currency "CAD"
            :population 33487208
            :gdp 1309350}
           {:country "South Korea"
            :capital-city "Seoul"
            :main-language "Korean"
            :currency "KRW"
            :population 48508972
            :gdp 1338848}
           {:country "Spain"
            :capital-city "Madrid"
            :main-language "Spanish"
            :currency "EUR"
            :population 40525002
            :gdp 1406218}
           {:country "Mexico"
            :capital-city "Mexico City"
            :main-language "Spanish"
            :currency "MXN"
            :population 111211789
            :gdp 1579207}
           {:country "Italy"
            :capital-city "Rome"
            :main-language "Italian"
            :currency "EUR"
            :population 58126212
            :gdp 1819350}
           {:country "Brazil"
            :capital-city "Brasilia"
            :main-language "Portuguese"
            :currency "BRL"
            :population 198739269
            :gdp 2027141}
           {:country "France"
            :capital-city "Paris"
            :main-language "French"
            :currency "EUR"
            :population 64057792
            :gdp 2126719}
           {:country "United Kingdom"
            :capital-city "London"
            :main-language "English"
            :currency "GBP"
            :population 61113205
            :gdp 2230632}
           {:country "Russia"
            :capital-city "Moscow"
            :main-language "Russian"
            :currency "RUB"
            :population 140041247
            :gdp 2254664}
           {:country "Germany"
            :capital-city "Berlin"
            :main-language "German"
            :currency "EUR"
            :population 82329758
            :gdp 2914473}
           {:country "India"
            :capital-city "New Delhi"
            :main-language "Hindi"
            :currency "INR"
            :population 1166079217
            :gdp 3381630}
           {:country "Japan"
            :capital-city "Tokyo"
            :main-language "Japanese"
            :currency "JPY"
            :population 127078679
            :gdp 4320675}
           {:country "China"
            :capital-city "Beijing"
            :main-language "Chinese"
            :currency "RMB"
            :population 1338612968
            :gdp 8031678}
           {:country "United States"
            :capital-city "Washington D.C."
            :main-language "English"
            :currency "USD"
            :population 307212123
            :gdp 14408249}]))
