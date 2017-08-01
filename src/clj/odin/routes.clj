(ns odin.routes
  (:require [odin.routes.api :as api]
            [compojure.core :as compojure :refer [context defroutes GET]]
            [facade.core :as facade] [ring.util.response :as response]))

(defn- show-odin [req]
  (let [render (partial apply str)]
    (-> (facade/app req "odin"
                    :stylesheets [facade/font-awesome]
                    :css-bundles ["styles.css" "antd.css"])
        (render)
        (response/response)
        (response/content-type "text/html"))))

(defroutes routes
  (context "/api" [] api/routes)

  (compojure/routes (GET "*" [] show-odin)))