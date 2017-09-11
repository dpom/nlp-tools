(ns nlptools.corpus
  (:require
  [integrant.core :as ig]
  [clojure.java.io :as io]
  [clojure.string :as str]
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

(defn filter-row [row]
  (-> row
      :text))

(defn write-corpus! [filename, resultset]
  (with-open [w (clojure.java.io/writer filename)]
    (reduce  (fn [total row]
               (.write w row)
               (.newLine w)
               (.write w (str/join (repeat 50 "*")))
               (.newLine w)
               (inc total))
             0 resultset)))

(defn create-corpus! [system filename]
  (let [db (get system :nlptools/db)]
    (.query db ["select sheet_text as text from  sheets where subsidiary_id = 1"]
            filter-row
            (partial write-corpus! "corpus.txt"))))



(defn create-command
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
