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
            [reflecti.video :as video]
            ["antd" :as ant]
            [reflecti.font-awesome :as fa]
            [utilis.map :refer [deep-merge compact]]
            [utilis.fn :refer [fsafe]]
            [applied-science.js-interop :as j]
            [reagent.core :as r :refer [adapt-react-class]]
            ["draft-js" :as draft-js]
            [immutable :as immutable]
            [clojure.string :as st]))

;;; Declarations

(def ^:private djs-editor (adapt-react-class draft-js/Editor))

(declare toolbar block-renderer handle-return)

;;; API

(defn editor
  [{:keys [state on-change disabled placeholder style] :as props}]
  (let [on-change (fsafe on-change)]
    [:div (when-not disabled {:class-name "editor-root"})
     [djs-editor
      (merge
       {:editor-state state
        :placeholder placeholder
        :on-change on-change
        :read-only disabled
        :spell-check true
        :handle-key-command
        (fn [command]
          (if-let [new-state (.handleKeyCommand draft-js/RichUtils state command)]
            (do (on-change new-state) "handled")
            "not-handled"))
        :style style}
       (dissoc props :state :on-change :disabled :placeholder :style))]]))

(defn rich-editor
  []
  (let [editor-container-id (str "editor-container-" (gensym))]
    (fn [{:keys [state on-change disabled placeholder style error media-base-url
                media-disabled]
         :or {media-base-url "/media"}
         :as props}]
      (let [on-change (fsafe on-change)]
        [:div {:style (merge
                       {:border "1px solid"
                        :border-color (if error "#f5222d" "#d9d9d9")
                        :border-radius "4px"
                        :width "100%"}
                       style)}
         [:div
          {:id editor-container-id
           :style {:min-height 200
                   :position "relative"}}
          [editor
           (merge
            {:on-change on-change
             :block-renderer-fn (partial block-renderer state on-change)
             :handle-return (partial handle-return state on-change)}
            (dissoc props
                    :on-change
                    :disabled
                    :placeholder
                    :media-base-url
                    :style
                    :error))]
          [toolbar
           {:editor-container-id editor-container-id
            :editor-state state
            :media-disabled media-disabled
            :media-base-url media-base-url
            :on-change on-change}]]]))))

(defn state
  [contents]
  (if contents
    (.createWithContent draft-js/EditorState contents)
    (.createEmpty draft-js/EditorState)))

(defn set-editor-state
  [editor-state state]
  (draft-js/EditorState.set editor-state (clj->js state)))

(defn selection
  [root]
  (cond
    (j/get root :getSelection) (j/call root :getSelection)

    (j/get-in root [:document :getSelection])
    (j/call-in root [:document :getSelection])

    (j/get-in root [:document :selection])
    (j/get (j/call-in root [:document :selection :createRange]) :text)

    :else nil))

(defn text-state
  [text]
  (state (.createFromText draft-js/ContentState text)))

(defn deserialize
  [text]
  (state (when text (draft-js/convertFromRaw (js/JSON.parse text)))))

(defn serialize
  [state]
  (js/JSON.stringify (draft-js/convertToRaw (.getCurrentContent state))))

(defn apply-style
  [contents style]
  (.toggleInlineStyle draft-js/RichUtils contents style))

(defn empty?
  [state]
  (not (.hasText (.getCurrentContent state))))

;;; Private

(defn ->clj-map
  [m]
  (let [m (js->clj m :keywordize-keys true)]
    (if (or (map? m) (not (j/get m :keys)))
      m
      (js->clj
       (j/call m :toObject)
       :keywordize-keys true))))

(defn ->immutable-map
  [m]
  (let [js-map (clj->js m)]
    (immutable/Map. js-map)))

(defn selection-rect
  [selected]
  (let [rect (-> selected
                 (j/call :getRangeAt 0)
                 (j/call :getBoundingClientRect))]
    (or (if (and rect (j/get rect :top))
          rect
          (-> selected
              (j/call :getRangeAt 0)
              (j/call :getClientRects)
              (j/get 0)))
        (when-let [rect (and (j/get selected :anchorNode)
                             (j/call-in selected [:anchorNode :getBoundingClientRect]))]
          (j/assoc! rect :isEmptyLine true)))))

(defn force-selection
  [editor-state selection]
  (draft-js/EditorState.forceSelection editor-state selection))

