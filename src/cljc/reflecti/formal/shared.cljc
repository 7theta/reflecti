(ns reflecti.formal.shared)

(def ^:dynamic *deferred* nil)
(def ^:dynamic *spec-id* nil)
(def registry (atom {}))
