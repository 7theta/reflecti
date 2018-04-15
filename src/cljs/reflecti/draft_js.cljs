;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.draft-js
  (:refer-clojure :exclude [empty?])
  (:require [reflecti.ant-design :as antd]
            [reflecti.font-awesome :as fa]
            [utilis.map :refer [deep-merge]]
            [utilis.fn :refer [fsafe]]
            [reagent.core :refer [adapt-react-class]]
            [cljsjs.draft-js]))

(def ^:private djs-editor (adapt-react-class js/Draft.Editor))

(defn state
  [contents]
  (if contents
    (.createWithContent js/Draft.EditorState contents)
    (.createEmpty js/Draft.EditorState)))

(defn text-state
  [text]
  (state (.createFromText js/Draft.ContentState text)))

(defn deserialize
  [text]
  (state (when text (js/Draft.convertFromRaw (js/JSON.parse text)))))

(defn serialize
  [state]
  (js/JSON.stringify (js/Draft.convertToRaw (.getCurrentContent state))))

(defn apply-style
  [contents style]
  (.toggleInlineStyle js/Draft.RichUtils contents style))

(defn empty?
  [state]
  (not (.hasText (.getCurrentContent state))))

(defn editor
  [{:keys [state on-change disabled placeholder style]}]
  (let [on-change (fsafe on-change)]
    [:div (when-not disabled {:class-name "editor-root"})
     [djs-editor {:editor-state state
                  :placeholder placeholder
                  :on-change on-change
                  :read-only disabled
                  :spell-check true
                  :handle-key-command
                  (fn [command]
                    (if-let [new-state (.handleKeyCommand js/Draft.RichUtils state command)]
                      (do (on-change new-state) "handled")
                      "not-handled"))
                  :style style}]]))

(defn rich-editor
  [{:keys [state on-change disabled placeholder style error] :as opts}]
  [:div {:style (merge
                 {:border "1px solid"
                  :border-color (if error "#f5222d" "#d9d9d9")
                  :border-radius "4px"
                  :width "100%"}
                 style)}
   (let [apply-text-style (fn [style event]
                            (.preventDefault event)
                            ((fsafe on-change) (apply-style state style)))
         style-button (fn [{:keys [icon text-style button-style]}]
                        [:div {:on-mouse-down (partial apply-text-style text-style)
                               :style (merge
                                       {:display "inline-block"}
                                       button-style)}
                         [antd/button {:style {:border 0}}
                          [fa/icon {:type :light :name icon}]]])]
     [:span
      [style-button {:icon "bold" :text-style "BOLD" :button-style {:margin-left "8px"}}]
      [antd/divider {:type "vertical"}]
      [style-button {:icon "italic" :text-style "ITALIC"}]
      [antd/divider {:type "vertical"}]
      [style-button {:icon "underline" :text-style "UNDERLINE"}]
      [antd/divider {:type "vertical"}]
      [style-button {:icon "strikethrough" :text-style "STRIKETHROUGH"}]])
   [antd/divider {:style {:margin "0px"}}]
   [:div {:style {:margin "4px 11px"
                  :min-height "200px"}}
    [editor {:state state
             :on-change (fsafe on-change)}]]])
