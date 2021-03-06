(ns reflecti.formal.spec
  (:require [reflecti.formal.shared :refer [registry *deferred* *spec-id*]]
            #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [clojure.string :as st]))

;;; API

(defn deferred
  ([] (deferred *spec-id*))
  ([k]
   (when (not k)
     (throw
      (ex-info
       "A key must be provided to the deferred spec"
       {:k k})))
   (fn deferred
     ([] (or (get *deferred* k) any?))
     ([x]
      (boolean
       (when-let [spec (deferred)]
         (s/valid? spec x)))))))

;;; Component Options

(defn cascade
  [values]
  (let [valid? (comp boolean (set values))]
    (with-meta
      (fn cascade
        ([] values)
        ([x] (valid? x)))
      {::key ::cascade})))

(defn select
  [values]
  (let [valid? (comp boolean (set values))]
    (with-meta
      (fn select
        ([] values)
        ([x] (valid? x)))
      {::key ::select})))

(defn auto-complete
  [values]
  (let [valid? (comp boolean
                  #(or (and (string? %)
                            (seq (st/trim %)))
                       (some? %)))]
    (with-meta
      (fn auto-complete
        ([] values)
        ([x] (valid? x)))
      {::key ::auto-complete})))

(defn string
  [pred]
  (fn [x]
    (s/valid? pred x)))
