(ns example.events
  (:require [example.db :as db]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :example/words-updated
 (fn [{:keys [db]} [_ words]]
   {:db (assoc db :words words)}))
