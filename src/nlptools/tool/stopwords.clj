(ns nlptools.tool.stopwords
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [integrant.core :as ig]
   [duct.logger :refer [log]]
   [nlpcore.protocols :as core]
   [nlpcore.spec :as nsp]
   [nlptools.tool.core :as tool]
   [nlptools.command :as cmd]
   [nlptools.model.core :as model])
  (:import
   (opennlp.tools.tokenize Tokenizer)
   ))


(def ukey
  "this unit key"
  :nlptools.tool/stopwords)

(def cmdkey
  "the command key for this unit"
  :tool.stopwords)

(derive ukey tool/corekey)




(def punctuation #{"," "." " " "?" "!"})


(defn split-words
  [text]
  (str/split text #"\s+"))

(defrecord StopwordsTool [id stopwords tokenizer filepath logger]
  core/Tool
  (build-tool! [this]
    (log @logger :info ::build-tool {:id id :filepath filepath})
    (reset! stopwords (into (hash-set) (-> filepath
                                           slurp
                                           split-words
                                           ))))
  (apply-tool [this text _]
    (log @logger :debug ::apply-tool {:id id :text text})
    (->> text
         str/lower-case
         (.tokenize ^Tokenizer (core/get-model tokenizer))
         (remove punctuation)
         (remove @stopwords))))

(extend StopwordsTool
  core/Module
  core/default-module-impl)

(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [id filepath logger tokenizer] :or {filepath (io/resource "stop_words.ro")}} spec]
    (log logger :debug ::init {:id id})
    (let [tool (->StopwordsTool id (atom nil) tokenizer filepath (atom nil))]
      (core/set-logger! tool logger)
      (core/build-tool! tool)
      tool)))


(s/def ::result (s/coll-of string?))

(deftest apply-tool-test
  (let [config (merge (cmd/make-test-logger :error)
                      {ukey {:id "test stopwords"
                             :tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:id "test tokenizer"
                                                         :logger (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        remover (get system ukey)
        res (core/apply-tool remover "Acesta este un televizor Samsung" {})]
    (is (s/valid? ::result res))
    (is (= ["televizor" "samsung"] res))))


(defmethod cmd/help cmdkey [_]
  (str (name cmdkey) " - remove stopwords from the input"))

(defmethod cmd/syntax cmdkey [_]
  (str "nlptools " (name cmdkey) " -t TEXT"))

(defmethod cmd/run cmdkey [_ options summary]
  (let [opts  (cmd/set-config options)
        config (merge (cmd/make-logger opts)
                      {ukey {:tokenizer (ig/ref :nlptools.model.tokenizer/simple)
                             :logger (ig/ref :duct.logger/timbre)}
                       :nlptools.model.tokenizer/simple {:logger  (ig/ref :duct.logger/timbre)}})
        system (ig/init (cmd/prep-igconfig config))
        stopwords (get system ukey)
        text (get opts :text "")]
    (printf "text         : %s,\nw/o stopwords: %s\n" text (str/join " "(core/apply-tool stopwords text {})))
    (ig/halt! system)
    0))
