(ns schema.extensions.util-macros)

(defmacro if-then
  "For use in threading macro"
  [c f]
  `(fn [a#] (if ~c (-> a# ~f) a#)))