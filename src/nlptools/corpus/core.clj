(ns nlptools.corpus.core
  "Corpus common protocol and specs"
  (:require
   [clojure.spec.alpha :as s]))


(def corekey
  "corpus core key"
  :nlptools/corpus)

(defprotocol Corpus
  (build-corpus! [this] "Build and save corpus")
  (get-id [this] "Get corpus id"))

(s/def :corpus/filepath string?)
(s/def :corpus/db map?)
