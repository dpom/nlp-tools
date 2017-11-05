---

---

Architecture
============

core
----

Implement the CLI.

The actual actions keywords are stored in `commands` map.

Each action should implement 3 multimethod (defined in `command.clj`):

run  
execute the action

help  
return the help string

syntax  
return the syntax string

model
-----

Contains models implementations.

corpus
------

Contains corpus builder implementations.

module
------

Contains integrant modules.
