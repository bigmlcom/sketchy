;; Copyright 2013, 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.bloom
  "Functions for constructing a bloom filter.
   http://en.wikipedia.org/wiki/Bloom_filter"
  (:refer-clojure :exclude [merge contains? into distinct])
  (:import (java.lang Math))
  (:require (bigml.sketchy [murmur :as murmur]
                           [bits :as bits])))

(def ^:private log2 (Math/log 2))

(defn- choose-params [population false-positive-prob]
  (let [bins (- (/ (* population (Math/log false-positive-prob))
                   (* log2 log2)))
        bins (first (drop-while #(< % bins) (iterate (partial * 2) 1)))
        k (double (* (/ bins population) log2))]
    [(Long/numberOfTrailingZeros bins)
     (max (Math/round k) 1)]))

(defn create
  "Creates a bloom filter given the expected unique population and the
   desired false positive rate."
  [population false-positive-rate]
  (let [[bits k] (choose-params population false-positive-rate)]
    {:bins (bits/create (bit-shift-left 1 bits))
     :bits bits
     :k k}))

(defn- insert* [bloom val]
  (let [{:keys [bits k bins]} bloom]
    (assoc bloom
      :bins (apply bits/set bins (take k (murmur/hash-seq val bits))))))

(defn insert
  "Inserts one or more values into the bloom filter."
  [bloom & vals]
  (reduce insert* bloom vals))

(defn into
  "Inserts a collection of values into the bloom filter."
  [bloom coll]
  (reduce insert* bloom coll))

(defn- merge* [bloom1 bloom2]
  (when (apply not= (map (juxt :k :bits) [bloom1 bloom2]))
    (throw (Exception. "Bloom options must match for merging.")))
  (assoc bloom1
    :bins (bits/or (:bins bloom1) (:bins bloom2))))

(defn merge
  "Merges the bloom filters."
  [bloom & more]
  (reduce merge* bloom more))

(defn contains?
  "Returns true if the value was inserted into the bloom filter,
   otherwise returns false. False positives are possible, but false
   negatives are not."
  [bloom val]
  (let [{:keys [bits k bins]} bloom]
    (every? true? (map (partial bits/test bins)
                       (take k (murmur/hash-seq val bits))))))

(defn distinct
  "Removes non-distinct items."
  [vals & {:keys [population false-positive-rate]}]
  (let [bf (atom (create (or population 1E4)
                         (or false-positive-rate 1E-2)))]
    (remove #(let [is-member (contains? @bf %)]
               (when-not is-member (swap! bf insert %))
               is-member)
            vals)))
