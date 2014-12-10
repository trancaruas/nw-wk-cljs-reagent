(ns nw-wk-cljs-reagent.handler
  (:require [nw-wk-cljs-reagent.dev :refer [browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.adapter.jetty :as ring]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]])
   (:gen-class :main true))

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))

(def server (ring/run-jetty #'app {:port 3000 :join? false}))

(defn start-server []
  (.start server))

(defn -main []
  (start-server))
