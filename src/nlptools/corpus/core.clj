(ns nlptools.corpus.core
  "Corpus common protocol and specs"
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Corpus
  (build-corpus! [this] "Build and save corpus."))

(s/def :corpus/filepath string?)
(s/def :corpus/logger map?)
(s/def :corpus/db map?)
