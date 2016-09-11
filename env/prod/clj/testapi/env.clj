(ns testapi.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[testapi started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[testapi has shut down successfully]=-"))
   :middleware identity})
