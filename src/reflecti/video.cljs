(ns reflecti.video
  (:require ["react-player" :default ReactPlayer]
            ["screenfull" :as screenfull]
            ["react-dom" :as react-dom]
            [reflecti.ant-design :as antd]
            [reflecti.font-awesome :as fa]
            [clojure.string :as st]
            [utilis.types.number :refer [string->long]]
            [utilis.fn :refer [fsafe]]
            [reagent.core :as r
             :refer [current-component state set-state create-class
                     props adapt-react-class as-element]]
            [re-frame.core :refer [dispatch]]))

;;; Declarations

(def react-player (adapt-react-class ReactPlayer))

(def find-dom-node react-dom/findDOMNode)

(declare icon-button controls controls-wrapper format-control-tip bounding-rect
         seek-to play pause duration)

(defn- getDuration [r] ((aget r "getDuration")))
(defn- getCurrentTime [r] ((aget r "getCurrentTime")))
(defn- seekTo [r seek] ((aget r "seekTo") seek))
(defn- clientWidth [r] (aget r "clientWidth"))
(defn- clientHeight [r] (aget r "clientHeight"))
(defn- getInternalPlayer [r] ((aget r "getInternalPlayer")))
(defn- playedSeconds [r] (aget r "playedSeconds"))

(def controls-gradient
  "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAADGCAYAAAAT+OqFAAAAdklEQVQoz42QQQ7AIAgEF/T/D+kbq/RWAlnQyyazA4aoAB4FsBSA/bFjuF1EOL7VbrIrBuusmrt4ZZORfb6ehbWdnRHEIiITaEUKa5EJqUakRSaEYBJSCY2dEstQY7AuxahwXFrvZmWl2rh4JZ07z9dLtesfNj5q0FU3A5ObbwAAAABJRU5ErkJggg==)")

;;; API

(defn player
  []
  (let [player (r/atom nil)
        play-state (r/atom nil)
        seek-state (r/atom nil)
        fullscreen? (r/atom false)
        this-atom (atom nil)
        id (str "react-player-" (gensym))
        calc-sizes (fn [this]
                     (when this
                       (when-let [player (:ref @player)]
                         (let [wrapper-size (bounding-rect (js/document.getElementById id))]
                           (when (not= wrapper-size (:wrapper-size (state this)))
                             (let [player-size (bounding-rect (find-dom-node player))]
                               (set-state
                                this {:player-size player-size
                                      :player-aspect-ratio (/ (:width player-size)
                                                              (:height player-size))
                                      :wrapper-size wrapper-size})))))))
        fullscreen-listener (fn []
                              (->> screenfull/isFullscreen
                                   boolean
                                   (reset! fullscreen?)))
        resize-listener (fn [] (calc-sizes @this-atom))]
    (.addEventListener js/window "resize" resize-listener)
    (screenfull/on "change" fullscreen-listener)
    (create-class
     {:get-initial-state
      (fn [this]
        (let [{:keys [default-seek default-play?]} (props this)]
          (reset! this-atom this)
          (reset! seek-state (or default-seek 0))
          (reset!
           play-state
           (boolean
            (if (some? default-play?)
              default-play?
              false)))
          {:initial-progress? false}))
      :component-did-mount
      (fn [this]
        (let [seek @seek-state
              play? @play-state]
          (when (not= 0 seek) (seek-to @player seek))
          (if play?
            (play @player)
            (pause @player))))
      :component-will-unmount
      (fn [this]
        (.removeEventListener js/window "resize" resize-listener)
        (screenfull/off "change" fullscreen-listener))
      :reagent-render
      (fn [{:keys [url
                  controls
                  controls-props
                  ref
                  style
                  progress-interval
                  skip-interval-s
                  default-play?
                  default-seek]
           :or {controls controls
                progress-interval 500
                skip-interval-s 30}
           :as props}]
        (let [this (current-component)
              {:keys [dims initial-progress? loaded? player-size wrapper-size
                      player-aspect-ratio hover?]} (state this)
              on-ready (fn [player]
                         (let [internal-player (getInternalPlayer player)]
                           (set-state
                            this {:loaded? true
                                  :dims
                                  {:width (clientWidth internal-player)
                                   :height (clientHeight internal-player)}})))
              on-progress #(let [{:keys [initial-progress?]} (state this)]
                             (if initial-progress?
                               (reset! seek-state (playedSeconds %))
                               (set-state this {:initial-progress? true})))]
          [:div
           {:id id
            :on-mouse-over (fn [] (set-state this {:hover? true}))
            :on-mouse-out (fn [] (set-state this {:hover? false}))
            :style
            (merge
             {:display "flex"
              :flex-direction "column"
              :justify-content "center"
              :align-items "center"
              :width "100%"
              :position "relative"}
             style)}
           [react-player
            (merge
             {:ref #(when (not @player)
                      (let [player (reset! player
                                           {:ref %
                                            :id id
                                            :seek-state seek-state
                                            :play-state play-state
                                            :fullscreen? fullscreen?})]
                        (calc-sizes this)
                        ((fsafe ref) player)))
              :on-ready on-ready
              :on-start (fn [] (reset! play-state true))
              :on-play (fn [] (reset! play-state true))
              :on-pause (fn [] (reset! play-state false))
              :playing (boolean @play-state)
              :controls false
              :on-progress on-progress}
             (when player-aspect-ratio
               {:width (:width wrapper-size)
                :height (/ (:width wrapper-size) player-aspect-ratio)})
             (dissoc props
                     :ref
                     :style
                     :controls
                     :skip-interval-s
                     :controls-props
                     :default-seek
                     :default-play?
                     :full-width?))]
           [controls-wrapper
            {:controls controls
             :hover? hover?
             :controls-props
             (merge
              {:max-seek-seconds (duration @player)}
              controls-props)
             :player player
             :seek-state seek-state
             :play-state play-state
             :skip-interval-s skip-interval-s
             :fullscreen? @fullscreen?}]]))})))

