(ns nlptools.corpus
  (:require
  [integrant.core :as ig]
  [clojure.java.io :as io]
  [clojure.test :refer :all]
  [taoensso.timbre :as log]
))


(defn build-igconfig
  "Based on application options build the ig config.

  Args:
  options (map): the application options.

  Returns:
  (map): the ig config."
  [options]
  )

(defn create
  "Brief

  Args:
  options (map): the action options

  Returns:
  (vector): [ret msg] "
  [options ]
  (let [igconfig (build-igconfig options)
        system (ig/init igconfig) ]

    (ig/halt! system)
    [2 nil]
    ))
