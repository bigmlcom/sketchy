;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.count-min
  "Functions for constructing a count-min sketch.
   http://en.wikipedia.org/wiki/Count-Min_sketch"
  (:refer-clojure :exclude [merge into])
  (:require (bigml.sketchy [murmur :as murmur])))

(defn- hash-offsets [val hashers hash-bits]
  (let [offset (bit-shift-left 1 hash-bits)]
    (map #(+ %1 (* offset %2))
         (take hashers (murmur/hash-seq val :bits hash-bits))
         (range))))

(defn create
  "Creates a count-min sketch given the desired number of hash-bits
   and the number of hashers.  The total number of counters maintained
   by the sketch will be (2^hash-bits)*hashers, so choose these values
   carefully."
  [& {:keys [hash-bits hashers] :or {hash-bits 15 hashers 3}}]
  {:hash-bits hash-bits
   :hashers hashers
   :counters (vec (repeat (* hashers (bit-shift-left 1 hash-bits)) 0))})

(defn- insert* [sketch val]
  (let [{:keys [hashers hash-bits counters]} sketch]
    (assoc sketch
      :counters (reduce #(update-in %1 [%2] inc)
                        counters
                        (hash-offsets val hashers hash-bits)))))

(defn insert [sketch & vals]
  "Inserts one or more values into the count-min sketch."
  (reduce insert* sketch vals))

(defn into [sketch coll]
  "Inserts a collection of values into the count-min sketch."
  (reduce insert* sketch coll))

(defn- merge* [sketch1 sketch2]
  (when (apply not= (map (juxt :hashers :hash-bits) [sketch1 sketch2]))
    (throw (Exception. "Sketch options must match for merging.")))
  (assoc sketch1
    :counters (mapv + (:counters sketch1) (:counters sketch2))))

(defn merge
  "Merges the count-min sketches."
  [sketch & more]
  (reduce merge* sketch more))

(defn estimate-count
  "Returns an estimated occurance count for the value.  The true count
   is guanteed to be no less than the estimate."
  [sketch val]
  (let [{:keys [hashers hash-bits counters]} sketch
        results (remove zero? (map counters (hash-offsets val hashers hash-bits)))]
    (if (empty? results) 0 (apply min results))))
