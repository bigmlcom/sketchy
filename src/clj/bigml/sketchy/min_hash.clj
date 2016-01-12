;; Copyright 2013, 2014, 2015, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.min-hash
  "Functions for constructing a min hash.
   http://en.wikipedia.org/wiki/MinHash

   Also includes improvements recommended in:
   'Improved Densification of One Permutation Hashing'
   http://arxiv.org/abs/1406.4784"
  (:refer-clojure :exclude [merge into])
  (:import (java.lang Math)
           (java.util BitSet))
  (:require (bigml.sketchy [murmur :as murmur])))

(def ^:private max-long Long/MAX_VALUE)

(defn- make-rand-shifts [bits]
  (let [arr (make-array Long/TYPE 1)
        _ (aset-long arr 0 bits)
        bs (BitSet/valueOf arr)]
    (for [i (range 64)] (.get bs i))))

(def ^:private rand-shifts
  (mapcat make-rand-shifts (iterate murmur/hash 1)))

(defn create
  "Create a min-hash with an optional desired error rate when
   calculating similarity estimates (defaults to 0.05)."
  ([] (create 0.05))
  ([error-rate]
   (let [min-size (/ (* error-rate error-rate))]
     (vec (repeat (->> (iterate (partial * 2) 128)
                       (drop-while #(< % min-size))
                       (first))
                  max-long)))))

(defn- insert* [sketch val]
  (let [hv (murmur/hash val)
        index (bit-and (dec (count sketch)) hv)
        hv2 (->> (count sketch)
                 (Long/numberOfTrailingZeros)
                 (bit-shift-right hv))]
    (if (> (sketch index) hv2)
      (assoc sketch index hv2)
      sketch)))

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

(defn- densify
  "Densifies the min-hash sketch so it may be used for similarity
   estimation."
  [sketch]
  (let [filled-bins (remove #(= % max-long) sketch)]
    (loop [result []
           bins sketch
           filled-bins (cons (last filled-bins) (cycle filled-bins))
           shifts rand-shifts]
      (cond (empty? bins) result
            (empty? filled-bins) bins
            (= (first bins) max-long)
            (recur (conj result
                         (if (first shifts)
                           (first filled-bins)
                           (second filled-bins)))
                   (next bins)
                   filled-bins
                   (next shifts))
            :else
            (recur (conj result (first bins))
                   (next bins)
                   (next filled-bins)
                   (next shifts))))))

(defn jaccard-similarity
  "Calculates an estimate of the Jaccard similarity between the sets
   each sketch represents."
  [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (-> (filter true? (map = (densify sketch1) (densify sketch2)))
      (count)
      (/ (count sketch1))
      (double)))

(defn- merge* [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (mapv min sketch1 sketch2))

(defn merge
  "Merges the min-hashes (analogous to a union of the sets they
   represent)."
  [sketch & more]
  (reduce merge* sketch more))
