(ns nlptools.stemmer
  (:require
   [stemmer.snowball :as snowball]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.command :as cmd]
   ))

(def languages-codes {"ro" :romanian
                      "en" :english})

(defprotocol Stemmer
  (init [this  logger])
  (get-root [this text]))


(defrecord Boundary [stemmer lang logger]
  Stemmer
  (init [this newlogger]
    (reset! logger newlogger)
    (log @logger :info ::init-stemmer {:lang lang})
    (reset! stemmer (snowball/stemmer (get languages-codes lang :romanian)))
    this)
  (get-root [this text]
    (log @logger :debug ::get-root {:text text})
    (@stemmer text)))

(defmethod ig/init-key :nlptools/stemmer [_ spec]
  (let [{:keys [language logger]} spec]
    (.init (->Boundary (atom nil) language (atom nil)) logger)))

(defmethod cmd/help :stemmer [_]
  "stemmer - reduce inflected (or sometimes derived) words to their word stem ")

(defmethod cmd/syntax :stemmer [_]
  "nlptools stemmer -t TEXT")

(defmethod cmd/run :stemmer [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {:nlptools/stemmer {:language (:language opts)
                                          :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stemmer (:nlptools/stemmer system)
        word (get opts :text "")]
    (printf "word: %s,\nstem: %s\n" word (.get-root stemmer word))
    (ig/halt! system)
    0))
