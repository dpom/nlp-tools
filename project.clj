(defproject nlp-tools "0.1-dev01"
  :description "FIXME: write description"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha19"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]
                 [integrant "0.6.1"]
                 ]
  :pedantic? :warning
  :plugins [[s3-wagon-private "1.1.2" :exclusions [commons-logging commons-codec]]
            [lein-ancient "0.6.10" :exclusions [commons-logging org.clojure/clojure]]
            [jonase/eastwood "0.2.4"]
            [lein-kibit "0.1.6-beta2" :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure org.clojure/clojure rewrite-clj]]
            [lein-environ "1.1.0"]
            [lein-eftest "0.3.1"]
            [lein-codox "0.10.3" :exclusions [org.clojure/clojure]]]
  :repl-options {:init-ns user}
  :profiles {:check {:global-vars {*warn-on-reflection* true}}
             :dev {:source-paths   ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :test-paths ["src"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [integrant/repl "0.2.0" :exclusions [org.clojure/tools.namespace]]
                                  [eftest "0.3.1" :exclusions [org.clojure/tools.namespace]]
                                  [org.clojure/tools.trace "0.7.9"]
                                  ]}
             :uberjar {:aot [nlp-core.core]}}
 
)
