
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
