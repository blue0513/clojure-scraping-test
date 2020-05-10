(ns clojure-scraping-test.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def target-url "https://clojure.org/")
(def website-content
  (html/html-resource (java.net.URL. target-url)))

(defn get-image-tags []
  (html/select website-content [:img]))

(defn image-paths [img-tags]
  (map :src
       (map :attrs img-tags)))

(defn remove-start-slash [s]
  (if (string/starts-with? s "/")
    (.substring s 1)
    s))

(defn generate-valid-paths [img-tags]
  (map remove-start-slash
       (map string/trim (image-paths img-tags))))

(defn get-image-urls [img-paths]
  (map #(str target-url %) img-paths))

(defn copy-uri-to-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn filename-from [url]
  (last (string/split url #"/")))

(defn execute []
  (let [tags (get-image-tags)
        paths (generate-valid-paths tags)
        target-image-urls (get-image-urls paths)]
    (map
     #(copy-uri-to-file % (filename-from %))
     target-image-urls)))

(defn -main []
  (println (str "target URL is: " target-url))
  (execute))
