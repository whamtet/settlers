(ns ctmx.catan.web.views.hello
    (:require
      [ctmx.core :as ctmx :refer [defcomponent]]
      [ctmx.catan.web.htmx :refer [page-htmx]]
      [ctmx.catan.web.views.board :as board]))

(defn ui-routes [base-path]
  (ctmx/make-routes
   base-path
   (fn [req]
     (page-htmx
      (board/board req)))))
