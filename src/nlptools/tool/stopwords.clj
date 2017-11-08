(ns nlptools.tool.stopwords
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlptools.tool.core :refer [Tool]]
   [nlptools.command :as cmd]))

(def punctuation #{"," "." " " "?" "!"})


(defn split-words
  [text]
  (str/split text #"\s+"))

(defrecord StopwordsTool [stopwords tokenizer filepath logger]
  Tool
  (build-tool! [this]
    (log @logger :info ::build-tool {:filepath filepath})
    (reset! stopwords (into (hash-set) (-> filepath
                                           slurp
                                           split-words
                                           ))))
  (set-logger! [this newlogger]
    (reset! logger newlogger))
  (apply-tool [this text]
    (log @logger :debug ::apply-tool {:text text})
    (->> text
         str/lower-case
         (.tokenize (.get-model tokenizer))
         (remove punctuation)
         (remove @stopwords))))

(defmethod ig/init-key :nlptools.tool/stopwords [_ spec]
  (let [{:keys [filepath logger tokenizer] :or {filepath (io/resource "stop_words.ro")}} spec]
    (log logger :debug ::init)
    (let [tool (->StopwordsTool (atom nil) tokenizer filepath (atom nil))]
      (.set-logger! tool logger)
      (.build-tool! tool)
      tool)))

(defmethod cmd/help :tool.stopwords [_]
  "tool.stopwords - remove stopwords from the input")

(defmethod cmd/syntax :tool.stopwords [_]
  "nlptools tool.stopwords -t TEXT")

(defmethod cmd/run :tool.stopwords [_ options summary]
  (let [opts  (cmd/set-config options)
        k :nlptools.tool/stopwords
        config (merge (cmd/make-logger opts)
                      {k {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                          :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger  (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stopwords (get system k)
        text (get opts :text "")]
    (printf "text         : %s,\nw/o stopwords: %s\n" text (str/join " "(.apply-tool stopwords text)))
    (ig/halt! system)
    0))
