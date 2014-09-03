;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.bits
  "Functions for an immutable bitset backed by a vector of longs."
  (:refer-clojure :exclude [set or and test])
  (:import (java.lang Math)))

(defn create
  "Creates a bitset supporting the desired number of bits."
  [num-bits]
  (vec (repeat (long (Math/ceil (/ num-bits 64))) 0)))

(defn test
  "Returns true or false for the bit at the given index."
  [bits index]
  (bit-test (bits (bit-shift-right index 6))
            (bit-and index 0x3f)))

(defn- set* [bits index]
  (let [word-index (bit-shift-right index 6)]
    (assoc bits word-index (bit-set (bits word-index)
                                    (bit-and index 0x3f)))))

(defn set
  "Sets the bits associated with each index."
  [bits & indicies]
  (reduce set* bits indicies))

(defn clear* [bits index]
  (let [word-index (bit-shift-right index 6)]
    (assoc bits word-index (bit-clear (bits word-index)
                                      (bit-and index 0x3f)))))

(defn clear
  "Clears the bits associated with each index."
  [bits & indicies]
  (reduce clear* bits indicies))

(defn flip* [bits index]
  (let [word-index (bit-shift-right index 6)]
    (assoc bits word-index (bit-flip (bits word-index)
                                     (bit-and index 0x3f)))))

(defn flip
  "Flips the bits associated with each index."
  [bits & indicies]
  (reduce flip* bits indicies))

(defn set-seq
  "Returns a seq containing the indicies of all set bits."
  [bits]
  ;; TODO - Replace with a not-so-slow implementation!
  (keep #(when (test bits %) %) (range (* 64 (count bits)))))

(defn clear-seq
  "Returns a seq containing the indicies of all unset bits."
  [bits]
  ;; TODO - Replace with a not-so-slow implementation!
  (keep #(when-not (test bits %) %) (range (* 64 (count bits)))))

(defn- check-size! [bits1 bits2]
  (when (not= (count bits1) (count bits2))
    (throw (Exception. "Bit sets are not the same size."))))

(defn and
  "Ands the two bitsets."
  [bits1 bits2]
  (check-size! bits1 bits2)
  (mapv bit-and bits1 bits2))

(defn or
  "Ors the two bitsets."
  [bits1 bits2]
  (check-size! bits1 bits2)
  (mapv bit-or bits1 bits2))

(defn set-count
  "Returns the total number of set bits."
  [bits]
  (reduce + (map #(Long/bitCount %) bits)))

(defn hamming-distance
  "Returns the hamming distance between the bit sets (same as L1 or
   manhattan distance)."
  [bits1 bits2]
  (check-size! bits1 bits2)
  (set-count (map bit-xor bits1 bits2)))

(defn dot-product
  "Returns the dot product (or inner product) of the bit sets."
  [bits1 bits2]
  (check-size! bits1 bits2)
  (set-count (map bit-and bits1 bits2)))

(defn jaccard-similarity
  "Calculates the Jaccard similarity between the bit sets."
  [bits1 bits2]
  (let [union (set-count (map bit-or bits1 bits2))]
    (when (pos? union)
      (double (/ (dot-product bits1 bits2) union)))))

(defn cosine-similarity
  "Calculates the cosine similarity between the bit sets."
  [bits1 bits2]
  (let [magnitude (Math/sqrt (* (set-count bits1) (set-count bits2)))]
    (when (pos? magnitude)
      (/ (dot-product bits1 bits2) magnitude))))
