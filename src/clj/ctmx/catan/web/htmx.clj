(ns ctmx.catan.web.htmx
  (:require
   [ctmx.render :as render]
   [ring.util.http-response :as http-response]
   [hiccup.core :as h]
   [hiccup.page :as p]))

(defn page [opts & content]
  (-> (p/html5 opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn ui [opts & content]
  (-> (h/html opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn page-htmx [& body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
    [:title "Settlers of Catan"]
    [:link {:rel "stylesheet"
            :href "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
            :integrity "sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2"
            :crossorigin "anonymous"}]]
   [:body (render/walk-attrs body)]
   [:script {:src "/htmx.min.js"}]
   [:script "htmx.config.defaultSettleDelay = 0;
htmx.config.defaultSwapStyle = 'outerHTML';"]))
