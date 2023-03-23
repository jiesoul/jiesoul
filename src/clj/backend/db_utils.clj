(ns backend.db-utils)

(defn populate 
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str " generated always as identity "
                        " (start with 1, increment by 1) "
                        " primary key "))]
    ))

(defn opt-to-sql [s {:keys [filter sort page]}]
  (let [v []
        [s v] (if (empty? filter) [s v] [(str s " where " (first filter)) (into v (second filter))])
        [s v] (if (empty? sort) [s v] [(str s " order by " sort) v])
        [s v] (if (empty? page) [s v] [(str s " limit ? offset ? ") (into v page)])]
    (into [s] v)))