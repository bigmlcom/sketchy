;; Copyright 2014, 2015 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.murmur
  "Functions for Murmur hashing.
   http://en.wikipedia.org/wiki/MurmurHash"
  (:refer-clojure :exclude [hash])
  (:import (bigml.sketchy MurmurUtil)))

(def ^:private default-seed 1651860712)

(defn- hash* [val seed]
  (if val
    (let [hv (MurmurUtil/hash val seed)]
      (if (zero? hv)
        (hash* (clojure.core/hash val) seed)
        hv))
    0))

(defn- seed->long [seed]
  (cond (nil? seed) default-seed
        (not (instance? Long seed)) (clojure.core/hash seed)
        (zero? seed) default-seed
        :else seed))

(defn hash
  "Returns a long hash given a value and an optional seed."
  ([val]
     (hash* val default-seed))
  ([val seed]
     (hash* val (seed->long seed))))

(defn truncate
  "Truncates the hash-value given the desired number of bits."
  [hash-val bits]
  (bit-and hash-val (unchecked-dec (bit-shift-left 1 bits))))

(defn hash-seq
  "Returns a lazy infinite sequence of hashes (each with a unique
   seed) given a value and optional desired bits."
  ([val]
     (map hash (repeat val) (range)))
  ([val bits]
     (map (comp #(truncate % bits) hash)
          (repeat val)
          (range))))
