;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.re-charts
  (:require [cljsjs.recharts]
            [reagent.core :refer [adapt-react-class]]))

(def radar-chart (adapt-react-class js/Recharts.RadarChart))
(def radar (adapt-react-class js/Recharts.Radar))
(def legend (adapt-react-class js/Recharts.Legend))
(def polar-grid (adapt-react-class js/Recharts.PolarGrid))
(def polar-angle-axis (adapt-react-class js/Recharts.PolarAngleAxis))
(def polar-radius-axis (adapt-react-class js/Recharts.PolarRadiusAxis))

(def composed-chart (adapt-react-class js/Recharts.ComposedChart))
(def area-chart (adapt-react-class js/Recharts.AreaChart))
(def line-chart (adapt-react-class js/Recharts.LineChart))
(def bar-chart (adapt-react-class js/Recharts.BarChart))
(def scatter-chart (adapt-react-class js/Recharts.ScatterChart))
(def pie-chart (adapt-react-class js/Recharts.PieChart))

(def bar (adapt-react-class js/Recharts.Bar))
(def area (adapt-react-class js/Recharts.Area))
(def line (adapt-react-class js/Recharts.Line))
(def scatter (adapt-react-class js/Recharts.Scatter))
(def pie (adapt-react-class js/Recharts.Pie))
(def sector (adapt-react-class js/Recharts.Sector))

(def cell (adapt-react-class js/Recharts.Scatter))
(def brush (adapt-react-class js/Recharts.Brush))
(def x-axis (adapt-react-class js/Recharts.XAxis))
(def y-axis (adapt-react-class js/Recharts.YAxis))
(def z-axis (adapt-react-class js/Recharts.ZAxis))
(def reference-area (adapt-react-class js/Recharts.ReferenceArea))
(def reference-line (adapt-react-class js/Recharts.ReferenceLine))
(def cartesian-grid (adapt-react-class js/Recharts.CartesianGrid))
(def tooltip (adapt-react-class js/Recharts.Tooltip))
(def label (adapt-react-class js/Recharts.Label))
(def label-list (adapt-react-class js/Recharts.LabelList))
