(ns clj-alekseev-lab.core
    (:require [clojure.core.async :refer [>! <! <!! >!! timeout chan alts! go go-loop close!]]))

(defn channel-to-print [channel]
    (<!! (go-loop []
             (when-let [x (<! channel)]
                 (println x)
                 (recur)))))

(defn timeout-translator [input, output]
    (go-loop []
        (let [[value port] (alts! [input (timeout 1000)])]
            (if (nil? value)
                (if (identical? input port)
                    (do
                        (close! output)
                        nil)
                    (do
                        (>! output :timeout)
                        (recur)))
                (do
                    (>! output value)
                    (recur))))))

(defn -main [& _]
    ;; Напишите функцию перенаправляющие данные из одного канал в другой,
    ;; в случае если данных в канала нет более n миллисекунд
    ;; отправлять в канал для записи значение :timeout (Алексеев
    (let [input-channel (chan 10) output-channel (chan 10)]
        (timeout-translator input-channel output-channel)
        (>!! input-channel 5)
        (Thread/sleep 1500)
        (>!! input-channel 6)
        (>!! input-channel 1)
        (Thread/sleep 1500)
        (>!! input-channel 9)
        (close! input-channel)

        (channel-to-print output-channel)))
