(ns reflecti.formal
  (:require-macros [reflecti.formal :as f])
  (:require [reflecti.formal.shared :refer [registry *deferred*]]
            [spec-tools.core :as spt]
            [spec-tools.impl :as impl]
            [spec-tools.form :as form]
            [cljs.spec.alpha :as s]
            [spec-tools.visitor :as visitor]
            [inflections.core :as inflections]
            [utilis.types.string :refer [->string]]
            [utilis.types.number :refer [string->long]]
            [utilis.map :refer [compact map-keys map-vals filter-vals]]
            [utilis.fn :refer [fsafe]]
            [reagent.core :as r]
            [clojure.string :as st]))

(declare input* input-form-item

         change-watcher
         spec-id->map
         ratom?
         flatten-state
         ratoms
         drop-nth
         ->label
         resolve-deferred-spec)

;;; API

(defn input
  "
  Available props:
  :spec - The root spec provided to the renderer. This is the spec that you
          would like to be rendered as a series of inputs.
  :ui   - The style in which to render the inputs. #{:antd}

  You can then provide, as a key, any spec definition you wish to customize. As
  an example, if you have a spec defined as :my.spec/foo, then in this props map
  you can provide {:my.spec/foo { ... options } } where options are defined
  below.

  Available options on a provided spec:
  :disabled?        - Boolean - Is this input disabled?
  :hidden?          - Boolean - Is this input hidden?
  :reset-on-update? - Boolean - If this input is updated for any reason, should
                                it be reset? (e.g. its default value)
  :default-value    - Any/fn  - Default value for this input. Alternatively, you
                                can provide a function that will be given this
                                input's props which you can use to generate a
                                default value dynamically.
  :spec             - Any     - When deferring a spec, provide it here at
                                runtime.
  :spec-xform       - fn      - Will be called in lieu of :spec, if provided,
                                with the input's props as its only argument. The
                                expected return value is a spec.
  :on-add           - fn      - Called when the form has been submitted
  :help             - String  - Error message when spec does not validate on the
                                input.
  :delete?          - fn      - Given this input's props, can it be deleted if
                                part of a collection.
  :xform-children   - fn      - When provided to a collection, applied to the
                                children in the collection immediately before
                                layout (effectively allowing you to sort them).
  :add-button-label - Text    - When provided to a collection, the label for the
                                'Add Another Item' button.
  :style            - Map     - Some inputs accept various styles. API is still
                                being worked on.
  "
  []
  (let [form-state (r/atom nil)
        form-valid? (r/atom nil)]
    (fn [{:keys [spec ui default-value style]
         :or {ui :antd}
         :as props}]
      (let [this (r/current-component)]
        [:div
         {:key (str spec)
          :style style}
         (when-let [on-change (:on-change props)]
           [change-watcher
            {:on-change (comp on-change #(get % ((comp keyword name) spec)))
             :a form-state}])
         [input*
          (cond-> props
            true (dissoc :ui)
            true (assoc ::form-state form-state
                        ::form-valid? form-valid?
                        ::ui ui)
            (:default-value props) (update-in
                                    [spec :default-value]
                                    #(or % (:default-value props)))
            true (merge (spec-id->map spec)))]]))))

;;; Prop Utilities

(defn spec-id
  [props]
  (::spec-id props))

(defn spec-props
  [props]
  (get props (spec-id props)))

(defn disabled?
  [props]
  (boolean (:disabled? (spec-props props))))

(defn hidden?
  [props]
  (boolean (:hidden? (spec-props props))))

(defn default-value
  [props]
  (let [v (:default-value (spec-props props))]
    (if (fn? v)
      (v props)
      v)))

(defn style
  [props]
  (:style (spec-props props)))

(defn value
  [{::keys [form-state path spec-id]}]
  (when form-state
    (let [a (get-in @form-state path)
          value (if (ratom? a)
                  ((fsafe deref) a)
                  (flatten-state a))]
      (when (not (= value ::none)) value))))

(defn path
  [props]
  (if (= (::spec-id props) (last (::path props)))
    (::path props)
    (conj (vec (::path props)) (::spec-id props))))

(defn swap-value!
  [{::keys [form-state path] :as props} f]
  (when-let [a (get-in @form-state path)]
    (swap! a f)))

(defn reset-value!
  [props value]
  (swap-value! props (constantly value)))

(defn valid?
  [{::keys [spec spec-id] :as props}]
  (try (binding [*deferred* {spec-id (resolve-deferred-spec props)}]
         (s/valid? spec (value props)))
       (catch js/Error e
         (js/console.error e (str "Error evaluating spec: " spec-id))
         false)))

(defn has-value?
  [props]
  (let [value (value props)]
    (if (or (coll? value) (string? value))
      (boolean (seq value))
      (some? value))))

;;; Rendering

(defmulti spec->input
  (fn [{::keys [spec-key]}]
    (cond
      (keyword? spec-key) spec-key
      (coll? spec-key) (first spec-key)
      :else nil)))

;;; Standardize Components

(def div :div)
(def h4 :h4)
(def span :span)

;;; Custom UI Style Helpers

(defonce inputs (atom {}))

(defn definput
  [ui input-key component]
  (swap! inputs assoc-in [ui input-key] component))

(defn on-input-change
  ([props] (on-input-change props identity))
  ([props value-fn]
   (fn [& args]
     (->> args
          (apply value-fn)
          (reset-value! props)))))

(defn ui-component-for-key
  [ui input-key]
  (let [component (get-in @inputs [ui input-key])]
    (when-not component
      (js/console.warn
       "No component found for inputs"
       (clj->js
        {:ui ui
         :input-key input-key})))
    (or component
        [div "No component found for inputs"
         {:ui ui
          :input-key input-key}])))

;;; Private

(def ^:private int-in-range-regex
  #"\(\(clojure\.spec\.alpha/int-in-range\? (\d+) (\d+) \%\) \[\%\] clojure\.core/fn\)")

(defn- normalize-form-string
  [form-string]
  (-> form-string
      (st/replace #"cljs.spec.alpha/" "clojure.spec.alpha/")
      (st/replace #"cljs.core/" "clojure.core/")))

(defn- spec->keyword
  [spec]
  (cond
    (keyword? spec) spec
    (symbol? spec) (keyword (str spec))
    (coll? spec) spec
    :else nil))

(defn identify
  [spec spec-id children]
  (cond

    ;; spec/int-in
    (and (= :clojure.spec.alpha/and (spec->keyword spec))
         (= (count children) 2)
         (= (::spec-key (first children)) :clojure.core/int?)
         (= (::spec-key (second children)) :clojure.spec.alpha/int-in-range?))
    (when-let [found (->> (::spec-id (second children)) str
                          (normalize-form-string)
                          (re-find int-in-range-regex))]
      [:clojure.spec.alpha/int-in
       (string->long (second found))
       (dec (string->long (last found)))])

    :else nil))

(defn- spec-id->map
  [root-spec]
  (visitor/visit
   root-spec
   (fn [spec spec-id children context]
     (let [spec-key (spec->keyword spec)
           spec-key (if (or (not spec-key)
                            (#{:clojure.spec.alpha/and} spec-key))
                      (when-let [identified (identify spec spec-id children)]
                        (spec->keyword identified))
                      spec-key)]
       (compact
        {::children children
         ::spec-id spec-id
         ::spec-key spec-key
         ::spec (s/get-spec spec-id)})))))

(defn- input*
  []
  (let [xform-props (fn [props]
                      (-> props
                          (assoc ::path (path props))
                          (dissoc :key)))
        update-valid! (fn [props]
                        (let [props (xform-props props)]
                          (when (not (#{:clojure.spec.alpha/keys
                                        :spec-tools.visitor/vector-of} (::spec-key props)))
                            (if (or (::required? props) (has-value? props))
                              (swap! (::form-valid? props) assoc (path props) (valid? props))
                              (swap! (::form-valid? props) dissoc (path props))))))]
    (r/create-class
     {:get-initial-state
      (fn [this]
        (let [{::keys [spec-id spec spec-key children form-state] :as props} (r/props this)]
          (when (not (#{:clojure.spec.alpha/keys
                        :spec-tools.visitor/vector-of} spec-key))
            (let [a (r/atom
                     (if (and (map? props)
                              (contains? props spec-id)
                              (contains? (get props spec-id) :default-value))
                       (default-value props)
                       ::none))]
              (swap! form-state assoc-in (path props) a)
              {:a a}))))
      :component-will-update (fn [_ [_ new-props]] (update-valid! new-props))
      :component-did-mount (fn [this] (update-valid! (r/props this)))
      :reagent-render
      (fn [{::keys [spec-id] :as props}]
        (when (keyword? spec-id)
          (let [this (r/current-component)
                {:keys [a]} (r/state this)
                _ (when a @a)]
            (if (hidden? props)
              [div]
              [spec->input (xform-props props)]))))})))

(defn make-literal [a]
  (-> a
      (st/replace #"\"" "\\\"")
      (st/replace #"\$" "\\$")))

(defn extract-between [prefix suffix from-string]
  (let [pattern (str (make-literal prefix) "([\\s\\S]*?)" (make-literal suffix))]
    (st/join "" (map #(second %) (re-seq (re-pattern pattern) from-string)))))

(defn- reflecti-spec
  [spec]
  (:reflecti.formal.spec/key (meta spec)))

(defn- reflecti-spec?
  [spec]
  (boolean (reflecti-spec spec)))

(defn- normalize-spec-symbol
  "based on https://github.com/metosin/spec-tools/blob/master/src/spec_tools/visitor.cljc"
  [spec]
  (cond
    (or (s/spec? spec) (s/regex? spec) (keyword? spec))
    (let [form (s/form spec)]
      (if (not= form ::s/unknown)
        (if (seq? form)
          (impl/normalize-symbol (first form))
          (normalize-spec-symbol form))
        spec))
    (set? spec) ::set
    (seq? spec) (impl/normalize-symbol (first (impl/strip-fn-if-needed spec)))
    (symbol? spec) (impl/normalize-symbol spec)
    (reflecti-spec? spec) (reflecti-spec spec)
    :else (impl/normalize-symbol (form/resolve-form spec))))

;;; spec->input methods

(defmethod spec->input :default
  [{::keys [spec-id spec-key spec] :as props}]
  [div [span (str "Unknown spec: " spec-id " " spec-key)]])

(defn- default-keys-template
  []
  [:div
   [:div {:style
          {:display "flex"
           :flex-direction "row"
           :justify-content "flex-end"
           :width "100%"}}
    [:formal/delete-button]]
   [:div :formal/children]
   [:div [:formal/submit-button]]])

(defn- replace-template-symbols
  [props hiccup]
  (cond

    (and (vector? hiccup) (= :formal/delete-button (first hiccup)))
    (when (:delete? props)
      [(:formal/delete-button props) (merge props (second hiccup))])

    (and (vector? hiccup) (= :formal/submit-button (first hiccup)))
    (when (not (:hide-submit? props))
      [(:formal/submit-button props) (merge props (second hiccup))])

    (and (vector? hiccup) (get (:children-by-spec props) (first hiccup)))
    (do (swap! (:formal/replaced props) conj (first hiccup))
        (get (:children-by-spec props) (first hiccup)))

    (vector? hiccup)
    (->> hiccup
         (map (partial replace-template-symbols props))
         (into []))

    (map? hiccup) hiccup
    (string? hiccup) hiccup

    (and (= :formal/children hiccup)
         (:formal/children props))
    (:formal/children props)

    :else hiccup))

(defn- resolve-template
  [{::keys [spec-id] :as props}]
  (let [template (or (get-in props [spec-id :template]) default-keys-template)
        hiccup (cond
                 (fn? template) (template props)
                 (vector? template) template
                 :else (throw (ex-info "Template must be a react component or a hiccup vector" {:template template})))
        children (->> (r/current-component)
                      (r/children)
                      (into '()))
        children-by-spec (->> children
                              (map (fn [[_ props & _ :as child]]
                                     [(::spec-id props) child]))
                              (into {}))
        replaced (atom #{})
        props (assoc props
                     :formal/replaced replaced
                     :children-by-spec children-by-spec)]
    (replace-template-symbols
     (assoc
      props :formal/children
      (remove
       (fn [[_ {::keys [spec-id] :as props} & _ :as child]]
         (@replaced spec-id))
       children))
     (replace-template-symbols props hiccup))))

(defmethod spec->input :clojure.spec.alpha/keys
  [{::keys [spec-id spec path form-state children
            delete? on-delete hide-submit? ui] :as props}]
  (let [default-value (default-value props)
        props (if default-value
                (reduce
                 (fn [props {::keys [spec-id]}]
                   (let [default-value (get default-value (keyword (name spec-id)))]
                     (if (nil? default-value)
                       props
                       (update-in
                        props [spec-id :default-value]
                        #(or % default-value)))))
                 props
                 children)
                props)
        spec-form-args (->> (s/form spec) rest (partition 2 2))
        spec-required-keys (->> spec-form-args
                                (filter (fn [[opt _]] (#{:req-un :req} opt)))
                                (mapcat second)
                                set)
        required? (comp boolean spec-required-keys)
        flattened-state (flatten-state (get-in @form-state path))]
    (into
     [resolve-template
      (assoc props
             :submit? (boolean
                       (and
                        (not= (compact default-value)
                              (compact flattened-state))
                        (when-let [valid (not-empty @(::form-valid? props))]
                          (->> valid
                               (remove
                                (comp (partial = [spec-id])
                                   first))
                               vals
                               (every? true?)))))
             :on-submit (fn [] ((fsafe (:on-add props)) spec-id flattened-state))
             :hide-submit? (or (:hide-submit? (spec-props props)) hide-submit?)
             :delete? (and delete?
                           (or (not (contains? (get props spec-id) :delete?))
                               (let [delete? (get-in props [spec-id :delete?])]
                                 (if (fn? delete?)
                                   (delete? props)
                                   (boolean delete?)))))
             :on-delete (fsafe on-delete)
             :formal/submit-button (ui-component-for-key ui :formal/submit-button)
             :formal/delete-button (ui-component-for-key ui :formal/delete-button))]
     (->> children
          (map
           (fn [child]
             (let [props (merge
                          (dissoc props ::children)
                          child
                          {:key (str (::spec-id child))
                           ::hide-submit? true
                           ::required?
                           (required?
                            (::spec-id child))})]
               [input* props])))
          (remove nil?)
          doall))))

(defn- ->options
  [values]
  (if (every? #(= 1 (count %)) values)
    (mapv (fn [[v & _]] {:value v :label v}) values)
    (let [groups (group-by first values)]
      (mapv (fn [[prefix children]]
              {:value prefix
               :label prefix
               :children (->options (map rest children))})
            groups))))

(defn- spec->values
  [spec-id spec]
  (cond
    (set? (s/form spec)) (s/form spec)
    (coll? spec) spec
    (fn? spec) (spec)
    :else (when-let [f (get @registry spec-id)]
            (spec->values spec-id f))))

(defmethod spec->input :reflecti.formal.spec/cascade
  []
  (r/create-class
   {:component-will-mount
    (fn [this]
      (let [{::keys [spec-id spec path form-state] :as props} (r/props this)
            a (get-in @form-state path)]
        (when (and a (= ::none @a))
          (reset! a (first (spec->values spec-id spec))))))
    :component-will-update
    (fn [this [_ new-props]]
      (let [{::keys [spec-id spec] :as props} new-props]
        (when (not (valid? props))
          (->> spec
               (spec->values spec-id)
               first
               (reset-value! props)))))
    :reagent-render
    (fn [{::keys [spec spec-id ui required?] :as props}]
      (let [values (spec->values spec-id spec)]
        [input-form-item
         (merge props
                {::component (ui-component-for-key ui :formal/cascade)
                 ::input-props
                 {:placeholder (->label spec-id)
                  :label (->label spec-id)
                  :options (->options values)
                  :default-value (first values)
                  :disabled? (disabled? props)
                  :value (value props)
                  :required? required?
                  :on-change (on-input-change props)
                  :style (style props)}})]))}))

(defmethod spec->input :clojure.core/set
  [props]
  [spec->input (assoc props ::spec-key :spec-tools.visitor/set)])

(defmethod spec->input :spec-tools.visitor/set
  [props]
  [spec->input (assoc props ::spec-key :reflecti.formal.spec/select)])

(defmethod spec->input :reflecti.formal.spec/select
  []
  (let [->option-str (fn [option]
                       (->string
                        (cond
                          (string? option) option
                          (keyword? option) (->string option)
                          (:value option) (:value option)
                          :else (->string option))))
        props->options (fn [{::keys [spec spec-id] :as props}]
                         (let [values (spec->values spec-id spec)]
                           (map
                            (fn [option]
                              (let [option-str (->option-str option)]
                                {:option-str option-str
                                 :option option}))
                            values)))
        maybe-update-value! (fn [this]
                              (let [{::keys [path form-state] :as props} (r/props this)
                                    a (get-in @form-state path)]
                                (let [options (props->options props)]
                                  (when (and a (= ::none @a) (seq options))
                                    (reset-value! props (:option (first options)))))))]
    (r/create-class
     {:component-will-mount maybe-update-value!
      :component-did-update maybe-update-value!
      :reagent-render
      (fn [{::keys [spec-id ui required?] :as props}]
        (let [options (props->options props)]
          [input-form-item
           (merge props
                  {::component (ui-component-for-key ui :formal/select)
                   ::input-props
                   {:placeholder (->label spec-id)
                    :value (->option-str (value props))
                    :options options
                    :label (->label spec-id)
                    :disabled? (disabled? props)
                    :required? required?
                    :default-value (value props)
                    :on-change (on-input-change props (fn [idx] (:option (nth (vec options) idx nil))))
                    :style (style props)}})]))})))

(defmethod spec->input :reflecti.formal.spec/auto-complete
  []
  (let [->option-str (fn [option]
                       (->string
                        (cond
                          (string? option) option
                          (keyword? option) (->string option)
                          (:value option) (:value option)
                          :else (->string option))))
        prep-option (fn [option]
                      (let [option-str (->option-str option)]
                        {:option-str option-str
                         :option option}))
        props->options (fn [{::keys [spec spec-id] :as props}]
                         (->> spec
                              (spec->values spec-id)
                              (map prep-option)))]
    (fn [{::keys [spec-id ui required?] :as props}]
      (let [options (props->options props)
            this (r/current-component)
            {:keys [blur?] :or {blur? false}} (r/state this)]
        [input-form-item
         (merge props
                {::component (ui-component-for-key ui :formal/auto-complete)
                 ::validate? (or (has-value? props) blur?)
                 ::input-props
                 {:placeholder (->label spec-id)
                  :value (->option-str (value props))
                  :options options
                  :label (->label spec-id)
                  :disabled? (disabled? props)
                  :required? required?
                  :on-focus (fn [] (r/set-state this {:blur? false}))
                  :on-blur (fn [] (r/set-state this {:blur? true}))
                  :default-value (value props)
                  :on-change (on-input-change
                              props (fn [value]
                                      (if (number? value)
                                        (:option (nth (vec options) value nil))
                                        (:option value))))
                  :style (style props)}})]))))

(defmethod spec->input :reflecti.formal/set
  [props]
  [spec->input (assoc props ::spec-key :spec-tools.visitor/set)])

(defmethod spec->input :reflecti.formal.spec/deferred
  [{::keys [spec-id deferred] :as props}]
  (let [spec (resolve-deferred-spec props)
        spec-keyword (spec->keyword (normalize-spec-symbol spec))]
    (if (= spec-keyword :reflecti.formal.spec/deferred)
      (let [msg (str "Recursive deferred value detected [spec-id='" spec-id "']."
                     " Deferred values must be realized at render time.")]
        (js/console.warn msg)
        [div [span msg]])
      [spec->input
       (compact
        (assoc props
               ::spec spec
               ::spec-key spec-keyword))])))

(defmethod spec->input :spec-tools.visitor/vector-of
  []
  (r/create-class
   {:get-initial-state
    (fn [this]
      {::values (default-value (r/props this))})
    :reagent-render
    (fn [{::keys [spec spec-id children form-state path ui] :as props}]
      (let [this (r/current-component)
            values (vec (::values (r/state this)))
            xform-children (or (get-in props [spec-id :xform-children]) identity)
            {:keys [min-count max-count]
             :or {min-count 0
                  max-count js/Number.MAX_SAFE_INTEGER}} (->> (s/form spec)
                                                              (partition 2 2)
                                                              (filter (comp #{:min-count :max-count} first))
                                                              (mapv vec)
                                                              (into {}))
            component (ui-component-for-key ui :formal/collection)]
        [div
         [div
          {:style {:display "flex"
                   :flex-direction "row"}}
          [h4 {:style (:header (style props))}
           (st/capitalize (name spec-id))]
          [div
           (when (< (count values) min-count)
             [span {:style {:margin-left 6
                            :color "#f5222d"}}
              (str "(At least " min-count " required)")])]]
         [div
          (into
           [component
            {:on-add (fn [] (r/set-state this {::values (conj values {::id (str (gensym))})}))
             :add-button-disabled? (>= (count values) max-count)
             :add-button-label (or (get-in props [spec-id :add-button-label])
                                   (->> (name spec-id)
                                        (inflections/singular)
                                        (st/capitalize)
                                        (str "Add ")))}]
           (->> values
                (map-indexed
                 (fn [idx {::keys [id]}]
                   [input*
                    (merge
                     (dissoc props ::children)
                     (first children)
                     {:key (str "value/" id)
                      ::count (count values)
                      ::hide-submit? true
                      ::delete? true
                      ::on-delete (fn []
                                    (swap! form-state update-in path
                                           #(->> (dissoc % idx)
                                                 (sort-by first)
                                                 (map-indexed
                                                  (fn [idx [_ value]]
                                                    [idx value]))
                                                 (into {})))
                                    (r/set-state this {::values (drop-nth idx values)}))
                      ::path (conj (vec (::path props)) idx)})]))
                doall
                xform-children))]]))}))

(defmethod spec->input :reflecti.formal.spec/string
  [props]
  [spec->input (assoc props ::spec-key :clojure.core/string?)])

(defmethod spec->input :clojure.core/string?
  [{::keys [spec-id required? ui] :as props}]
  (let [this (r/current-component)
        {:keys [blur?] :or {blur? false}} (r/state this)]
    [input-form-item
     (merge props
            {::component (ui-component-for-key ui :formal/text-input)
             ::validate? (or (has-value? props) blur?)
             ::input-props
             {:placeholder (->label spec-id)
              :label (->label spec-id)
              :disabled? (disabled? props)
              :required? required?
              :default-value (value props)
              :on-focus (fn [] (r/set-state this {:blur? false}))
              :on-blur (fn [] (r/set-state this {:blur? true}))
              :on-change (on-input-change props #(if (empty? %) ::none %))
              :style (style props)}})]))

(defmethod spec->input :clojure.core/integer?
  [{::keys [spec spec-id min-value max-value ui required?] :as props}]
  (let [this (r/current-component)
        {:keys [blur?] :or {blur? false}} (r/state this)]
    [input-form-item
     (merge props
            {::component (ui-component-for-key ui :formal/integer-input)
             ::validate? (or (has-value? props) blur?)
             ::input-props
             {:placeholder (->label spec-id)
              :label (->label spec-id)
              :disabled? (disabled? props)
              :required? required?
              :default-value (value props)
              :on-focus (fn [] (r/set-state this {:blur? false}))
              :on-blur (fn [] (r/set-state this {:blur? true}))
              :on-change (on-input-change props)
              :style (style props)}})]))

(defmethod spec->input :clojure.core/boolean?
  []
  (let [maybe-update-value! (fn [this]
                              (let [{::keys [path form-state] :as props} (r/props this)
                                    a (get-in @form-state path)]
                                (when (and a (= ::none @a))
                                  (reset-value! props false))))]
    (r/create-class
     {:component-will-mount maybe-update-value!
      :reagent-render
      (fn [{::keys [spec spec-id ui required?] :as props}]
        [input-form-item
         (merge props
                {::component (ui-component-for-key ui :formal/boolean-input)
                 ::validate? (has-value? props)
                 ::input-props
                 {:placeholder (->label spec-id)
                  :label (->label spec-id)
                  :disabled? (disabled? props)
                  :required? required?
                  :default-value (value props)
                  :on-change (on-input-change props)
                  :style (style props)}})])})))

(defmethod spec->input :clojure.spec.alpha/int-in
  [{::keys [spec-key] :as props}]
  [spec->input
   (assoc props
          ::spec-key :clojure.core/integer?
          ::min-value (second spec-key)
          ::max-value (last spec-key))])

;;; Private

(defn- ->label
  [s]
  (when (or (keyword? s) (string? s))
    (->> (st/split (name s) #"[- ]")
         (filter seq)
         (map st/capitalize)
         (st/join " "))))

(defn- input-form-item
  []
  (let [last-path (atom nil)]
    (r/create-class
     {:component-will-update
      (fn [this [_ new-props]]
        (let [spec-props (spec-props new-props)
              new-path (path new-props)]
          (when (or (:reset-on-update? spec-props)
                    (and (:reset-on-path-change? spec-props)
                         @last-path
                         (not= @last-path new-path)))
            (reset-value! new-props (default-value new-props))
            (reset! last-path new-path))))
      :reagent-render
      (fn [{::keys [spec spec-id required? path form-state validate? component input-props] :as props}]
        (when (not (hidden? props))
          (let [has-value? (has-value? props)
                valid? (valid? props)]
            [component
             (merge
              input-props
              (select-keys (get props spec-id) [:label :form-item-props])
              {:invalid-input? (boolean (and validate? has-value? (false? valid?)))
               :invalid-input-message (or (:help (spec-props props)) "Enter a valid input")
               :missing-input? (boolean (and validate? required? (not has-value?)))
               :missing-input-message "This is a required input"})])))})))

(defn- drop-nth
  [n coll]
  (->> coll
       (keep-indexed #(if (not= %1 n) %2))
       (into (empty coll))))

(def ratom-type (type (r/atom nil)))

(defn- ratom?
  [x]
  (= (type x) ratom-type))

(defn- flatten-state
  [x]
  (cond
    (map? x)
    (let [m (->> x
                 (map-vals flatten-state)
                 (filter-vals (partial not= ::none)))]
      (if (every? integer? (keys m))
        (->> m
             (sort-by first)
             (mapv (comp first vals second)))
        (map-keys (comp keyword name) m)))

    (coll? x) (into (empty x) (map flatten-state x))
    (ratom? x) @x
    :else x))

(defn- ratoms
  [m]
  (cond

    (map? m)
    (->> m vals ratoms flatten)

    (coll? m)
    (->> m
         (map ratoms)
         flatten
         (remove nil?))

    (ratom? m) m

    :else nil))

(defn- change-watcher
  []
  (let [last-value (atom ::none)]
    (r/create-class
     {:component-did-update
      (fn [this]
        (let [{:keys [a on-change]} (r/props this)
              value (flatten-state @a)]
          (when (not= value @last-value)
            ((fsafe on-change) value))
          (reset! last-value value)))
      :reagent-render
      (fn [{:keys [a]}]
        (let [_ (doseq [a (cons a (ratoms @a))] (deref a))]
          [div]))})))

(defn- resolve-deferred-spec
  [{::keys [spec-id] :as props}]
  (when-let [spec-xform (or (get-in props [spec-id :spec-xform])
                            (when-let [spec (get-in props [spec-id :spec])]
                              (constantly spec)))]
    (spec-xform props)))
