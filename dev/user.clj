(ns user
  (:require [integrant.repl :as ig-repl :refer [clear go halt prep init reset reset-all]]
            [jiesoul.server :as server]))

(ig-repl/set-prep! (constantly server/system-config))

;; (def go ig-repl/go)
;; (def halt ig-repl/halt)
;; (def reset ig-repl/reset)
;; (def reset-all ig-repl/reset-all)

;; (def go ig-repl/go)
;; (def halt ig-repl/halt)
;; (def reset ig-repl/reset)
;; (def reset-all ig-repl/reset-all)

;; (comment
;;   (go)
;;   (halt)
;;   (reset)
;;   (reset-all))