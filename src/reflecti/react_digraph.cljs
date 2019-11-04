(ns reflecti.react-digraph
  (:require [utilis.map :refer [compact]]
            [utilis.js :as j]
            [react-digraph :as dg]
            [reagent.core :as r]))


;;; Declarations

(declare default-graph-config default-node-types default-edge-types)

;;; API

(def layout-engine-type-snap-to-grid "SnapToGrid")
(def layout-engine-type-none "None")
(def layout-engine-type-vertical-tree "VerticalTree")
(def layout-engine-type-horizontal-tree "HorizontalTree")

(def GraphView (r/adapt-react-class dg/GraphView))
(defn graph-view
  [props]
  (let [this (r/current-component)]
    [GraphView
     (merge
      {:ref "GraphView"
       :node-key "id"
       :node-types default-node-types
       :node-subtypes {}
       :edge-types default-edge-types
       :edges []
       :nodes []
       :on-select-node (fn [])
       :on-create-node (fn [])
       :on-update-node (fn [])
       :on-delete-node (fn [])
       :on-select-edge (fn [])
       :on-create-edge (fn [])
       :on-swap-edge (fn [])
       :on-delete-edge (fn [])}
      (compact (r/props this)))]))

(def edge (r/adapt-react-class dg/Edge))
(def node (r/adapt-react-class dg/Node))

(defn pan-to-node
  ([graph-view node-id] (pan-to-node graph-view node-id true))
  ([graph-view node-id zoom?]
   (j/call graph-view :panToNode node-id zoom?)))

(defn default-node-shape
  [{:keys [id]}]
  (r/as-element
   [:symbol
    {:viewBox "0 0 100 100"
     :width "100"
     :height "100"
     :id id
     :key "0"}
    [:circle
     {:cx "50"
      :cy "50"
      :r "49"}]]))

(defn default-edge-shape
  [{:keys [id]}]
  (r/as-element
   [:symbol
    {:viewBox "0 0 50 50"
     :id id
     :key "0"}
    [:circle
     {:cx "25"
      :cy "25"
      :r "8"
      :fill "currentColor"}]]))

(defn node-type
  [{:keys [shape-id type-text shape]}]
  {:typeText type-text
   :shapeId shape-id
   :shape shape})

(defn edge-type
  [{:keys [shape-id shape]}]
  {:shapeId shape-id
   :shape shape})

(def default-node-types
  {:empty (node-type
           {:type-text "Empty"
            :shape-id "#empty"
            :shape default-node-shape})})

(def default-edge-types
  {:empty-edge (edge-type
                {:shape-id "#emptyEdge"
                 :shape default-edge-shape})})
