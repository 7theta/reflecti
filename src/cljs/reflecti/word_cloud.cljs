(ns reflecti.word-cloud
  (:require [cljsjs.d3-cloud]
            [reagent.core :refer [create-class props]]))

;;; Declarations

(def ^:private d3 js/d3)
(def ^:private d3-cloud (.. d3 -layout -cloud))

(def scale-linear (.scaleLinear d3))
(def scale-log (.scaleLog d3))
(def scale-sqrt (.scaleSqrt d3))

(def scale-lookup
  {:linear scale-linear
   :log scale-log
   :sqrt scale-sqrt})

(declare draw-cloud layout-cloud)

;;; API

(defn cloud
  []
  (let [svg-id (str "d3-cloud-" (gensym))
        default-width 500
        default-height 500
        default-props {:width default-width
                       :height default-height
                       :scale (partial * 25)
                       :scale-range [5 100]
                       :padding 5
                       :rotate (constantly 0)
                       :cloud-transform
                       (str "translate("
                            (/ default-width 2)
                            ","
                            (/ default-height 2)
                            ")")
                       :text-anchor "middle"
                       :text-anchor-transform
                       (fn [d]
                         (str "translate(" (.-x d) "," (.-y d) ") "
                              (when-let [rotate (.-rotate d)]
                                (str "rotate (" (.-rotate d) ")"))))}
        layout (partial layout-cloud svg-id default-props)]
    (create-class
     {:component-did-mount layout
      :component-did-update layout
      :reagent-render
      (fn [] [:svg {:id svg-id}])})))

;;; Private

(defn- draw-cloud
  [svg-id {:keys [width height font-family text-anchor cloud-transform
                  text-anchor-transform]} words]
  (when-let [svg (js/document.getElementById svg-id)]
    (while (.hasChildNodes svg)
      (.removeChild svg (.-lastChild svg))))
  (when-let [svg (.select d3 (str "#" svg-id))]
    (cond-> svg
      true (.attr "width" width)
      true (.attr "height" height)
      true (.append "g")
      cloud-transform (.attr "transform" (if (and width height)
                                           (str "translate("
                                                (/ width 2)
                                                ","
                                                (/ height 2)
                                                ")")
                                           cloud-transform))
      true (.selectAll "text")
      true (.data words)
      true (.enter)
      true (.append "text")
      true (.style "fill" "#666")
      true (.style "font-size" (fn [d] (str (.-size d) "px")))
      font-family (.style "font-family" font-family)
      text-anchor (.attr "text-anchor" text-anchor)
      text-anchor (.attr "transform" text-anchor-transform)
      true (.text (fn [d] (.-text d))))))

(defn- layout-cloud
  [svg-id default-props this]
  (let [{:keys [width height
                font-family
                words
                scale
                scale-range
                rotate
                padding] :as props} (merge default-props (props this))

        words (map (fn [[word occurrences]]
                     {:text word
                      :occurrences occurrences})
                   (if (map? words) words (frequencies words)))
        word-occurrences (map :occurrences words)
        scale-domain [(apply min word-occurrences)
                      (apply max word-occurrences)]
        scale (cond

                (keyword? scale)
                (when-let [f (scale-lookup scale)]
                  (-> f
                      (.domain (clj->js scale-domain))
                      (.range (clj->js scale-range))))

                (fn? scale) (scale scale-domain scale-range)

                :else nil)
        _ (when-not scale
            (throw
             (ex-info
              "The 'scale' prop must either be a fn or one of the keyword options"
              {:options (keys scale-lookup)})))

        words (->> words
                   (map (fn [{:keys [text occurrences]}]
                          #js {:text text
                               :size (scale occurrences)}))
                   (clj->js))

        layout (cond-> (d3-cloud)
                 true (.size #js [width height])
                 true (.words words)
                 true (.padding padding)
                 rotate (.rotate rotate)
                 font-family (.font font-family)
                 true (.fontSize (fn [d] (.-size d)))
                 true (.on "end" (partial draw-cloud svg-id props)))]
    (.start layout)))
