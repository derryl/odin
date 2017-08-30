(ns odin.utils.dom
  (:refer-clojure :exclude [val]))


(defn val [event]
  (.. event -currentTarget -value))


(defn checked [event]
  (.. event -currentTarget -checked))