(defn seek-to
  [player seek]
  (when-let [r (:ref player)]
    (seekTo r seek)
    (reset! (:seek-state player) seek)
    nil))

(defn duration
  [player]
  (when-let [r (:ref player)]
    (getDuration r)))

(defn fast-forward
  [player interval-s]
  (when-let [r (:ref player)]
    (seek-to
     player
     (->> (+ (getCurrentTime r) interval-s)
          (min (getDuration r))
          (max 0)))))

(defn rewind
  [player interval-s]
  (fast-forward player (- interval-s)))

(defn play
  [player]
  ((fsafe reset!) (:play-state player) true))

(defn pause
  [player]
  ((fsafe reset!) (:play-state player) false))

(defn fullscreen
  [player]
  (if screenfull/isFullscreen
    (screenfull/exit)
    (screenfull/request
     (js/document.getElementById (:id player)))))

(defn controls
  [{:keys [on-play
           on-pause
           on-rewind
           on-fast-forward
           on-seek
           on-fullscreen
           seek
           max-seek-seconds
           play?
           fullscreen?
           player-hover?

           rewind-icon
           fast-forward-icon
           play-icon
           pause-icon
           fullscreen-icon]}]
  (let [icon-style (when fullscreen? {:font-size 24})]
    [:div
     {:style
      (merge
       {:width "100%"
        :position "absolute"
        :bottom 0
        :left 0}
       (if (not fullscreen?)
         {:opacity (if player-hover? 1.0 0.0)
          :transition "opacity .25s cubic-bezier(0.0,0.0,0.2,1)"}))}
     [:div
      {:style
       {:position "absolute"
        :bottom 0
        :width "100%"
        :padding-top 49
        :height 100
        :z-index 0
        :background-repeat "repeat-x"
        :background-image controls-gradient
        :background-position "bottom"
        :pointer-events "none"}}]
     [:div {:style {:width "calc(100% - 32px)"
                    :margin-left 16
                    :position "relative"
                    :z-index 1
                    :top (if fullscreen? 6 12)}}
      [antd/slider
       {:style {:width "100%"
                :margin-left 0
                :margin-right 0
                :padding-left 0
                :padding-right 0}
        :default-value 0
        :value seek
        :max (long max-seek-seconds)
        :min 0
        :tip-formatter format-control-tip
        :on-change (fsafe on-seek)}]]
     [:div
      {:style
       {:flex-direction "row"
        :justify-content "center"
        :display "flex"
        :position "relative"
        :z-index 1}}
      [:div {:style {:width "100%"
                     :display "flex"
                     :flex-direction "row"
                     :justify-content "space-between"
                     :padding-left (if fullscreen? 25 16)
                     :padding-right (if fullscreen? 25 16)
                     :padding-bottom (if fullscreen? 8 4)}}
       [:div {:style {:display "flex"
                      :flex-direction "row"
                      :justify-content "flex-start"}}
        (if (not play?)
          [icon-button
           {:icon (merge {:name "play"
                          :style icon-style} play-icon)
            :on-click (fsafe on-play)}]
          [icon-button
           {:icon (merge {:name "pause"
                          :style icon-style} pause-icon)
            :on-click (fsafe on-pause)}])
        [icon-button
         {:icon
          (merge {:name "chevron-double-left"
                  :style icon-style} rewind-icon)
          :on-click (fsafe on-rewind)}]
        [icon-button
         {:icon
          (merge {:name "chevron-double-right"
                  :style icon-style} fast-forward-icon)
          :on-click (fsafe on-fast-forward)}]]
       [:div {:style {:display "flex"
                      :flex-direction "row"
                      :justify-content "flex-end"}}
        [icon-button
         {:icon
          (merge {:name (if fullscreen? "compress" "expand")
                  :style icon-style} fullscreen-icon)
          :on-click (fsafe on-fullscreen)}]]]]]))

