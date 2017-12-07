(ns nlptools.spec
  (:require
   [clojure.spec.alpha :as s]
   [nlpcore.spec :as nsp]))


(s/def :nlptools/tokenizer map?)
