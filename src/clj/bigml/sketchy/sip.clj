;; Copyright 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.sip
  "Functions for Sip-hashing, wrapping Guava's implementation.
   http://en.wikipedia.org/wiki/SipHash"
  (:refer-clojure :exclude [hash])
  (:import (com.google.common.hash Hashing HashFunction)
           (bigml.sketchy SipUtil)))

(defn- ^HashFunction sip-hasher
  ([]
     (Hashing/sipHash24))
  ([k]
     (Hashing/sipHash24 k (inc k)))
  ([k0 k1]
     (Hashing/sipHash24 k0 k1)))

(defn- hash* [^HashFunction hf val]
  (let [hv (SipUtil/hash hf val)]
    (if (zero? hv)
      (hash* hf (clojure.core/hash val))
      hv)))

(defn- seed->long [seed]
  (if (instance? Long seed)
    seed
    (clojure.core/hash seed)))

(defn hash
  "Returns a long hash given a value and an optional seed."
  ([val]
     (hash* (sip-hasher) val))
  ([val seed]
     (hash* (sip-hasher (seed->long seed)) val)))

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
