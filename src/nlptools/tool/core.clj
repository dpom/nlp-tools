(ns nlptools.tool.core
  "Tool common protocol and specs"
  (:require
   [integrant.core :as ig]
   [clojure.spec.alpha :as s]))

(def corekey
  "tool core key"
  :nlptools/tool)


(defprotocol Tool
  (build-tool! [this] "(Re)Build the tool")
  (apply-tool [this text] "Apply the tool to a text")
  (set-logger! [this newlogger] "Set a new logger"))