;;; Private

(defn- icon-button
  [{:keys [style on-click title icon]}]
  (let [this (current-component)
        {:keys [hover?]} (state this)]
    [:div {:on-click on-click
           :on-mouse-over (fn [] (set-state this {:hover? true}))
           :on-mouse-out (fn [] (set-state this {:hover? false}))
           :style (merge {:cursor "pointer"
                          :height "100%"
                          :width "100%"
                          :display "flex"
                          :flex-direction "column"
                          :justify-content "center"
                          :align-items "center"}
                         style)}
     [fa/icon
      (merge
       {:type :light
        :name name
        :style (merge
                {:color (if hover? "#FFF" "rgba(255,255,255,0.90)")
                 :font-size 18
                 :padding 8}
                (:style icon))}
       (dissoc icon :style))]
     (when title [:h3 {:style {:text-align "center"}} title])]))

(defn- format-control-tip
  [value]
  (str (when value
         (let [minutes (long (/ value 60))
               seconds (mod value 60)]
           (str minutes ":"
                (if (< seconds 10)
                  (str "0" seconds)
                  seconds))))))

(defn- controls-wrapper
  [{:keys [controls
           player
           play-state
           seek-state
           skip-interval-s
           controls-props
           fullscreen?
           hover?]}]
  (when controls
    (let [seek @seek-state
          play? @play-state]
      [controls
       (merge
        {:play? play?
         :player-hover? hover?
         :fast-forward? true
         :rewind? true
         :seek seek
         :on-seek (partial seek-to @player)
         :on-rewind (partial rewind @player skip-interval-s)
         :on-fast-forward (partial fast-forward @player skip-interval-s)
         :on-pause (partial pause @player)
         :on-play (partial play @player)
         :on-fullscreen (partial fullscreen @player)
         :fullscreen? fullscreen?}
        (dissoc controls-props :seek :play?))])))

(defn- bounding-rect
  [node]
  (let [rect (.getBoundingClientRect node)]
    {:width (.-width rect)
     :height (.-height rect)
     :top (.-top rect)
     :left (.-left rect)}))
