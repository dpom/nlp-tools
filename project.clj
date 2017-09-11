(defproject nlptools "0.1-dev01"
  :description "FIXME: write description"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha19"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.7"]
                 [environ "1.1.0"]
                 [integrant "0.6.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.reader "1.1.0"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [mysql/mysql-connector-java "5.1.44"]
                 [hikari-cp "1.7.6"]
                 [org.jsoup/jsoup "1.10.3"]
                 ]
  :pedantic? :warning
  :main ^:skip-aot nlptools.core
  :plugins [[s3-wagon-private "1.1.2" :exclusions [commons-logging commons-codec]]
            [lein-ancient "0.6.10" :exclusions [commons-logging org.clojure/clojure]]
            [jonase/eastwood "0.2.4"]
            [lein-kibit "0.1.6-beta2" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure org.clojure/clojure rewrite-clj]]
            [lein-environ "1.1.0"]
            [lein-codox "0.10.3" :exclusions [org.clojure/clojure]]]
  :repl-options {:init-ns user}
  :profiles {:check {:global-vars {*warn-on-reflection* true}}
             :dev {:source-paths   ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :test-paths ["src"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [integrant/repl "0.2.0" :exclusions [org.clojure/tools.namespace]]
                                  [org.clojure/tools.trace "0.7.9"]
                                  ]}
             :uberjar {:aot [nlptools.core]}}
 
)
