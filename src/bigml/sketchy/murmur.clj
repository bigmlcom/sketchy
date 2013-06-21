;; Copyright 2013 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.murmur
  "Functions for Murmur hashing, wrapping Guava's implementation.
   http://en.wikipedia.org/wiki/MurmurHash"
  (:refer-clojure :exclude [hash])
  (:import (com.google.common.hash Hashing HashFunction Hasher HashCode)
           (org.apache.commons.lang ArrayUtils)
           (java.math BigInteger))
  (:require (bigml.sampling [random :as random])))

(def ^:private byte-array-type (Class/forName "[B"))
(def ^:private big-zero (BigInteger. (byte-array 1)))
(def ^:private big-one (BigInteger. (byte-array [(byte 1)])))

(defn- ^HashFunction hash-fn [type seed]
  (cond (= type :int)
        (Hashing/murmur3_32 seed)
        (or (= type :long) (= type :bigint) (= type :bytes))
        (Hashing/murmur3_128 seed)
        :else (throw (Exception. (str "Invalid hash type: " type)))))

(defn- take-bits-big [val bits]
  (.and (biginteger val)
        (.subtract (.setBit big-zero bits) big-one)))

(defn- take-bits [bits val]
  (cond (or (instance? Integer val) (instance? Long val))
        (let [r (bit-and val (unchecked-dec (bit-shift-left 1 bits)))]
          (if (instance? Integer val) (int r) r))
        (instance? clojure.lang.BigInt val)
        (bigint (take-bits-big val bits))
        (instance? byte-array-type val)
        (.toByteArray ^BigInteger (take-bits-big val bits))
        :else (throw (Exception. "Value must be int, long, bigint, or byte[]"))))

(defn- coerce [^HashCode hash-code type bits]
  (let [result (cond (= type :int) (.asInt hash-code)
                     (= type :long) (.asLong hash-code)
                     (or (= type :bigint) (= type :bytes))
                     (let [bytes (.asBytes hash-code)]
                       (if (= type :bigint)
                         (bigint bytes)
                         bytes)))]
    (if bits (take-bits bits result) result)))

(defn- ^HashCode hash* [^HashFunction hasher val]
  (cond (string? val)
        (.hashString hasher ^String val)
        (or (instance? Integer val)
            (instance? Long val))
        (.hashLong hasher (long val))
        (or (instance? Float val)
            (instance? Double val))
        (.hashLong hasher (Double/doubleToLongBits val))
        (instance? byte-array-type val)
        (.hashBytes hasher val)
        :else
        ;; Using Clojure's standard hash as a seed means more possible
        ;; collisions for the Murmur hash, but I think this is okay as
        ;; a catch-all
        (.hashLong hasher (clojure.core/hash val))))

(defn- pick-type [type bits]
  (or type
      (when bits (condp > bits 32 :int 64 :long :bigint))
      :int))

(defn hash
  "Returns a Murmur hashes of the given value.

   Options:
     type - The desired output format of the hashed value (:int, :long,
            :bigint, or :bytes).
     bits - Truncates the hash to the desired number of bits.
     seed - A long value as a seed for the hashing function."
  [val & {:keys [seed type bits] :or {seed 0}}]
  (let [type (pick-type type bits)]
    (coerce (hash* (hash-fn type (clojure.core/hash seed)) val)
            type bits)))

(defn int-hash
  "Hashes the given value into an integer representing 32 bits. A
   faster alternative to the generalized Murmur 'hash' fn."
  [val]
  (.asInt (hash* (Hashing/murmur3_32) val)))

(defn long-hash
  "Hashes the given value into a long representing 64 bits. A
   faster alternative to the generalized Murmur 'hash' fn."
  [val]
  (.asLong (hash* (Hashing/murmur3_128) val)))

(defn bytes-hash
  "Hashes the given value into a byte array representing 128 bits. A
   faster alternative to the generalized Murmur 'hash' fn."
  [val]
  (.asBytes (hash* (Hashing/murmur3_128) val)))

(defn bigint-hash
  "Hashes the given value into a bigint representing 128 bits. A
   faster alternative to the generalized Murmur 'hash' fn."
  [val]
  (bigint (bytes-hash val)))

(defn hash-seq
  "Applies an infinite sequence of Murmur hash functions to the value,
   creating one hash for each function.

   Options:
     type - The desired output format of the hashed values (:int, :long,
            :bigint, or :bytes).
     bits - Truncates the hash to the desired number of bits.
     seed - A long value as a seed for the hashing functions."
  [val & {:keys [bits seed type] :or {seed 0}}]
  (let [type (pick-type type bits)
        rng (random/create :seed seed)]
    (repeatedly #(coerce (hash* (hash-fn type (random/next-int! rng))
                                val)
                         type
                         bits))))
