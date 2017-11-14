(ns nlptools.command
  (:require
   [integrant.core :as ig]
   [clojure.tools.reader.edn :as edn]
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   ))

(def default-config-filename ".nlptools.edn")

(defn set-config
  "Set the configs map using command line options and the configuration file.

   Args:
     options (map): command line options

   Returns:
     (map): the new config map."
  [options]
  (let [cfgfile (io/file (get options :config default-config-filename))]
    (if (.exists cfgfile)
      (merge (edn/read-string (slurp cfgfile)) options)
      options)))

(defn prep-igconfig [config]
  (doto config ig/load-namespaces))

(defn make-logger [{:keys [quiet]}]
  {
   :duct.logger/timbre {:level  (if quiet :error :info)
                        :set-root-config? true
                        :appenders {:duct.logger.timbre/brief (ig/ref :duct.logger.timbre/brief)}},
   :duct.logger.timbre/brief {:min-level (if quiet :error :info)}
   }
  )

(defn make-test-logger []
  {
   :duct.logger/timbre {:level :error
                        :set-root-config? true
                        :appenders {:duct.logger.timbre/brief (ig/ref :duct.logger.timbre/brief)}},
   :duct.logger.timbre/brief {:min-level :error}
   }
  )

(defmulti help
  "Returns the command description."
  {:arglists '([key])}
  identity)

(defmethod help :default [_] " ")

(defmulti syntax
  "Returns the command syntax."
  {:arglists '([key])}
  identity)

(defmethod syntax :default [_] " ")

(defmulti run
  "Run a command."
  {:arglists '([key config summary])}
 (fn [k _ _] k))




