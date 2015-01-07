;; Copyright 2013, 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.hyper-loglog
  "Implements the hyper-loglog algorithm backed by a vector of bytes.
   http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.142.9475"
  (:refer-clojure :exclude [merge into])
  (:require (bigml.sketchy [murmur :as murmur])))

(defn create
  "Creates a hyper-loglog sketch whose cardinality estimation error
   is similar to the optional error rate (default is 0.05)."
  ([] (create 0.05))
  ([target-error-rate]
     (let [sketch (max 128 (Math/pow (/ 1.04 target-error-rate) 2))
           sketch (first (drop-while #(< % sketch) (iterate (partial * 2) 128)))]
       (vec (repeat sketch (byte 0))))))

(defn- insert* [sketch val]
  (let [hv (murmur/hash val)
        bin-index (bit-and (dec (count sketch)) hv)
        offset (Long/numberOfTrailingZeros (count sketch))
        zeros (Long/numberOfTrailingZeros (bit-shift-right hv offset))]
    (if (> zeros (sketch bin-index))
      (assoc sketch bin-index (byte zeros))
      sketch)))

(defn insert
  "Inserts one or more values into the hyper-loglog sketch."
  [sketch & vals]
  (reduce insert* sketch vals))

(defn into
  "Inserts a collection of values into the hyper-loglog sketch."
  [sketch coll]
  (reduce insert* sketch coll))

(defn- check-size! [sketch1 sketch2]
  (when (not= (count sketch1) (count sketch2))
    (throw (Exception. "HyperLogLog sketches must be the same size."))))

(defn- merge* [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (mapv (comp byte max) sketch1 sketch2))

(defn merge
  "Merges the hyper-loglog sketches."
  [sketch & more]
  (reduce merge* sketch more))

(defn distinct-count
  "Estimates the number of distinct values inserted into the
   hyper-loglog sketch."
  [sketch]
  (let [m (count sketch)
        v (count (filter zero? sketch))
        e (* (/ (reduce + (map #(Math/pow 2 (- %)) sketch)))
             (bit-shift-left m 1)
             (/ 0.7213 (inc (/ 1.079 m)))
             m)]
    (long (if (and (< e (* 5 m)) (pos? v))
            (* 2 m (Math/log (/ m v)))
            e))))

(defn jaccard-similarity
  "Calculates an estimate of the Jaccard similarity between the sets
   each sketch represents."
  [sketch1 sketch2]
  (check-size! sketch1 sketch2)
  (let [union (distinct-count (merge sketch1 sketch2))
        intersection (- (+ (distinct-count sketch1)
                           (distinct-count sketch2))
                        union)]
    (double (/ intersection union))))
