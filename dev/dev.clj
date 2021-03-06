;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns dev
  (:require [compojure.core :as compojure :refer [GET POST]]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.defaults :as ring-defaults]

            [integrant.core :as ig]

            [integrant.repl :refer [clear go halt init reset reset-all]]
            [integrant.repl.state :refer [system]]

            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]

            [clojure.tools.namespace.repl :refer [refresh refresh-all disable-reload!]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.test :refer [run-tests run-all-tests]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]))

;; (defmethod ig/init-key :example/ring-handler [_ _]
;;   (-> (compojure/routes
;;        (GET "/" req-req (response/content-type
;;                          (response/resource-response "public/index.html")
;;                          "text/html"))
;;        (route/resources "/"))
;;       (ring-defaults/wrap-defaults ring-defaults/site-defaults)))

;; (disable-reload! (find-ns 'integrant.core))

;; (def dev-config
;;   {:figwheel {:ring-handler (ig/ref :example/ring-handler)}
;;    :example/ring-handler {}})

;; (defmethod ig/init-key :figwheel [_ {:keys [ring-handler]}]
;;   (component/start
;;    (component/system-map
;;     :figwheel-system (-> (fig/fetch-config)
;;                          (assoc-in [:data :figwheel-options :ring-handler] ring-handler)
;;                          fig/figwheel-system)
;;     :css-watcher (fig/css-watcher
;;                   {:watch-paths ["resources/public/css"]}))))

;; (defmethod ig/halt-key! :figwheel [_ figwheel-system]
;;   (component/stop figwheel-system))

;; (ig/load-namespaces dev-config)

;; (integrant.repl/set-prep! (constantly dev-config))

;; (defn cljs-repl
;;   "Initializes and starts the cljs REPL"
;;   []
;;   (fig/cljs-repl (get-in system [:figwheel :figwheel-system])))
