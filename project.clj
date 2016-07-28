;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(defproject com.7theta/reflecti "0.2.1"
  :description "A component library based on React"
  :url "https://github.com7theta/reflecti"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.93"]

                 [com.7theta/utilis "0.8.0"]
                 [com.7theta/via "0.2.1"]

                 [cljs-react-material-ui "0.2.19"]
                 [reagent "0.6.0-alpha" :exclusions [org.clojure/tools.reader cljsjs/react]]
                 [re-frame "0.7.0"]

                 [cljsjs/d3 "3.5.16-0"]
                 [cljsjs/nvd3 "1.8.2-1"]

                 [com.andrewmcveigh/cljs-time "0.5.0-alpha1"]

                 [prismatic/schema "1.1.2"]]
  :source-paths ["src/cljs"]
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.4-7" :exclusions [cider/cider-nrepl
                                                  org.clojure/clojure]]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :profiles {:dev {:source-paths ["dev" "example/src"]
                   :resource-paths ["example/resources"]
                   :dependencies [[figwheel-sidecar "0.5.4-7"]
                                  [com.cemerick/piggieback "0.2.1"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "reflecti.example.core/mount-root"}
                        :warning-handlers [(fn [warning-type env extra]
                                             (when (warning-type cljs.analyzer/*cljs-warnings*)
                                               (when-let [s (cljs.analyzer/error-message warning-type extra)]
                                                 (binding [*out* *err*]
                                                   (println "WARNING:" (cljs.analyzer/message env s)))
                                                 (System/exit 1))))]
                        :compiler {:main reflecti.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :optimizations :none
                                   :pretty-print  true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main reflecti.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :scm {:name "git"
        :url "https://github.com/7theta/reflecti"})
