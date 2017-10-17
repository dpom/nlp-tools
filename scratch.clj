
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

(import java.util.Properties)

(defn get-version [dep]
  (let [path (str "META-INF/maven/" (or (namespace dep) (name dep))
                  "/" (name dep) "/pom.properties")
        props (io/resource path)]
    (when props
      (with-open [stream (io/input-stream props)]
        (let [props (doto (Properties.) (.load stream))]
          (.getProperty props "version"))))))

(get-version 'nlptools) 
(get-version 'org.clojure/clojure) 


(def db (get system :nlptools/mongo)) 

(def resultset (.query db "nlp" {:is_valid true} ["text" "entities"]))  

(defn write-corpus! [filename, resultset]
  (with-open [w (clojure.java.io/writer filename)]
    (reduce  (fn [total {:keys [text entities]}]
               (let [intent (get entities :intent "necunoscut")]
                   (.write w (format "%s %s" intent text))
                   (.newLine w)
                   (inc total)))
             0 resultset))) 

(write-corpus! "ema.train" resultset) 


(require '[opennlp.nlp :as onlp]) 
(require '[opennlp.treebank :as tb]) 
(require '[opennlp.tools.train :as train]) 

(def cat-model (train/train-document-categorization "ro" "ema.train")) 

(def get-category (onlp/make-document-categorizer cat-model)) 

(get-category "Vreau un telefon") 
(get-category "Vreau un cadou") 
(get-category "Ce stii despre  ultima mea comanda") 
(get-category "Vreau produse din categoria laptopuri") 

(def intent (get system :nlptools/intent)) 

(.get-intent intent "Vreau un telefon") 

;; (alter-var-root #'state/system (fn [sys] (halt-system sys) (ig/init state/config)))
(alter-var-root #'system (fn [sys] (ig/init config)))

(require '[duct.logger :as logger]) 

(defrecord TestLogger [logs]
  logger/Logger
  (-log [_ level ns-str file line id event data]
    (swap! logs conj [event data]))) 

(def  logger   (->TestLogger (atom []))) 
(.init intent (get system :nlptools/intent-corpus) logger) 

@(:logs logger) 

(def corpus-intent (get system :nlptools.corpus/intent)) 

(.init corpus-intent logger) 


(def stemmer (get system :nlptools/stemmer)) 

(.get-root stemmer "fetita") 
(.get-root stemmer "fetiţa") 
(.get-root stemmer "fetitele") 

(.init stemmer logger) 

(def stop-words (slurp (io/resource "stop_words.ro")))

(require '[clojure.string :as str]) 

(defn split-words
  [text]
  (str/split (str/lower-case text) #"\s+")) 



(def stop-words (into (hash-set) (-> (io/resource "stop_words.ro")
                    slurp
                    split-words
                    ))) 

(remove stop-words (split-words "Acesta este un text")) 
(remove stop-words (split-words "Vreau sa cumpar un televizor")) 


(def stopwords (get system :nlptools/stopwords)) 
(.remove-stopwords stopwords "Acesta este un text") 
(.init stopwords logger) 

@(:logs logger) 

(split-words "aceasta, este o propozitie?") 

(.tokenize tok "Aceasta, este o propozitie?") 

(.remove-stopwords stopwords "Aceasta, este o propozitie?") 

(def stemmer (get system :nlptools/stemmer)) 

(.get-root stemmer "fetita") 

(.get-root stemmer "fetita fetitele") 

(.get-root stemmer " fetita") 
