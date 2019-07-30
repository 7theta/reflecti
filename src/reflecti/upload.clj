(ns reflecti.upload
  (:require [utilis.fn :refer [fsafe]]
            [compojure.core :as compojure :refer [GET POST PUT]]
            [ring.middleware.partial-content :refer [wrap-partial-content]]
            [ring.util.response :as response]
            [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.string :as st]))

;;; Declarations

(declare uuid ->filename download upload)

;;; Integrant

(defmethod ig/init-key ::ring-handler
  [_ {:keys [directory filename on-upload endpoint]
      :or {endpoint "/media"
           filename ->filename}}]
  (let [file (io/file directory)]
    (when (not (.exists file))
      (when (not (.mkdirs file))
        (throw (ex-info "Unable to create upload directory" {:directory directory})))))
  (let [endpoint (str "/" (->> (st/split (str endpoint) #"/")
                               (filter seq)
                               (st/join "/")))]
    (fn [app]
      (let [handler (-> app
                        (compojure/routes
                         (GET (str endpoint "/:filename") [filename :as request] (download request filename))
                         (POST endpoint request upload))
                        wrap-partial-content)]
        (fn [request]
          (handler
           (assoc request
                  ::directory directory
                  ::filename filename
                  ::on-upload on-upload)))))))

;;; API

(defn upload
  [{::keys [directory filename on-upload] :as request}]
  (let [file (-> request :params :file)
        {:keys [content-type tempfile]} file]
    (when (#{"video/mp4" "video/ogg" "video/webm"
             "image/png" "image/jpeg"} content-type)
      (let [filename (filename file)
            file-location (->> filename (io/file directory) str)]
        (with-open [os (io/output-stream file-location)]
          (io/copy (io/input-stream tempfile) os)
          ((fsafe on-upload) {:file-location file-location
                              :params (:params request)}))
        {:status 201
         :headers {"Content-Type" "text/plain"}
         :body filename}))))

(defn download
  [{::keys [directory] :as request} file-name]
  (response/file-response (str (io/file directory file-name))))

;;; Private

(defn- ->filename
  [{:keys [content-type filename]}]
  (let [suffix (last (st/split content-type #"/"))]
    (str (uuid) "-" (System/currentTimeMillis) "." suffix)))

(defn- uuid
  []
  (str (java.util.UUID/randomUUID)))
