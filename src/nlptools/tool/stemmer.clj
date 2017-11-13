(ns nlptools.tool.stemmer
  (:require
   [stemmer.snowball :as snowball]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :as tool]
   [nlptools.command :as cmd]))

(def languages-codes {"ro" :romanian
                      "en" :english})

(def ukey
  "this unit key"
  :nlptools.tool/stemmer)

(def cmdkey
  "the command key for this unit"
  :tool.stemmer)

(derive ukey tool/corekey)


(defrecord StemmerTool [stemmer lang logger]
  tool/Tool
  (build-tool! [this]
    (log @logger :info ::build-tool {:lang lang})
    (reset! stemmer (snowball/stemmer (get languages-codes lang :romanian)))
    this)
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (apply-tool [this text]
    (log @logger :debug ::apply-tool {:text text})
    (@stemmer text)))

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [language logger]} spec]
    (log logger :debug ::init)
    (let [stemmer (->StemmerTool (atom nil) language (atom nil))]
      (tool/set-logger! stemmer logger)
      (tool/build-tool! stemmer)
      stemmer)))

(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - reduce inflected (or sometimes derived) words to their word stem "))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -t TEXT"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        k :nlptools.tool/stemmer
        config (merge (cmd/make-logger opts)
                      {k {:language (:language opts)
                          :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stemmer (get system k)
        word (get opts :text "")]
    (printf "word: %s,\nstem: %s\n" word (tool/apply-tool stemmer word))
    (ig/halt! system)
    0))
