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
            suggestions-pane
            on-search
            on-clear
            on-suggestion-click
            theme
            icons-layout
            style
            search-bar-style
            theme]
     :or {theme themes/light-theme}
     :as custom-opts}]
   (let [default-style {:width 400
                        :height 48
                        :zIndex 3
                        :zDepth 3
                        :display "flex"
                        :margin "auto"}]
     [mui/mui-theme-provider
      {:mui-theme (ui/get-mui-theme theme)}
      [mui/paper {:zDepth (or (:zDepth style) (:zDepth default-style))
                  :style (merge default-style style)}
       [floating-search-bar (merge {:suggestions-pane suggestions-pane
                                    :suggestions search-suggestions
                                    :on-selection (fn [_] )
                                    :theme theme
                                    :close-click-away true
                                    :search-fn on-search
                                    :clear-fn on-clear
                                    :suggestion-fn on-suggestion-click
                                    :icons-layout (or icons-layout :right)
                                    :style (merge {:box-shadow "none"}
                                                  search-bar-style)}
                                   (select-keys custom-opts
                                                [:text-field-hint-style
                                                 :text-field-input-style
                                                 :text-field-style
                                                 :horizontal-divider-style
                                                 :suggestions-pane-style
                                                 :suggestion-style
                                                 :suggestions-menu-style]))]]])))

;;; Implementation

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(defn- first-tab-child
  [el]
  (if (.getAttribute el "tabindex")
    el
    (some first-tab-child (seq (.-childNodes el)))))

(defn- default-suggestions-pane
  []
  (r/create-class
   {:reagent-render
    (fn [{:keys [suggestions
                style
                suggestions-menu-style
                suggestion-style
                on-selection
                menu-id]}]
      [mui/paper {:style (merge {:transition "none"} style)
                  :rounded false}
       [mui/menu {:id menu-id
                  :autoWidth false
                  :disableAutoFocus true
                  :initiallyKeyboardFocused true
                  :style (merge {:width "100%"} suggestions-menu-style)
                  :listStyle {:display "block" :width "100%"}}
        (doall
         (map-indexed
          (fn [idx suggestion]
            [mui/menu-item
             {:primaryText (:display-text suggestion)
              :disableFocusRipple true
              :key idx
              :onTouchTap (partial on-selection suggestion)
              :innerDivStyle (merge {:cursor "pointer"}
                                    {:padding-left 16
                                     :padding-top 0
                                     :padding-bottom 0}
                                    suggestion-style)}])
          suggestions))]])}))

(defn- search-icon
  []
  (let [default-icon-style {:margin "auto" :width 48}
        default-color "#D3D3D3"]
    (fn [{:keys [style color hover-color on-touch-tap]}]
      (let [this (r/current-component)
            {:keys [hover?]} (r/state this)]
        [muic/action-search
         {:style (merge default-icon-style
                        {:fill (if hover?
                                 hover-color
                                 (or color default-color))}
                        style)
          :onClick (fn [_] (on-touch-tap))
          :onMouseOver (fn [_] (r/set-state this {:hover? true}))
          :onMouseOut (fn [_] (r/set-state this {:hover? false}))}]))))

(defn- clear-icon
  []
  (let [default-icon-style {:margin "auto" :width 48}
        default-color "#D3D3D3"]
    (fn [{:keys [style color hover-color on-touch-tap]}]
      (let [this (r/current-component)
            {:keys [hover?]} (r/state this)]
        [muic/content-clear
         {:style (merge default-icon-style
                        {:fill (if hover?
                                 hover-color
                                 (or color default-color))}
                        style)
          :onClick (fn [_] (on-touch-tap))
          :onMouseOver (fn [_] (r/set-state this {:hover? true}))
          :onMouseOut (fn [_] (r/set-state this {:hover? false}))}]))))

(defn- layer
  []
  (fn [{:keys [style on-touch-tap]}]
    [:div {:style (merge {:position "fixed"
                          :top 0
                          :left 0
                          :width (.-innerWidth js/window)
                          :height (.-innerHeight js/window)
                          :background-color "transparent"}
                         style)
           :onClick (fn [_] (on-touch-tap))}]))

