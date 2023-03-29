(ns backend.util.db-util)

(defn populate 
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str " generated always as identity "
                        " (start with 1, increment by 1) "
                        " primary key "))]
    ))

(defn opt-to-sql [s {:keys [filter sort page] :as query}]
  (if (seq query)
    (let [[s v] [s []]
          [s v] (if filter 
                  [(str s " where " (first filter)) (into v (second filter))]
                  [s v])
          [s v] (if sort 
                  [(str s " order by ? ") (conj v sort)]
                  [s v])
          [s v] (if page 
                  [(str s " limit ? offset ? ") (into v page)]
                  [s v])]
      (into [s] v))
    s))