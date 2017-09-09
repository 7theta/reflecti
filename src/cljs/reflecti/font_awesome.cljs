(ns reflecti.font-awesome
  (:require [clojure.string :as st]))

(defn icon
  "An icon component that mimics the APi of the Ant Design icon
  component, but uses Font Awesome icons instead.

  Additional configuration of Font Awesome icons is possible
  by passing a seq of strings. Examples can be found at:
    http://fontawesome.io/examples/"
  [{:keys [type classes] :as props}]
  [:i (merge
       {:class (str "fa fa-" type (st/join " " classes))}
       (dissoc props :type :classes))])
