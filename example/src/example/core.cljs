(ns example.core
  (:require [example.events]
            [example.subs]
            [example.views :as views]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:dev/after-load init []
  (re-frame/dispatch-sync [:initialize-db])
  (enable-console-print!)
  (mount-root))
