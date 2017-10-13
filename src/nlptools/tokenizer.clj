(ns nlptools.tokenizer
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [duct.logger :refer [log]])
   (:import 
    [org.languagetool.language Romanian]))


(def languages-obj {"ro" (Romanian.)
;;                     "fr" (French.)
;;                     "en" (AmericanEnglish.)
})


(defprotocol Tokenizer
  (init [this  logger])
  (tokenize [this text]))



(defrecord Boundary [tokenizer lang logger]
  Tokenizer
  (init [this newlogger]
    (reset! logger newlogger)
    (log @logger :info ::init-tokenizer)
    (reset! tokenizer (.getWordTokenizer lang))
    this)
  (tokenize [this text] (.tokenize  @tokenizer text)))

(defmethod ig/init-key :nlptools/tokenizer [_ spec]
  (let [{:keys [logger language] :or {language "ro"}} spec]
    (.init (->Boundary (atom nil) (get languages-obj language) (atom nil)) logger)))
