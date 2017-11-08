(ns nlptools.tool.stemmer
  (:require
   [stemmer.snowball :as snowball]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool]]
   [nlptools.command :as cmd]))

(def languages-codes {"ro" :romanian
                      "en" :english})

(defprotocol Stemmer
  (init [this  logger])
  (get-root [this text]))


(defrecord StemmerTool [stemmer lang logger]
  Stemmer
  (init [this newlogger]
    (reset! logger newlogger)
    (log @logger :info ::init-stemmer {:lang lang})
    (reset! stemmer (snowball/stemmer (get languages-codes lang :romanian)))
    this)
  (get-root [this text]
    (log @logger :debug ::get-root {:text text})
    (@stemmer text)))

(defmethod ig/init-key :nlptools.tool/stemmer [_ spec]
  (let [{:keys [language logger]} spec]
    (.init (->StemmerTool (atom nil) language (atom nil)) logger)))

(defmethod cmd/help :tool.stemmer [_]
  "tool.stemmer - reduce inflected (or sometimes derived) words to their word stem ")

(defmethod cmd/syntax :tool.stemmer [_]
  "nlptools tool.stemmer -t TEXT")

(defmethod cmd/run :tool.stemmer [_ options summary]
  (let [opts  (cmd/set-config options)
        k :nlptools.tool/stemmer
        config (merge (cmd/make-logger opts)
                      {k {:language (:language opts)
                          :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stemmer (get system k)
        word (get opts :text "")]
    (printf "word: %s,\nstem: %s\n" word (.get-root stemmer word))
    (ig/halt! system)
    0))
