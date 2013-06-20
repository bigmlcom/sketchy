;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.demo
  (:use clojure.test)
  (:import (java.util.zip GZIPInputStream))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def ^:private split-pattern #"\r?\n| ")
(def ^:private stop-tokens #{"" "?" "." "," "!" ";" ":"})

(defn- get-tokens [file]
  (with-open [in (-> file io/input-stream GZIPInputStream.)]
    (map str/lower-case
         (remove #{"" "?" "." "," "!" ";" ":"}
                 (str/split (slurp in) #"\r?\n| ")))))

(def hamlet-tokens (get-tokens "res/hamlet.txt.gz"))

(let [parts (partition-all (/ (count hamlet-tokens) 2) hamlet-tokens)]
  (def hamlet-part1 (first parts))
  (def hamlet-part2 (second parts)))

(def midsummer-tokens (get-tokens "res/midsummer.txt.gz"))

(let [parts (partition-all (/ (count midsummer-tokens) 2) midsummer-tokens)]
  (def midsummer-part1 (first parts))
  (def midsummer-part2 (second parts)))
