(defproject dpom/nlptools "0.6-dev01"
  :description "Tools for Natural Language Processing"
  :url "https://dpom.github.io/nlp-tools/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.reader "1.1.0"]
                 [environ "1.1.0"]
                 [integrant "0.6.1"]
                 ;; [duct/logger "0.2.1"]
                 [duct/logger.timbre "0.4.1"]
                 ;; [com.fzakaria/slf4j-timbre "0.3.7"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [mysql/mysql-connector-java "5.1.44"]
                 [hikari-cp "1.7.6"]
                 [com.novemberain/monger "3.1.0"]
                 [org.jsoup/jsoup "1.10.3"]
                 [org.languagetool/language-ro "3.9" :exclusions [com.google.guava/guava]]
                 [org.apache.opennlp/opennlp-tools "1.8.3"]
                 [snowball-stemmer "0.1.0"]
                 ]
  :pedantic? :warning
  :plugins [[lein-ancient "0.6.10" :exclusions [commons-logging org.clojure/clojure]]
            [jonase/eastwood "0.2.6-beta2"]
            [lein-kibit "0.1.6" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure org.clojure/clojure rewrite-clj]]
            [lein-environ "1.1.0"]
            [lein-codox "0.10.3" :exclusions [org.clojure/clojure]]]
  :main nlptools.core
  :repl-options {:init-ns user}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :profiles {:check {:global-vars {*warn-on-reflection* true}}
             :dev {:source-paths   ["dev/src"]
                   :resource-paths ["resources" "dev/resources"]
                   :test-paths ["src"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [integrant/repl "0.2.0" :exclusions [org.clojure/tools.namespace]]
                                  [org.clojure/tools.trace "0.7.9"]
                                  [fipp "0.6.10"]
                                  ]}
             :uberjar {:aot [nlptools.core]}} 
  :jvm-opts ["-Xmx2048m"]
  :pom-addition [:developers [:developer
                              [:name "Dan Pomohaci"]
                              [:email "dan.pomohaci@gmail.com"]
                              [:timezone "+3"]]]
  :codox {:doc-files []
          :exclude-vars nil
          :project {:name "nlptools"}
          :source-paths ["src"]
          :output-path "docs/api"}
)
