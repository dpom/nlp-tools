(ns nlptools.corpus.claim
  (:require
  [integrant.core :as ig]
  [clojure.java.io :as io]
  [clojure.string :as str]
  [duct.logger :refer [log]]
  [clojure.test :refer :all]
  [nlptools.module.db :as db]
  [nlptools.corpus.core :as corpus])
  (:import
   [org.jsoup Jsoup]))


(def ukey
  "this unit key"
  :nlptools.corpus/claim)

(def cmdkey
  "the command key for this unit"
  :corpus.claim)

(derive ukey corpus/corekey)



(defn strip-html-tags
  "Function strips HTML tags from string."
  [s]
  (.text (Jsoup/parse s)))

(defn filter-row [row]
  (-> row
      :text
      strip-html-tags))

(defn write-corpus! [filename, resultset]
  (with-open [ w (clojure.java.io/writer filename)]
    (reduce  (fn [total row]
               (if (str/blank? row)
                 total
                 (do
                   (.write ^java.io.Writer w row)
                   (.newLine ^java.io.Writer w)
                   (inc total))))
             0 resultset)))


(defrecord ClaimCorpus [filepath db logger]
  corpus/Corpus
  (build-corpus! [this]
    (log logger :info ::creating-corpus {:file filepath})
    (db/query db ["select sheet_text as text from  sheets where subsidiary_id = 1"]
            filter-row
            (partial write-corpus! filepath))
    this))
 
(defmethod ig/init-key ukey [_ spec]
  (let [{:keys [db filepath logger]} spec]
    (->ClaimCorpus filepath db logger)))



