;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns reflecti.datasource)

(defprotocol DataSource
  (data [this] "Method to return the ratom holding some data."))

(defprotocol Sortable
  (dispatch-sort [this sort-key direction]
    "Method to sort the data based on the sort-key and the direction."))
