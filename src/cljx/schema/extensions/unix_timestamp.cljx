(ns schema.extensions.unix-timestamp
  (:require [schema.core :as s]
            [#+clj clj-time.core #+cljs cljs-time.core :as t]
            [#+clj clj-time.coerce #+cljs cljs-time.coerce :as c]
            [schema.extensions.util :refer [PosInt]]
            [schema.extensions.field :refer [describe]]))

;; Second timestamp

(defn unix->date-time [unix]
  (-> unix (* 1000) c/from-long))

(defn date-time->unix [dt]
  (-> dt c/to-long (quot 1000)))

(defn valid-unix-timestamp? [unix]
  (try (t/after? (unix->date-time unix) (t/date-time 2000 1 1))
       (catch #+clj Exception #+cljs js/Error e false)))

(def UnixTimestamp
  (describe (s/both PosInt
                    (s/pred valid-unix-timestamp? 'valid-unix-timestamp?))
            "A unix (seconds since epoch) timestamp"))

;; Millisecond timestamp

(defn ms->date-time [ms] (-> ms c/from-long))

(defn date-time->ms [dt] (c/to-long dt))

(defn valid-ms-timestamp? [ms]
  (try (t/after? (ms->date-time ms) (t/date-time 2000 1 1))
       (catch #+clj Exception #+cljs js/Error e false)))

(def MsTimestamp
  (describe (s/both PosInt
                    (s/pred valid-ms-timestamp? 'valid-ms-timestamp?))
            "A millisecond since epoch timestamp"))


;; Util

(defn unix-or-ms->ms [t]
  (if (valid-unix-timestamp? t) (* t 1000) t))