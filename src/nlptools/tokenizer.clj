(ns nlptools.tokenizer
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [opennlp.span :as nspan])
  (:import
   (opennlp.tools.tokenize TokenizerME
                           SimpleTokenizer
                           WhitespaceTokenizer
                           TokenizerModel
                           TokenSampleStream
                           TokenizerFactory)
   (opennlp.tools.util Span)))

(defn span-strings
  "Takes a collection of spans and the data they refer to. Returns a list of
  substrings corresponding to spans."
  [span-col data]
  (if (seq span-col)
    (if (string? data)
      (seq
       (Span/spansToStrings
        #^"[Lopennlp.tools.util.Span;" (into-array span-col)
        ^String data))
      (seq
       (Span/spansToStrings
        #^"[Lopennlp.tools.util.Span;" (into-array span-col)
        #^"[Ljava.lang.String;" (into-array data))))
    []))

(defn to-native-span
  "Take an OpenNLP span object and return a pair [i j] where i and j are the
start and end positions of the span."
  [^Span span]
  (nspan/make-span (.getStart span) (.getEnd span) (.getType span)))

(defn make-tokenizer 
  [^TokenizerModel model]
  (fn tokenizer
    [sentence]
    {:pre [(string? sentence)]}
    (let [tokenizer (TokenizerME. model)
          spans     (.tokenizePos tokenizer sentence)
          probs     (seq (.getTokenProbabilities tokenizer))
          tokens    (span-strings spans sentence)]
      (with-meta
        (vec tokens)
        {:probabilities probs
         :spans         (map to-native-span spans)}))))




;; (defprotocol Tokenizer
;;   (init [this  logger])
;;   (tokenize [this text]))



;; (defrecord Boundary [tokenizer lang logger]
;;   Tokenizer
;;   (init [this newlogger]
;;     (reset! logger newlogger)
;;     (log @logger :info ::init-tokenizer)
;;     (reset! tokenizer (.getWordTokenizer lang))
;;     this)
;;   (tokenize [this text] (.tokenize  @tokenizer text)))

;; (defmethod ig/init-key :nlptools/tokenizer [_ spec]
;;   (let [{:keys [logger language] :or {language "ro"}} spec]
;;     (.init (->Boundary (atom nil) (get languages-obj language) (atom nil)) logger)))
