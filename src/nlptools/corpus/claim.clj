(ns nlptools.corpus.claim
  (:require
  [integrant.core :as ig]
  [clojure.java.io :as io]
  [clojure.string :as str]
  [duct.logger :refer [log]]
  [clojure.test :refer :all]
  [nlptools.corpus.core :refer [Corpus]])
  (:import
   [org.jsoup Jsoup]))

(defn strip-html-tags
  "Function strips HTML tags from string."
  [s]
  (.text (Jsoup/parse s)))

(defn filter-row [row]
  (-> row
      :text
      strip-html-tags))

(defn write-corpus! [filename, resultset]
  (with-open [w (clojure.java.io/writer filename)]
    (reduce  (fn [total row]
               (if (str/blank? row)
                 total
                 (do
                   (.write w row)
                   (.newLine w)
                   (inc total))))
             0 resultset)))


(defrecord Boundary [filepath db logger]
  Corpus
  (build-corpus! [this]
    (log logger :info ::creating-corpus {:file filepath})
    (.query db ["select sheet_text as text from  sheets where subsidiary_id = 1"]
            filter-row
            (partial write-corpus! filepath))
    this))
 
(defmethod ig/init-key :nlptools.corpus/claim [_ spec]
  (let [{:keys [db filepath logger]} spec]
    (->Boundary filepath db logger)))



