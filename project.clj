;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(defproject com.7theta/reflecti "1.7.0"
  :description "A library of reagent components"
  :url "https://github.com7theta/reflecti"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[reagent "0.8.0"
                  :exclusions [cljsjs/react cljsjs/react-dom
                               cljsjs/react-dom-server]]
                 [cljsjs/react-dom "16.3.2-0"]
                 [cljsjs/react "16.3.2-0"]
                 [cljsjs/antd "3.5.0-0"]
                 [cljsjs/moment "2.22.0-0"]
                 [cljsjs/draft-js "0.10.5-0"]
                 [cljsjs/d3-cloud "1.2.1-0"]
                 [cljsjs/recharts "1.0.0-beta.10-3"]
                 [com.7theta/utilis "1.1.0"]
                 [inflections "0.13.0"]]
  :source-paths ["src/cljs"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :profiles {:dev {:source-paths ["dev" ]
                   :resource-paths ["example/resources"]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-figwheel "0.5.15"]]
                   :dependencies [[org.clojure/clojure "1.9.0"]
                                  [org.clojure/clojurescript "1.10.238"]

                                  [ring/ring-core "1.6.3" :exclusions [ring/ring-codec]]
                                  [ring/ring-defaults "0.3.1"]
                                  [ring/ring-anti-forgery "1.2.0"]

                                  [com.cemerick/piggieback "0.2.2"]
                                  [binaryage/devtools "0.9.10"]

                                  [integrant "0.6.3"]
                                  [integrant/repl "0.3.1"]

                                  [compojure "1.6.1"]
                                  [ns-tracker "0.3.1"]
                                  [figwheel-sidecar "0.5.15"]
                                  [re-frame "0.10.5"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "example/src/cljs"]
                        :figwheel {:on-jsload "example.core/mount-root"}
                        :compiler {:main example.core
                                   :output-to "example/resources/public/js/compiled/app.js"
                                   :output-dir "example/resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :preloads [devtools.preload]
                                   :external-config {:devtools/config {:features-to-install :all}}}}]}
  :scm {:name "git"
        :url "https://github.com/7theta/reflecti"})
