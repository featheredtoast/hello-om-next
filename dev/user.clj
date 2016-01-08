(ns user
  (:require [hello2.server]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.repl-api :as figwheel]))


(def http-handler
  (wrap-reload hello2.server/http-handler))

(defn run []
  (figwheel/start-figwheel!))

(def browser-repl figwheel/cljs-repl)
