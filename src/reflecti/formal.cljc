(ns reflecti.formal
  (:refer-clojure :exclude [def])
  (:require [reflecti.formal.shared :refer [registry *spec-id*]]
            #?(:cljs [cljs.spec.alpha :as s])))

;;; API

#?(:clj
   (defmacro def
     [k spec-form]
     `(binding [*spec-id* ~k]
        (do (swap! registry assoc ~k ~spec-form)
            (s/def ~k ~spec-form)))))
