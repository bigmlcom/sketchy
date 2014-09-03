;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.count-min
  "Functions for constructing a count-min sketch.
   http://en.wikipedia.org/wiki/Count-Min_sketch"
  (:refer-clojure :exclude [merge into])
  (:require (bigml.sketchy [sip :as sip])))

(defn- hash-offsets [val hashers hash-bits]
  (let [offset (bit-shift-left 1 hash-bits)
        doffset (unchecked-dec offset)]
    (loop [i 0
           offsets []]
      (if (= i hashers)
        offsets
        (recur (inc i)
               (conj offsets (+ (bit-and (sip/hash val i) doffset)
                                (* offset i))))))))

(defn create
  "Creates a count-min sketch given the desired number of hash-bits
   and the number of hashers.  The total number of counters maintained
   by the sketch will be (2^hash-bits)*hashers, so choose these values
   carefully."
  [& {:keys [hash-bits hashers] :or {hash-bits 15 hashers 3}}]
  {:inserts 0
   :hash-bits hash-bits
   :hashers hashers
   :counters (vec (repeat (* hashers (bit-shift-left 1 hash-bits)) 0))})

(defn- insert* [sketch val]
  (let [{:keys [hashers hash-bits counters inserts]} sketch]
    (assoc sketch
      :inserts (inc inserts)
      :counters (reduce #(assoc %1 %2 (inc (%1 %2)))
                        counters
                        (hash-offsets val hashers hash-bits)))))

(defn insert
  "Inserts one or more values into the count-min sketch."
  [sketch & vals]
  (reduce insert* sketch vals))

(defn into
  "Inserts a collection of values into the count-min sketch."
  [sketch coll]
  (reduce insert* sketch coll))

(defn- merge* [sketch1 sketch2]
  (when (apply not= (map (juxt :hashers :hash-bits) [sketch1 sketch2]))
    (throw (Exception. "Sketch options must match for merging.")))
  (assoc sketch1
    :inserts (+ (:inserts sketch1) (:inserts sketch2))
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
