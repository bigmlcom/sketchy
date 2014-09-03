
# Sketching Algorithms in Clojure

## Installation

`sketchy` is available as a Maven artifact from
[Clojars](http://clojars.org/bigml/sketchy).

For [Leiningen](https://github.com/technomancy/leiningen):

```clojure
[bigml/sketchy "0.2.0"]
```

## Overview

This library contains various sketching/hash-based algorithms useful
for building compact summaries of large datasets.

All the sketches are composed using vanilla Clojure data
structures. That means immutability and easy serialization but humble
performance. [stream-lib](https://github.com/addthis/stream-lib) is a
good alternative for those in need of speed.

General Utilities:
- [SipHash](#sip-hash)
- [Immutable Bitset](#immutable-bitset)

Sketching/hash-based algorithms:
- [Bloom Filter](#bloom-filter)
- [Min Hash](#min-hash)
- [Hyper-LogLog](#hyper-loglog)
- [Count-Min](#count-min)

As we review each section, feel free to follow along in the REPL. Note
that `bigml.sketchy.test.demo` loads *Hamlet* and *A Midsummer Night's
Dream* into memory for our code examples.

```clojure
user> (ns test
        (:use [bigml.sketchy.test.demo])
        (:require (bigml.sketchy [sip :as sip]
                                 [bits :as bits]
                                 [bloom :as bloom]
                                 [min-hash :as min-hash]
                                 [hyper-loglog :as hll]
                                 [count-min :as count-min])))
```

## SipHash

The `bigml.sketchy.sip` namespace piggybacks
[Guava](https://code.google.com/p/guava-libraries/) to make it easy to
generate [Sip hashes](http://en.wikipedia.org/wiki/SipHash).  Sip
hashes are popular as they are resonably quick to produce, adequately
random, and robust to security issues.

These Sip hashes are all produced as 64 bit longs.  A simple example
hashing the string "foo" to a long:

```clojure
test> (sip/hash "foo")
2121124175213604009
```

An optional seed parameter selects a unique hashing function. Anything
that's hashable by `clojure.core/hash` is valid as a seed.

```clojure
test> (sip/hash "foo" 0)
-4874926522583603657
test> (sip/hash "foo" 42)
-1062743973399493368
test> (sip/hash "foo" :bar)
-5589927979006578086
```

The `truncate` function can be used to truncate the number of bits
(must be less than 64 and more than 0).

```clojure
test> (sip/truncate (sip/hash "foo") 32)
4229541033
test> (sip/truncate (sip/hash "foo") 16)
44201
test> (sip/truncate (sip/hash "foo") 8)
169
```

If you need multiple unique hashes for a value, `hash-seq` is a
convenience function for that.  It applies an infinite sequence of
unique hash functions (always in the same order), so `take` as many
as you need.

```clojure
test> (take 3 (sip/hash-seq "foo"))
(-4874926522583603657 2540774949006662565 6327481778609814014)
```

## Immutable Bitset

Besides being my favorite name for a namespace, `bigml.sketchy.bits`
provides an immutable bitset supporting bit-level operations for any
number of bits. The bitset is backed by a vector of longs.

The `create` function builds a bitset given the desired number of
bits. Every bit will be initialized as clear (all zero).

The `set` function sets the bits at the given indicies. The `test`
function returns true if the bit at the given index is set.

```clojure
test> (def my-bits (-> (bits/create 256)
                       (bits/set 2 48 58 184 233)))
test> (bits/test my-bits 47)
false
test> (bits/test my-bits 48)
true
```

The `set-seq` function returns the indicies of every set
bit. Alternatively, `clear-seq` returns all the clear bits.

```clojure
test> (bits/set-seq my-bits)
(2 48 58 184 233)
```

The `clear` function complements `set` by clearing the bits for the
given indices. Similarly, the `flip` function reverses a bit's state.

```clojure
test> (bits/set-seq (bits/clear my-bits 48))
(2 58 184 233)
test> (bits/set-seq (bits/flip my-bits 48))
(2 58 184 233)
```

Moreover, the namespace offers functions to `and` and `or` two
bitsets. You can also measure `hamming-distance`,
`jaccard-similarity`, or `cosine-similarity`.

## Bloom Filter

`bigml.sketchy.bloom` contains an implementation of a [Bloom
filter](http://en.wikipedia.org/wiki/Bloom_filter), useful for testing
set membership. When checking set membership for an item, false
positives are possible but false negatives are not.

You may `create` a Bloom filter by providing the expected number of
items to be inserted into the filter and the acceptable
false positive rate.

After creating the filter, you may either `insert` individual items or
add an entire collection of items `into` the Bloom filter.

```clojure
test> (def hamlet-bloom
        (reduce bloom/insert
                (bloom/create (count hamlet-tokens) 0.01)
                hamlet-tokens))

test> (def midsummer-bloom
        (bloom/into (bloom/create (count midsummer-tokens) 0.01)
                    midsummer-tokens))
```

Item membership is tested with `contains?`.

```clojure
test> (bloom/contains? hamlet-bloom "puck")
false
test> (bloom/contains? midsummer-bloom "puck")
true
```

The Bloom filters are also merge friendly as long as they are
initialized with the same parameters.

```clojure
test> (def summerham-bloom
        (let [total (+ (count hamlet-tokens) (count midsummer-tokens))]
          (bloom/merge (bloom/into (bloom/create total 0.01) midsummer-tokens)
                       (bloom/into (bloom/create total 0.01) hamlet-tokens))))
test> (bloom/contains? summerham-bloom "puck")
true
test> (bloom/contains? summerham-bloom "yorick")
true
test> (bloom/contains? summerham-bloom "henry")
false
```

## Min-Hash

`bigml.sketchy.min-hash` contains an implementation of the
[MinHash](http://en.wikipedia.org/wiki/MinHash) algorithm, useful for
comparing the [Jaccard
similarity](http://en.wikipedia.org/wiki/Jaccard_index) of two sets.

To `create` a MinHash, you may provide a target error rate for
similarity (default is 0.05). After that, you can either `insert`
individual values or add collections `into` the MinHash.

In the following example we break *A Midsummer Night's Dream* into two
halves (`midsummer-part1` and `midsummer-part2`) and build a MinHash
for each. We then compare the two parts together to see if they are
more similar than a MinHash of *Hamlet*.

As we'd expect, the two halves of *A Midsummer Night's Dream* are more
alike than *Hamlet*.

```clojure
test> (def hamlet-hash (min-hash/into (min-hash/create) hamlet-tokens))
test> (def midsummer1-hash (min-hash/into (min-hash/create) midsummer-part1))
test> (def midsummer2-hash (min-hash/into (min-hash/create) midsummer-part2))
test> (min-hash/jaccard-similarity midsummer1-hash midsummer2-hash)
0.2875
test> (min-hash/jaccard-similarity midsummer1-hash hamlet-hash)
0.2175
```

The MinHashes are merge friendly as long as they're initialized with
the same target error rate.

```clojure
test> (def midsummer-hash (min-hash/into (min-hash/create) midsummer-tokens))
test> (min-hash/jaccard-similarity midsummer-hash
                                   (min-hash/merge midsummer1-hash
                                                   midsummer2-hash))
1.0
```

## Hyper-LogLog

`bigml.sketchy.hyper-loglog` contains an implementation of the
[HyperLogLog](http://research.google.com/pubs/pub40671.html) sketch,
useful for estimating the number of distinct items in a set. This is a
technique popular for tracking unique visitors over time.

To `create` a HyperLogLog sketch, you may provide a target error rate
for distinct item estimation (default is 0.05). After that, you can
either `insert` individual values or add collections `into` the
sketch.

```clojure
test> (def hamlet-hll (hll/into (hll/create 0.01) hamlet-tokens))
test> (def midsummer-hll (hll/into (hll/create 0.01) midsummer-tokens))
test> (count (distinct hamlet-tokens)) ;; actual
4793
test> (hll/distinct-count hamlet-hll)  ;; estimated
4868
test> (count (distinct midsummer-tokens)) ;; actual
3034
test> (hll/distinct-count midsummer-hll) ;; estimated
3018
```

HyperLogLog sketches may be merged if they're initialized with the
same error rate.

```clojure
test> (count (distinct (concat hamlet-tokens midsummer-tokens))) ;; actual
6275
test> (hll/distinct-count (hll/merge hamlet-hll midsummer-hll)) ;; estimated
6312
```

Similar to MinHash, HyperLogLog sketches can also provide an estimate
of the [Jaccard
similarity](http://en.wikipedia.org/wiki/Jaccard_index) between two
sets.

```clojure
test> (def midsummer1-hll (hll/into (hll/create 0.01) midsummer-part1))
test> (def midsummer2-hll (hll/into (hll/create 0.01) midsummer-part2))
test> (hll/jaccard-similarity midsummer1-hll midsummer2-hll)
0.2833001988071571
test> (hll/jaccard-similarity midsummer1-hll hamlet-hll)
0.201231310466139
```

## Count-Min

`bigml.sketchy.count-min` provides an implementation of the [Count-Min
sketch](http://en.wikipedia.org/wiki/Count-Min_sketch), useful for
estimating frequencies of arbritrary items in a stream.

To `create` a count-min sketch you may define the desired number of
hash-bits and the number of independent hash functions.  The total
number of counters maintained by the sketch will be
(2^hash-bits)*hashers, so choose these values carefully.

After creating a sketch, you may either `insert` individual values or
add collections `into` the sketch.

In the example below we build a Count-Min sketch that uses 1500
counters to estimate frequencies for the 4800 unique tokens in
*Hamlet*.

```clojure
test> (def hamlet-cm (count-min/into (count-min/create :hash-bits 9)
                                     hamlet-tokens))
test> (count (:counters hamlet-cm))
1536
test> ((frequencies hamlet-tokens) "hamlet")
77
test> (count-min/estimate-count hamlet-cm "hamlet")
87
test> ((frequencies hamlet-tokens) "rosencrantz")
7
test> (count-min/estimate-count hamlet-cm "rosencrantz")
15
```

As with the other sketching algorithms, Count-Min sketches may be
merged if they're initialized with the same parameters.

```clojure
test> (def midsummer1-cm (count-min/into (count-min/create :hash-bits 9)
                                         midsummer-part1))
test> (def midsummer2-cm (count-min/into (count-min/create :hash-bits 9)
                                         midsummer-part2))
test> ((frequencies midsummer-tokens) "love") ;; actual count
98
test> (count-min/estimate-count (count-min/merge midsummer1-cm midsummer2-cm)
                                "love")
104
```

## License

Copyright (C) 2013 BigML Inc.

Distributed under the Apache License, Version 2.0.
