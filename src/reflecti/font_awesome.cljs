(ns reflecti.font-awesome
  (:require [clojure.string :as st]))

(defn icon
  "An icon component that mimics the APi of the Ant Design icon
  component, but uses Font Awesome icons instead.

  Additional configuration of Font Awesome icons is possible
  by passing a seq of strings. Examples can be found at:
    http://fontawesome.io/examples/"
  [{:keys [name type classes] :or {type :regular} :as props}]
  {:pre [(#{:solid :regular :light :duotone :brands} type)]}
  [:i (merge
       {:class (str (->> type clojure.core/name first (str "fa"))
                    " fa-" name " " (st/join " " classes))}
       (dissoc props :type :name :classes))])
