(ns dev
  (:refer-clojure :exclude [test])
  (:require
   [clojure.repl :refer :all]
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.gen.alpha :as gen]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.java.jdbc :as j]
   [fipp.edn :refer [pprint] :rename {pprint fipp}]
   [integrant.core :as ig]
   [integrant.repl :refer [clear halt go init prep reset]]
   [integrant.repl.state :refer [config system]]
   [duct.logger :as logger] 
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.command :as cmd]
   ))

(defn read-config []
  (ig/read-string (slurp (io/resource "dev.edn"))))

(defn dev-prep [config]
  (doto config ig/load-namespaces))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src")

;; (when (io/resource "local.clj")
;;   (load "local"))

(integrant.repl/set-prep! (comp dev-prep read-config))
