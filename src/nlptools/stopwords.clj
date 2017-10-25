(ns nlptools.stopwords
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.command :as cmd]
   ))

(def punctuation #{"," "." " " "?" "!"})

(defprotocol Stopwords
  (init [this tokenizer logger])
  (remove-stopwords [this text]))

(defn split-words
  [text]
  (str/split text #"\s+"))

(defrecord Boundary [stopwords tokenizer filepath logger]
  Stopwords
  (init [this newtokenizer newlogger]
    (reset! logger newlogger)
    (reset! tokenizer newtokenizer)
    (log @logger :info ::init-stopwords {:filepath filepath})
    (reset! stopwords (into (hash-set) (-> filepath
                                           slurp
                                           split-words
                                           )))
    this)
  (remove-stopwords [this text]
    (log @logger :debug ::remove-stopwords {:text text})
    (->> text
         str/lower-case
         (.tokenize @tokenizer)
         (remove punctuation)
         (remove @stopwords))))

(defmethod ig/init-key :nlptools/stopwords [_ spec]
  (let [{:keys [filepath logger tokenizer] :or {filepath (io/resource "stop_words.ro")}} spec]
    (.init (->Boundary (atom nil) (atom nil) filepath (atom nil)) tokenizer logger)))

(defmethod cmd/help :stopwords [_]
  "stopwords - remove stopwords from the input")
