;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.hyper-loglog
  "Implements the hyper-loglog algorithm backed by a vector of bytes.
   http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.142.9475"
  (:refer-clojure :exclude [merge into])
  (:require (bigml.sketchy [murmur :as murmur])))

(defn create
  "Creates a hyper-loglog sketch whose cardinality estimation error
   is similar to the given error rate."
  [target-error-rate]
  (let [bins (max 128 (Math/pow (/ 1.04 target-error-rate) 2))
        bins (first (drop-while #(< % bins) (iterate (partial * 2) 128)))]
    (vec (repeat bins (byte 0)))))

(defn- insert* [bins val]
  (let [hash (murmur/long-hash val)
        bin-index (bit-and (dec (count bins)) hash)
        offset (Long/numberOfTrailingZeros (count bins))
        zeros (Long/numberOfTrailingZeros (bit-shift-right hash offset))]
    (if (> zeros (bins bin-index))
      (assoc bins bin-index (byte zeros))
      bins)))

(defn insert
  "Inserts the values into the hyper-loglog sketch."
  [bins & vals]
  (reduce insert* bins vals))

(defn- merge* [bins1 bins2]
  (when (not= (count bins1) (count bins2))
    (throw (Exception. "HyperLogLog bins must be the same length to merge.")))
  (mapv (comp byte max) bins1 bins2))

(defn merge
  "Merges the hyper-loglog sets."
  [bins & more]
  (reduce merge* bins more))

(defn distinct-count
  "Estimates the number of distinct values inserted into the
   hyper-loglog sketch."
  [bins]
  (let [m (count bins)
        v (count (filter zero? bins))
        e (* (/ (reduce + (map #(Math/pow 2 (- %)) bins)))
             (bit-shift-left m 1)
             (/ 0.7213 (inc (/ 1.079 m)))
             m)]
    (long (if (and (< e (* 5 m)) (pos? v))
            (* 2 m (Math/log (/ m v)))
            e))))