(defn join-uris
  [& uris]
  (str "/" (->> uris
                (mapcat #(st/split % #"/"))
                (filter seq)
                (st/join "/"))))

(def upload-dragger (r/adapt-react-class ant/Upload.Dragger))

(defn upload-media
  [{:keys [base-url on-upload]}]
  (let [this (r/current-component)
        {:keys [file-list] :or {file-list []}} (r/state this)]
    [upload-dragger
     (merge
      {:multiple false
       :style {:padding-top 25
               :padding-bottom 25}
       :action base-url
       :on-change (fn [args]
                    (let [arg #(js->clj (j/get args %) :keywordize-keys true)
                          file (arg "file")
                          done? (= "done" (:status file))]
                      (r/set-state this {:file-list (if done? [] (j/get args :fileList))})
                      (when done?
                        ((fsafe on-upload) (compact
                                            {:base-url base-url
                                             :content-type (:type file)
                                             :filename (:response file)})))))
       :file-list (clj->js file-list)
       :accept "video/mp4,video/ogg,video/webm,image/jpeg,image/png"})
     [:div
      [:p {:class-name "ant-upload-drag-icon"}
       [antd/icon {:type "upload"}]]
      [:p {:class-name "ant-upload-text"}
       "Click or drag an image or video file to this area to upload."]
      [:p {:class-name "ant-upload-hint"}
       "Supported formats are jpeg, png, mp4, ogg, webm."]]]))

(defn video
  [{:keys [filename base-url] :as props}]
  [video/player {:url (str (join-uris base-url filename))}])

(defn image
  [{:keys [filename base-url] :as props}]
  [:img {:src (str (join-uris base-url filename))}])

(defn media
  [{:keys [replace-data merge-data data] :as props}]
  (let [{:keys [filename content-type base-url] :as data} (data)
        this (r/current-component)
        replace-data (fn [data]
                       (replace-data data)
                       (r/force-update this true))]
    [:div
     (if filename
       [:div
        (condp = (first (st/split (str content-type) #"/"))
          "video" [video data]
          "image" [image data]
          (throw (ex-info "Unrecognized content type" data)))]
       [upload-media
        {:base-url base-url
         :on-upload replace-data}])]))

(defn media-content-block
  [props]
  (let [block (j/get props :block)
        editor-state (j/get-in props [:blockProps :editor-state])
        on-change (j/get-in props [:blockProps :on-change])]
    (r/as-element
     [media
      {:replace-data #(let [content-state (j/call editor-state :getCurrentContent)
                            selection-state (draft-js/SelectionState.createEmpty (j/call block :getKey))
                            new-content-state (draft-js/Modifier.setBlockData content-state selection-state (->immutable-map %))]
                        (on-change (draft-js/EditorState.push editor-state new-content-state)))
       :data #(-> block (j/call :getData) (->clj-map))}])))

(defn block-renderer
  [editor-state on-change content-block]
  (clj->js
   (condp = (.getType content-block)
     "media" {:component media-content-block
              :props {:editor-state editor-state
                      :on-change on-change}}
     nil)))

(defn toolbar-button
  [{:keys [icon on-click style]}]
  [antd/button
   {:on-click (fn [e]
                (on-click e)
                (-> e
                    (j/get :currentTarget)
                    (j/call :blur)))
    :style (merge {:border 0
                   :background-color "transparent"
                   :color "#FFF"} style)}
   [fa/icon {:type :light :name icon}]])

(defn current-block
  [editor-state]
  (let [selection-state (selection editor-state)
        content-state (j/call editor-state :getCurrentContent)]
    (j/call content-state :getBlockForKey (j/call selection-state :getStartKey))))

(defn anchor-block
  [editor-state]
  (let [selection-state (selection editor-state)
        content-state (j/call editor-state :getCurrentContent)]
    (j/call content-state :getBlockForKey (j/get selection-state :anchorKey))))

(defn add-block-at
  [editor-state pivot-block-key block-type initial-data]
  (let [content (j/call editor-state :getCurrentContent)
        block-map (j/call content :getBlockMap)
        block (j/call block-map :get pivot-block-key)]
    (when (not block) (throw (js/Error. (str "Pivot key '" pivot-block-key "' is not present in block map."))))
    (let [blocks-before (-> block-map
                            (j/call :toSeq)
                            (j/call :takeUntil (partial = block)))
          blocks-after (-> block-map
                           (j/call :toSeq)
                           (j/call :skipUntil (partial = block))
                           (j/call :rest))
          new-block-key (draft-js/genKey)
          new-block (draft-js/ContentBlock.
                     (clj->js
                      {:key new-block-key
                       :type block-type
                       :text ""
                       :depth 0
                       :data (->immutable-map initial-data)}))
          new-block-map (-> blocks-before
                            (j/call :concat
                                    (clj->js
                                     [[pivot-block-key block]
                                      [new-block-key new-block]])
                                    blocks-after)
                            (j/call :toOrderedMap))
          selection (selection editor-state)
          new-content (j/call content :merge (clj->js {:blockMap new-block-map}))]
      (-> editor-state
          (draft-js/EditorState.push new-content "split-block")
          (force-selection (draft-js/SelectionState.
                            (clj->js
                             {:anchorKey new-block-key
                              :anchorOffset 0
                              :focusKey new-block-key
                              :focusOffset 0
                              :isBackward false})))))))

(defn add-block
  ([editor-state] (add-block editor-state "unstyled"))
  ([editor-state block-type] (add-block editor-state block-type {}))
  ([editor-state block-type initial-data]
   (add-block-at editor-state (j/call (selection editor-state) :getAnchorKey) block-type initial-data)))

(defn selected-block-node
  [root]
  (let [selection (selection root)]
    (when (not (zero? (j/get selection :rangeCount)))
      (loop [node (j/get (j/call selection :getRangeAt 0) :startContainer)]
        (cond

          (not node) nil

          (and (j/get node :getAttribute)
               (= (j/call node :getAttribute "data-block") "true"))
          node

          :else (recur (j/get node :parentNode)))))))

(defn toolbar
  []
  (let [inline-toolbar-id (str "toolbar-" (gensym))
        side-button-id (str "sidebutton-" (gensym))
        updated? (r/atom false)]
    (r/create-class
     {:component-did-update
      (fn [this]
        (let [{:keys [editor-state editor-container-id]} (r/props this)]
          (when editor-state
            (let [selection-state (selection editor-state)
                  native-selection (selection js/window)]

              (reset! updated? true)

              ;; side button
              (when (j/call selection-state :isCollapsed)
                (when-let [side-button-node (js/document.getElementById side-button-id)]
                  (let [side-button-boundary (j/call side-button-node :getBoundingClientRect)
                        width (j/get side-button-boundary :width)]
                    (when native-selection
                      (when-let [selected-block-node (selected-block-node js/window)]
                        (let [cursor-y (- (j/get selected-block-node :offsetTop) 6)
                              selection-boundary (selection-rect native-selection)
                              parent-node (js/document.getElementById editor-container-id)
                              parent-boundary (j/call parent-node :getBoundingClientRect)]
                          (j/assoc-in! side-button-node [:style :top] (str cursor-y "px")))))
                    (j/assoc-in! side-button-node [:style :left] (str (- (+ width 8)) "px")))))

              ;; inline toolbar
              (when (not (j/call selection-state :isCollapsed))
                (when (and (j/get native-selection :rangeCount)
                           (pos? (j/get native-selection :rangeCount)))
                  (when-let [toolbar-node (js/document.getElementById inline-toolbar-id)]
                    (let [selection-boundary (selection-rect native-selection)
                          toolbar-boundary (j/call toolbar-node :getBoundingClientRect)
                          parent-node (js/document.getElementById editor-container-id)
                          parent-boundary (j/call parent-node :getBoundingClientRect)]
                      (j/assoc-in!
                       toolbar-node [:style :top]
                       (str (- (j/get selection-boundary :top)
                               (j/get parent-boundary :top)
                               (j/get toolbar-boundary :height)
                               5) "px"))
                      (let [selection-center (- (+ (j/get selection-boundary :left)
                                                   (/ (j/get selection-boundary :width) 2))
                                                (j/get parent-boundary :left))
                            left (- selection-center (/ (j/get toolbar-boundary :width) 2))
                            screen-left (+ (j/get parent-boundary :left) left)
                            left (if (neg? screen-left) (- (j/get parent-boundary :left)) left)]
                        (j/assoc-in! toolbar-node [:style :left] (str left "px")))))))))))
      :reagent-render
      (fn [{:keys [editor-state media-disabled media-base-url on-change]}]
        (let [apply-text-style (fn [style event]
                                 (.preventDefault event)
                                 ((fsafe on-change) (apply-style editor-state style)))
              selection-state (selection editor-state)
              anchor-block (anchor-block editor-state)]

          [:div
           (when-not media-disabled
             (when (and @updated? (j/call selection-state :isCollapsed)
                        anchor-block (zero? (j/call anchor-block :getLength)))
               [:div {:id side-button-id
                      :style {:position "absolute"
                              :background-color "transparent"
                              :border-radius 5}}
                [toolbar-button
                 {:icon "paperclip"
                  :style {:color "#323845"
                          :box-shadow "none"}
                  :on-click (fn [] ((fsafe on-change) (add-block editor-state "media" {:base-url media-base-url})))}]]))

           (when (not (j/call selection-state :isCollapsed))
             [:div {:id inline-toolbar-id
                    :style {:position "absolute"
                            :background-color "#323845"
                            :border-radius 5
                            :color "#FFF"
                            :box-shadow "0 1px 3px 0 #747171"
                            :display "flex"}}
              [toolbar-button
               {:icon "bold"
                :on-click (partial apply-text-style "BOLD")}]
              [toolbar-button
               {:icon "italic"
                :on-click (partial apply-text-style "ITALIC")}]
              [toolbar-button
               {:icon "underline"
                :on-click (partial apply-text-style "UNDERLINE")}]
              [toolbar-button
               {:icon "strikethrough"
                :on-click (partial apply-text-style "STRIKETHROUGH")}]])]))})))

(defn handle-return
  [editor-state on-change e]
  (let [current-block (current-block editor-state)]
    (condp = (j/call current-block :getType)
      "media" (do (on-change (add-block editor-state)) "handled")
      "unhandled")))
