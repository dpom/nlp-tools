(ns nlptools.tool.stopwords
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool]]
   [nlptools.command :as cmd]))

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

(defmethod ig/init-key :nlptools.tool/stopwords [_ spec]
  (let [{:keys [filepath logger tokenizer] :or {filepath (io/resource "stop_words.ro")}} spec]
    (.init (->Boundary (atom nil) (atom nil) filepath (atom nil)) tokenizer logger)))

(defmethod cmd/help :tool.stopwords [_]
  "tool.stopwords - remove stopwords from the input")

(defmethod cmd/syntax :tool.stopwords [_]
  "nlptools tool.stopwords -t TEXT")

(defmethod cmd/run :tool.stopwords [_ options summary]
  (let [opts  (cmd/set-config options)
        k :nlptools.tool/stopwords
        config (merge (cmd/make-logger opts)
                      {k {:tokenizer (ig/ref :nlptools/tokenizer)
                                          :logger (ig/ref :duct.logger/timbre)}
                       :nlptools/tokenizer {:logger  (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stopwords (get system k)
        text (get opts :text "")]
    (printf "text         : %s,\nw/o stopwords: %s\n" text (str/join " "(.remove-stopwords stopwords text)))
    (ig/halt! system)
    0))
