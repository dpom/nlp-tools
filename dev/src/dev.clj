(ns dev
  (:refer-clojure :exclude [test])
  (:require
   [clojure.repl :refer :all]
   [fipp.edn :refer [pprint] :rename {pprint fipp}]
   ;; [nlptools.core :as nlp]
   [nlptools.tool.core :as tool]
   [nlptools.model.core :as model]
   [nlptools.corpus.core :as corpus]
   [nlptools.command :as cmd]
   [clojure.tools.namespace.repl :refer [refresh]]
   [clojure.java.io :as io]
   [clojure.java.jdbc :as j]
   [duct.logger :as logger] 
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
