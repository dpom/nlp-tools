(ns nlptools.config
  (:require
  [integrant.core :as ig]
  [clojure.tools.reader.edn :as edn]
  [clojure.java.io :as io]
  [clojure.test :refer :all]
  [taoensso.timbre :as log]
  ))

(defn set-config
  "Set the configs map using command line options and the configuration file.

   Args:
     options (map): command line options
     filename (string): default config filename

   Returns:
     (map): the new config map."
  [options filename]
  (let [cfgfile (io/file (get options :config filename))]
    (if (.exists cfgfile)
      (merge (edn/read-string (slurp cfgfile)) options)
      options)))
