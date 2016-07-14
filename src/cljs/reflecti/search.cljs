;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.search
  (:require [reflecti.themes :as themes]
            [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.reagent :as mui]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as muic]))

;;; Forward Declarations

(declare floating-search-bar)

;;; Public

(defn search-bar
  "Search box that uses a dropdown to display 'search-suggestions'. Each can
  be clicked and a callback function 'on-suggestion-click' will be fired. The
  'on-search' callback function is called when text is entered in the search
  input box or when the search button is clicked or when the enter key is pressed.
  Whenever the clear button is clicked, 'on-clear' will be called."
  ([] (search-bar nil))
  ([{:keys [search-suggestions
            on-search
            on-clear
            on-suggestion-click
            theme]
     :as custom-opts}]
   (let [{:keys [width
                 height
                 style
                 icon-bar-style
                 text-field-style
                 suggestions-pane-style
                 hint-text]
          :as search-theme} (merge themes/default-search-theme theme)
         default-style {:width 400
                        :margin-left 40
                        :height 48
                        :zIndex 3}]
     [mui/mui-theme-provider
      {:mui-theme (ui/get-mui-theme theme)}
      [mui/paper {:zDepth (or (:zIndex default-style) (:zIndex style))
                  :style (merge default-style style)}
       [floating-search-bar (merge {:suggestions (map :display-text search-suggestions)
                                    :theme search-theme
                                    :on-selection (fn [_] )
                                    :close-click-away true
                                    :search-fn on-search
                                    :clear-fn on-clear
                                    :suggestion-fn on-suggestion-click
                                    :style {:box-shadow "none"}})]]])))

;;; Implementation

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(defn- first-tab-child
  [el]
  (if (.getAttribute el "tabindex")
    el
    (some first-tab-child (seq (.-childNodes el)))))

(defn- suggestions-pane []
  (r/create-class
   {:reagent-render
    (fn [{:keys [suggestions style show on-selection menu-id]}]
      (if (and (seq suggestions) show)
        [mui/paper {:style {:transition "none"}
                    :rounded false}
         [mui/menu {:id menu-id
                    :autoWidth false
                    :disableAutoFocus true
                    :initiallyKeyboardFocused true
                    :style (merge {:width "100%"} (:drop-down-style style))
                    :listStyle {:display "block"
                                :width "100%"}}
          (doall
           (map-indexed
            (fn [idx suggestion]
              [mui/menu-item
               {:primaryText suggestion
                :disableFocusRipple true
                :key idx
                :onTouchTap (partial on-selection suggestion)
                :innerDivStyle (or (:drop-down-item-style style)
                                   {:padding-left 16
                                    :padding-top 0
                                    :padding-bottom 0})}])
            suggestions))]]
        [:div]))}))

