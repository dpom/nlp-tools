(ns nlptools.corpus.core
  "Corpus common keys and specs"
  (:require
   [clojure.spec.alpha :as s]))


(def corekey
  "corpus core key"
  :nlptools/corpus)

(s/def :corpus/filepath string?)
(s/def :corpus/db map?)
