(ns nlptools.test
  "Test system specific functions"
  (:require
   [clojure.java.io :as io]
   [integrant.core :as ig]))


(defn prep [config]
  (doto config ig/load-namespaces))


(defn get-test-module
  [configfile ukey]
  (-> configfile
      io/file
      slurp
      ig/read-string
      prep
      ig/init
      ukey))