(defn- floating-search-bar []
  (let [default-max-suggestions 5
        menu-id (str "suggestions-pane-menu-" (gensym))
        default-id (str "search-bar-" (gensym))
        text-field-id (str "search-text-field" (gensym))]
    (r/create-class
     {:reagent-render
      (fn [{:keys [max-suggestions
                  suggestions
                  theme
                  style
                  on-selection
                  close-click-away
                  search-fn
                  clear-fn
                  suggestion-fn
                  id]
           :or {close-click-away true}}]
        (let [{:keys [width
                      height
                      icon-bar-style
                      text-field-style
                      suggestions-pane-style
                      hint-text]} theme
              palette (:palette theme)
              total-width (or width 400)
              total-height (or height 48)
              icon-bar-width (or (:width icon-bar-style) 100)
              icon-width (or (get-in icon-bar-style [:icon-style :width]) 48)
              text-field-padding-left (or (:padding-left text-field-style) 16)
              text-field-width (or (:width text-field-style) total-width)
              inactive-color (:disabledColor palette)
              active-color (:primary1Color palette)
              this (r/current-component)
              {:keys [text
                      show-suggestions
                      text-field-focus
                      search-icon-hover
                      clear-icon-hover]} (r/state this)
              selection-handler (fn [text]
                                  (suggestion-fn text)
                                  (r/set-state this {:text text :show-suggestions false})
                                  (when (fn? on-selection)
                                    (on-selection text)))
              suggestions (not-empty
                           (take (or max-suggestions
                                     default-max-suggestions)
                                 suggestions))
              focused? (= (.-activeElement js/document)
                          (.getElementById js/document text-field-id))
              icon-fill (if (and text-field-focus
                                 focused?
                                 (not-empty text))
                          active-color
                          inactive-color)
              search-icon-fill (if (and search-icon-hover
                                        (not-empty text))
                                 active-color
                                 icon-fill)
              clear-icon-fill (if (and clear-icon-hover
                                       (not-empty text))
                                active-color
                                icon-fill)]
          [mui/paper {:rounded true
                      :id (or id default-id)
                      :style (merge {:width total-width
                                     :height total-height
                                     :transition "none"}
                                    style)}
           (if (and close-click-away
                    show-suggestions
                    (seq suggestions))
             [:div {:style {:position "fixed"
                            :top 0
                            :left 0
                            :width (.-innerWidth js/window)
                            :height (.-innerHeight js/window)
                            :background-color "transparent"}
                    :onClick
                    (fn [_] (r/set-state this {:show-suggestions false
                                              :text-field-focus false}))}]
             [:div])
           [:span {:style {:width text-field-width
                           :padding-left text-field-padding-left}}
            [mui/text-field {:id text-field-id
                             :style {:width text-field-width}
                             :autoComplete "off"
                             :fullWidth true
                             :hintText (or hint-text "Search")
                             :underlineShow false
                             :multiLine false
                             :value (str text)
                             :onKeyDown (fn [e]
                                          (let [keynum (if (.-event js/window)
                                                         (.-keyCode e)
                                                         (.-which e))]
                                            (condp = keynum
                                              ;; Enter Key
                                              13 (when-let [suggestion (first suggestions)]
                                                   (selection-handler suggestion)
                                                   (.preventDefault e))
                                              ;; Down Key
                                              40 (when-let [menu-el (.getElementById js/document menu-id)]
                                                   (when-let [el (first-tab-child menu-el)]
                                                     (.focus el)
                                                     (.preventDefault e)))
                                              ;; Backspace
                                              13 (when-not (str text)
                                                   (.preventDefault e))
                                              nil)))
                             :onFocus (fn [_] (r/set-state this {:text-field-focus true
                                                                :show-suggestions true}))
                             :onChange (fn [event search-text]
                                         (if search-text
                                           (when search-fn (search-fn search-text))
                                           (r/set-state this {:text ""}))
                                         (r/set-state this {:text search-text
                                                            :show-suggestions true}))}]]
           [:span
            (muic/action-search {:style (merge (:icon-style icon-bar-style)
                                               {:fill search-icon-fill}
                                               (when (not-empty text)
                                                 {:cursor "pointer"}))
                                 :onClick (fn [_]
                                            (when-let [suggestion (first suggestions)]
                                              (search-fn suggestion)
                                              (selection-handler suggestion)))
                                 :onMouseOver (fn [_] (r/set-state this {:search-icon-hover true}))
                                 :onMouseOut (fn [_] (r/set-state this {:search-icon-hover false}))})
            [mui/toolbar-separator {:style (:separator-style icon-bar-style)}]
            (muic/content-clear {:style (merge (:icon-style icon-bar-style)
                                               {:fill clear-icon-fill}
                                               (when (not-empty text)
                                                 {:cursor "pointer"}))
                                 :onClick (fn [_]
                                            (when (not-empty text)
                                              (r/set-state this {:text ""})
                                              (clear-fn)))
                                 :onMouseOver (fn [_] (r/set-state this {:clear-icon-hover true}))
                                 :onMouseOut (fn [_] (r/set-state this {:clear-icon-hover false}))})]
           (if (and show-suggestions
                    (seq suggestions))
             [mui/divider]
             [:div {:style {:height 0}}])
           [suggestions-pane {:menu-id menu-id
                              :style suggestions-pane-style
                              :suggestions suggestions
                              :show show-suggestions
                              :on-selection selection-handler}]]))})))
