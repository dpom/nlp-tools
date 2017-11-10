(ns nlptools.corpus.core)

(defprotocol Corpus
  (build-corpus! [this] "Build and save corpus."))
