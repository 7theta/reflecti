;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(defproject com.7theta/reflecti "2.0.0"
  :description "A library of reagent components"
  :url "https://github.com7theta/reflecti"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[reagent "0.8.1"
                  :exclusions [cljsjs/react cljsjs/react-dom
                               cljsjs/react-dom-server]]
                 [com.7theta/utilis "1.2.0"]
                 [metosin/spec-tools "0.9.1"]
                 [inflections "0.13.2"]]
  :profiles {:dev {:source-paths ["dev" "example/src"]
                   :resource-paths ["example/resources"]
                   :dependencies [[org.clojure/clojure "1.10.0"]
                                  [org.clojure/clojurescript "1.10.520"]
                                  [com.google.javascript/closure-compiler-unshaded "v20190325"]
                                  [org.clojure/google-closure-library "0.0-20190213-2033d5d9"]

                                  [ring/ring-core "1.7.1" :exclusions [ring/ring-codec]]
                                  [ring/ring-defaults "0.3.2"]
                                  [ring/ring-anti-forgery "1.3.0"]

                                  [integrant "0.7.0"]
                                  [integrant/repl "0.3.1"]

                                  [compojure "1.6.1"]
                                  [re-frame "0.10.6"]

                                  [binaryage/devtools "0.9.10"]
                                  [thheller/shadow-cljs "2.8.36"]]}}
  :scm {:name "git"
        :url "https://github.com/7theta/reflecti"})
