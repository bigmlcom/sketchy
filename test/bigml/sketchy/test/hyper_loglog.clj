;; Copyright 2013, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.sketchy.test.hyper-loglog
  (:require [clojure.test :refer :all]
            (bigml.sketchy [hyper-loglog :as hll])))

(defn- gen-data [size]
  (let [maxint Integer/MAX_VALUE]
    (repeatedly size #(rand-int maxint))))

(defn- measure-error [target-error data-size]
  (/ (Math/abs (- (hll/distinct-count
                   (reduce hll/insert
                           (hll/create target-error)
                           (gen-data data-size)))
                  data-size))
     data-size))

(defn- mean [vals]
  (/ (double (reduce + vals))
     (count vals)))

(defn- trials [trial-count target-error data-size]
  {:target-error target-error
   :actual-error (mean (repeatedly trial-count
                                   #(measure-error target-error data-size)))})

(deftest hyper-loglog-test
  (is (> 0.2 (:actual-error (trials 20 0.02 15000))))
  (let [h1 (reduce hll/insert (hll/create 0.01) (gen-data 10000))
        h2 (reduce hll/insert (hll/create 0.01) (gen-data 15000))]
    (is (<= 24300 (hll/distinct-count (hll/merge h1 h2)) 25700))))

(deftest similarity-test
  (is (<= 0.85
          (hll/jaccard-similarity (hll/into (hll/create) (range 0 5000))
                                  (hll/into (hll/create) (range 500 5000)))
          0.95))
  (is (== 1 (hll/jaccard-similarity (hll/merge (hll/into (hll/create)
                                                         (range 1000))
                                               (hll/into (hll/create)
                                                         (range 500 1500)))
                                    (hll/into (hll/create) (range 1500))))))
