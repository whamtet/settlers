(ns ctmx.catan.web.views.hello
    (:require
      [ctmx.core :as ctmx :refer [defcomponent]]
      [ctmx.catan.web.htmx :refer [page-htmx]]
      [ctmx.catan.web.views.board :as board]
      [ctmx.catan.web.views.menu :as menu]))

(defn ui-routes [base-path]
  (ctmx/make-routes
   base-path
   (fn [req]
     (page-htmx
      (if (-> req :session :game-name)
        (board/board req)
        (menu/menu req))))))
