(ns nlptools.command)

(defmulti help
  "Returns the command description."
  {:arglists '([key])}
  identity)

(defmethod help :default [_] " ")

(defmulti run
  "Run a command."
  {:arglists '([key config summary])}
 (fn [k _ _] k))




