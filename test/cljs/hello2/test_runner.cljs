(ns hello2.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [hello2.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'hello2.core-test))
    0
    1))
