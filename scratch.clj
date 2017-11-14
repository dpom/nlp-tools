
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

(def cat-model (train/train "ro" "ema.train")) 

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

(defn keyword->namespaces [kw]
  (if-let [ns (namespace (str kw))]
    [(symbol ns)
     (symbol (str ns "." (name kw)))])) 

(keyword->namespaces 'nlptools.intent) 

(symbol (str "nlptools." (name :corpus))) 
(symbol (str "nlptools." (name :corpus.intent))) 

(defn try-require [cmd]
  (let [sym (symbol (str "nlptools." cmd))]
    (try (do (require sym) sym)
       (catch java.io.FileNotFoundException _)))) 

(try-require "stemmer") 

nlptools.stemmer/languages-codes 

nlptools.stopwords/punctuation 

(def stopw (try-require "stopwords")) 

(defmacro run [x]
  `(. ~x punctuation)) 

(run nlptools.stopwords)  

(require '[nlptools.command :as cmd])
 
(cmd/help :stopwords)

(cmd/help :stemmer)

(cmd/help :cucu)

(require '[nlptools.model.classification :as model])

(def cat-model (model/train "ro" "test/ema.train"))

(require '[clojure.java.io :as io])

(.serialize cat-model2 (io/as-file "test/ema2.bin"))

(def cat-model2 (model/load "test/ema.bin"))


(require '[nlptools.model.classification :as model])

(import '[opennlp.tools.doccat
  DoccatModel
  DocumentCategorizerME])

(def catmodel (model/load-model "test/ema.bin"))


(defn parse-categories [outcomes-string outcomes]
  "Given a string that represents the opennlp outcomes and an array of
  probability outcomes, zip them into a map of category-probability pairs"
  (zipmap
   (map first (map rest (re-seq #"(\w+)\[.*?\]" outcomes-string)))
   outcomes))

(defn make-document-classifier
[^DoccatModel model]
(fn document-classifier
  [text]
  {:pre [(string? text)]}
  (let [classifier (DocumentCategorizerME. model)
        outcomes (.categorize classifier ^String text)]
    (with-meta
      {:best-category (.getBestCategory classifier outcomes)}
      {:probabilities (parse-categories
                       (.getAllResults classifier outcomes)
                       outcomes)}))))

(def cl (make-document-classifier catmodel))

(def res (cl "vreau informatii despre comanda 1234567890"))

(def classifier (DocumentCategorizerME. catmodel))

(def text "vreau informatii despre comanda 1234567890")

(def outcomes (.categorize classifier ^String text))

(def bestcat (.getBestCategory classifier outcomes))

(import '(opennlp.tools.tokenize TokenizerME
                         SimpleTokenizer
                         WhitespaceTokenizer
                         TokenizerModel
                         TokenSampleStream
                         TokenizerFactory)
        '(opennlp.tools.util Span))

 
(def tokenizer SimpleTokenizer/INSTANCE)

(require '[nlptools.tokenizer :as tok])

(def tokenizer (tok/make-tokenizer model-tokenizer))


(def res (.tokenize tokenizer "vreau informatii despre comanda 1234567890"))

(require '[clojure.pprint :as pp])

(pp/pprint res)


(require '[nlptools.classification :as cat])

(require '[nlptools.model.classification :as model])


(def catmodel (model/load-model "test/ema.bin"))

(def classifier (cat/make-document-classifier catmodel SimpleTokenizer/INSTANCE) )

(def resp (classifier "vreau informatii despre comanda 1234567890"))

resp


(meta resp)

(def s2 (ig/init config [:nlptools/stemmer])) 

(instance? duct.logger.Logger (:duct.logger/timbre s2))  


(def db (:nlptools.module/mongo system)) 

(def entity "category") 

(def entkey (keyword (str "entities." entity))) 
(def resultset (.query db "nlp" {:is_valid true entkey {"$exists" true}} ["text" "entities"])) 

(count resultset) 

(def item (first resultset))

(require '[clojure.string :as str]) 

(let [{:keys [text entities]} item 
      value  (get entities (keyword entity))]
  (str/replace text value (str "<START:" entity ">" value "<END>"))
  ) 

(def tool (:nlptools.tool/entity system)) 

(def res (.apply-tool tool "vreau sa cumpar un televizor")) 
(.apply-tool tool "vreau un telefon si un frigider") 

(def model (.get-model (:model tool))) 
(def tokenizer (.get-model (:tokenizer tool))) 

(import
 (opennlp.tools.tokenize Tokenizer)
 (opennlp.tools.util Span)
 (opennlp.tools.namefind NameFinderME
                         TokenNameFinderModel)
 ) 

(def finder (NameFinderME. model)) 

(def text "Vreau un televizor.") 

(def tokens (.tokenize tokenizer  ^String text))

(defn print-array [arr]
  (doseq [a arr]
     (fipp a)))  

(print-array tokens) 

(def matches (.find finder tokens)) 


(def res (distinct (Span/spansToStrings matches tokens))) 

(require '[nlptools.span :as nspan]) 

(defn to-native-span
  "Take an OpenNLP span object and return a pair [i j] where i and j are the
start and end positions of the span."
  [^Span span]
  (nspan/make-span (.getStart span) (.getEnd span) (.getType span))) 

(map to-native-span matches) 


  (def text "Vreau un televizor si un laptop.") 

(def tokens (.tokenize tokenizer  ^String text)) 


(def matches (.find finder tokens)) 

(def a (.apply-tool tool "Vreau un laptop"))  

(meta a) 

(count matches)

(def m (first matches))  

(str m) 

(.getProb m) 

(def s (Span/spansToStrings #^"[Lopennlp.tools.util.Span;" matches #^"[Ljava.lang.String;" tokens)) 

(first s) 

(require '[fipp.engine :refer (pprint-document)])

(defn ppd [doc]
  (pprint-document doc {:width 10}))

(ppd [:span "One" :line "Two" :line "Three"])

(ppd [:group "(do" [:nest 2 :line "(step-1)" :line "(step-2)"] ")"])

(def tool (:nlptools.tool/classification system)) 

(def res (tool/apply-tool tool "Vreau sa cumpar un cadou")) 

(meta res) 
