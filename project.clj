(defproject dpom/nlptools "0.1-dev05"
  :description "Tools for Natural Language Processing"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.7"]
                 [environ "1.1.0"]
                 [integrant "0.6.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.reader "1.1.0"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [mysql/mysql-connector-java "5.1.44"]
                 [hikari-cp "1.7.6"]
                 [com.novemberain/monger "3.1.0"]
                 [org.jsoup/jsoup "1.10.3"]
                 [clojure-opennlp "0.4.0"]
                 ;; [org.apache.opennlp/opennlp-tools "1.8.2"]
                 [snowball-stemmer "0.1.0"]
                 ]
  :pedantic? :warning
  :plugins [[lein-ancient "0.6.10" :exclusions [commons-logging org.clojure/clojure]]
            [jonase/eastwood "0.2.4"]
            [lein-kibit "0.1.6-beta2" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure org.clojure/clojure rewrite-clj]]
            [lein-environ "1.1.0"]
            [lein-codox "0.10.3" :exclusions [org.clojure/clojure]]]
  :repl-options {:init-ns user}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :profiles {:check {:global-vars {*warn-on-reflection* true}}
             :dev {:source-paths   ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :test-paths ["src"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [integrant/repl "0.2.0" :exclusions [org.clojure/tools.namespace]]
                                  [org.clojure/tools.trace "0.7.9"]
                                  ]}
             :uberjar {:aot [nlptools.core]}} 
  :pom-addition [:developers [:developer
                              [:name "Dan Pomohaci"]
                              [:email "dan.pomohaci@gmail.com"]
                              [:timezone "+3"]]]
  :codox {:doc-files []
          :output-path "docs/api"}
)