(defn- floating-search-bar []
  (let [default-max-suggestions 5
        menu-id (str "suggestions-pane-menu-" (gensym))
        default-id (str "search-bar-" (gensym))
        text-field-id (str "search-text-field" (gensym))
        default-outer-container-style {:position "relative"
                                       :width "100%"
                                       :height "100%"}
        default-inner-container-style {:display "flex"
                                       :flex-direction "column"
                                       :position "absolute"
                                       :width "100%"}]
    (r/create-class
     {:reagent-render
      (fn [{:keys [theme

                  max-suggestions
                  suggestions

                  suggestions-pane

                  suggestion-fn
                  search-fn
                  clear-fn

                  on-selection
                  close-click-away

                  id
                  rounded
                  icons-layout

                  style

                  horizontal-divider-style

                  text-field-input-style
                  text-field-hint-style
                  text-field-style

                  suggestions-menu-style
                  suggestion-style
                  suggestions-pane-style

                  hint-text]
           :or {close-click-away true
                hint-text "Search"}}]
        (let [icons-layout (or (#{:right :wrap} icons-layout) :right)
              palette (:palette theme)
              inactive-color (or (:disabledColor palette)
                                 (:disabled-color palette))
              active-color (:primary1Color palette)
              this (r/current-component)
              {:keys [text
                      show-suggestions
                      text-field-focus
                      search-icon-hover
                      clear-icon-hover]} (r/state this)
              selection-handler (fn [selection]
                                  (suggestion-fn selection)
                                  (r/set-state this {:text (:display-text selection)
                                                     :show-suggestions false})
                                  (when (fn? on-selection)
                                    (on-selection selection)))
              suggestions (not-empty
                           (take (or max-suggestions
                                     default-max-suggestions)
                                 suggestions))
              focused? (= (.-activeElement js/document)
                          (.getElementById js/document text-field-id))
              show-suggestions? (and show-suggestions (not-empty text))
              icon-fill (if (and text-field-focus focused? (not-empty text))
                          active-color
                          inactive-color)

              search-container-id (or id default-id)
              separator-style {:margin-left 0
                               :top 0
                               :margin "auto"
                               :background-color inactive-color}
              default-text-field-style {:padding-left (if (= icons-layout :right) 16 0)}
              icon-style (when (not-empty text) {:cursor "pointer"})
              search-icon (fn []
                            [search-icon
                             {:hover-color (or (and show-suggestions?
                                                    active-color)
                                               icon-fill)
                              :color icon-fill
                              :style (merge {:margin-right 1} icon-style)
                              :on-touch-tap (fn []
                                              (when-let [suggestion (first suggestions)]
                                                (search-fn (:display-text suggestion))
                                                (selection-handler suggestion)))}])
              clear-icon (fn []
                           [clear-icon
                            {:hover-color (or (and show-suggestions?
                                                   active-color)
                                              icon-fill)
                             :color icon-fill
                             :style (merge {:margin-left 1} icon-style)
                             :on-touch-tap (fn [] (when (not-empty text)
                                                   (r/set-state this {:text ""})
                                                   (when clear-fn (clear-fn))))}])
              layer-index 10]

          ;; Component Outer Container
          [:div {:style default-outer-container-style}

           ;; Click Away Layer
           (let [layer-active? (and close-click-away
                                    show-suggestions?)]
             [layer {:style {:z-index layer-index
                             :pointer-events (if layer-active? "auto" "none")}
                     :on-touch-tap (fn [] (r/set-state this
                                                      {:show-suggestions false
                                                       :text-field-focus false}))}])


           ;; Inner Container (Vertical Layout)
           [:div {:style (merge default-inner-container-style
                                {:z-index (inc layer-index)})}

            ;; Search Bar Container (Horizontal Layout)
            [mui/paper {:rounded (or (when-not (nil? rounded)
                                       (boolean rounded))
                                     true)
                        :id search-container-id
                        :style (merge {:display "flex"
                                       :transition "none"}
                                      style)}

             (when (= icons-layout :wrap) [search-icon])

             ;; Text field
             [mui/text-field {:id text-field-id
                              :hintStyle text-field-hint-style
                              :inputStyle text-field-input-style
                              :style (merge default-text-field-style text-field-style)
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
                              :onFocus (fn [e]
                                         (r/set-state this {:text-field-focus true
                                                            :show-suggestions true}))
                              :onChange (fn [event search-text]
                                          (if search-text
                                            (when search-fn (search-fn search-text))
                                            (r/set-state this {:text ""}))
                                          (r/set-state this {:text search-text
                                                             :show-suggestions (boolean
                                                                                (seq search-text))}))}]

             ;; Magnifying Glass Icon
             (when (= icons-layout :right) [search-icon])

             ;; Separator
             (when (= icons-layout :right)
               [mui/toolbar-separator {:style separator-style}])

             ;; X/Close Icon
             [clear-icon]]

            ;; Suggestions divider
            (when show-suggestions?
              [mui/divider
               {:style (merge {:margin-top 0}
                              horizontal-divider-style)}])

            ;; Dropdown
            (when show-suggestions?
              (or (when suggestions-pane
                    [suggestions-pane {:suggestions suggestions
                                       :menu-id menu-id
                                       :on-selection selection-handler}])
                  [default-suggestions-pane {:menu-id menu-id
                                             :style suggestions-pane-style
                                             :suggestions-menu-style suggestions-menu-style
                                             :suggestion-style suggestion-style
                                             :suggestions suggestions
                                             :on-selection selection-handler}]))]]))})))
