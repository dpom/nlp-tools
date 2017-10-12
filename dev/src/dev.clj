(ns dev
  (:refer-clojure :exclude [test])
  (:require
   [clojure.repl :refer :all]
   [nlptools.core :as nlp]
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.java.io :as io]
   [clojure.java.jdbc :as j]
   [integrant.core :as ig]
   [integrant.repl :refer [clear halt go init prep reset]]
   [integrant.repl.state :refer [config system]]))

(defn read-config []
  (ig/read-string (slurp (io/resource "dev.edn"))))

(defn dev-prep [config]
  (doto config ig/load-namespaces))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src")

;; (when (io/resource "local.clj")
;;   (load "local"))

(integrant.repl/set-prep! (comp dev-prep read-config))
