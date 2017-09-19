
(require '[clojure.java.jdbc :as j])

(def mysql-db {:dbtype "mysql"
               :host "gen-mysql9483-all-dev.om1.c.emag.network"
               :port 13353
               :dbname "claims"
               :user "root"
               :password ""})




(j/with-db-metadata [md mysql-db]
  (j/metadata-result (.getTables md nil nil nil (into-array ["TABLE" "VIEW"]))))

(j/query mysql-db
         ["select count(*) from sheets where subsidiary_id = 1"])

(j/query mysql-db
         ["select sheet_text as text from  sheets where subsidiary_id = 1"]
         {:result-set-fn first})

(def db (get-in system [:nlptools/db :spec]))

(j/with-db-connection [conn db]
  (j/query conn
           ["select sheet_text as text from  sheets where subsidiary_id = 1"]
           {:result-set-fn first}))

(j/query db
         ["select sheet_text as text from  sheets where subsidiary_id = 1"]
         {:result-set-fn first})

(def db (get system :nlptools/db))

(.query db ["select sheet_text as text from  sheets where subsidiary_id = 1"] nil first)

(defn write-corpus [filename, resultset]
  (with-open [w (clojure.java.io/writer filename)]
    (reduce  (fn [total row]
               (.write w (:text row))
               (.newLine w)
               (inc total))
             0 resultset)))

(.query db ["select sheet_text as text from  sheets where subsidiary_id = 1"] nil (partial write-corpus "corpus.txt"))

(require '[nlptools.corpus :refer :all])

(create-corpus! system "corpus.txt")

(import '[org.languagetool.language Romanian])

(def ro (Romanian.))

(require '[clojure.pprint :as pp]) 

(pp/pprint (.getCountries ro))  

(def tok (.getWordTokenizer ro)) 

(.tokenize tok "Aceasta este o propzitie.") 

(def tagger (.getTagger ro)) 


(def taged_sentence (.tag tagger (.tokenize tok "Aceasta este o propzitie."))) 

(.getToken (first taged_sentence)) 
(.getReadings (first taged_sentence)) 


(import '[opennlp.tools.stemmer.snowball SnowballStemmer]) 

(def stemmer (SnowballStemmer. SnowballStemmer/ALGORITHM/ROMANIAN)) 


(import '[opennlp.tools.stemmer.snowball romanianStemmer]) 


(def stemmer (romanianStemmer.)) 

(.stem stemmer "lasi") 

(require '[stemmer.snowball :as snowball]) 

(def stemmer (snowball/stemmer :romanian)) 

(stemmer "lasi") 

(stemmer "băieţel") 


(stemmer "femeile") 

(stemmer "fete") 
