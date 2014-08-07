;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.min-hash
  "Functions for constructing a min hash.
   http://en.wikipedia.org/wiki/MinHash"
  (:refer-clojure :exclude [merge into])
  (:import (java.lang Math))
  (:require (bigml.sketchy [murmur :as murmur])))

(defn create
  "Create a min-hash with an optional desired error rate when
   calculating similarity estimates (defaults to 0.05)."
  ([] (create 0.05))
  ([error-rate]
     (vec (repeat (int (Math/ceil (/ (* error-rate error-rate))))
                  Integer/MAX_VALUE))))

(defn- insert* [sketch val]
  (mapv min sketch (murmur/hash-seq val)))

(defn insert
  "Inserts one or more values into the min-hash."
  [sketch & vals]
  (reduce insert* sketch vals))

(defn into
  "Inserts a collection of values into the min-hash."
  [sketch coll]
  (reduce insert* sketch coll))

(defn- check-size! [sketch1 sketch2]
  (when (not= (count sketch1) (count sketch2))
    (throw (Exception. "Min-hash sketches must be the same size."))))

(defn jaccard-similarity
  "Calculates an estimate of the Jaccard similarity between the sets
   each sketch represents."
  [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (double (/ (count (filter true? (map = sketch1 sketch2)))
             (count sketch1))))

(defn- merge* [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (mapv min sketch1 sketch2))

(defn merge
  "Merges the min-hashes (analogous to a union of the sets they
   represent)."
  [sketch & more]
  (reduce merge* sketch more))
