(ns org.ego.accounts
  (:gen-class)
  (:import [java.net ServerSocket Socket SocketException InetAddress InetSocketAddress URL]
           [java.io InputStreamReader OutputStreamWriter PushbackReader ByteArrayInputStream Reader Writer OutputStream FileInputStream]
           [org.apache.log4j Logger])
  (:require [org.ego.config :as config]
            [clojure.contrib.sql :as sql]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; Common

(def #^{:private true} log (. Logger (getLogger (str *ns*))))
(def #^{:private true} conf (config/get-properties "server"))
(def #^{:private true} dbconf (config/get-properties "database"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; SQL

(defn login
  [username password]
  (sql/with-connection dbconf
    (let [user-id (sql/with-query-results rs 
                    [(str "SELECT id FROM accounts " 
                          "WHERE username = '" username "' "
                          "AND password = md5('" password "')")]
                    (if (= 1 (count rs)) (:id (first rs)) nil))]
      (if (identity user-id)
        (do (sql/do-commands 
              (str "UPDATE accounts SET last_login_timestamp = (timestamp 'now') WHERE id = " user-id))
            user-id)
        nil))))