(ns nlptools.stemmer
  (:require
   [stemmer.snowball :as snowball]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
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
  (get-root [this text] (@stemmer text)))

(defmethod ig/init-key :nlptools/stemmer [_ spec]
  (let [{:keys [language logger]} spec]
    (.init (->Boundary (atom nil) language (atom nil)) logger)))
