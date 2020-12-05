;;dependency needed: [io.titanoboa/titanoboa "0.9.0"]
;;This is a quick demo with repls - sending messages between two repls using async queues running on top of Rabbit MQ

;;in both REPLs:
(ns user)
(require '[clojure.core.async :as async :refer [>!! <!!]])
(require '[clojure.pprint :refer [pprint]])
(require '[titanoboa.channel :as ch :refer [with-mq-session with-mq-sessionpool current-session *mq-session*]])
(require '[titanoboa.channel.rmq :as channel])

(def connection (.start (channel/->RMQConnectionComponent {:host "localhost"
                                                      :port 5672
                                                      :username "guest"
                                                      :password "guest"
                                                      :vhost "/"
                                                      :connection-name "titanoboa-connection"}
                                                     nil)))

(def session (.start (channel/map->RMQSessionComponent {:connection-comp connection})))

(alter-var-root #'ch/*mq-session* (constantly (:session session)))

(def my-chan (channel/rmq-chan "channel1" true))

;;in REPL1
(>!! my-chan {:message "hello world!"})

;; in REPL2
(<!! my-chan)

;;in REPL1
(def resp-chan (channel/rmq-chan nil true))

(>!! my-chan {:message "here is some work for you" 
                         :fn '(fn [s] (str "Hello " s "!")) 
                         :input "there" 
                         :resp-chan resp-chan})

;; in REPL2
(def message (<!! my-chan))

(pprint message)

(>!! (:resp-chan message) 
       (assoc message :result ((eval (:fn message)) (:input message))))

;;in REPL1
(pprint (<!! resp-chan))
