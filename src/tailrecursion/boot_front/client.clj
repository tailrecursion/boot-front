(ns tailrecursion.boot-front.client
  (:import
    [com.amazonaws.auth                      BasicAWSCredentials]
    [com.amazonaws.services.cloudfront       AmazonCloudFrontClient]
    [com.amazonaws.services.cloudfront.model CreateInvalidationRequest Paths InvalidationBatch]))

(defn client [acc-key sec-key]
  (->> (BasicAWSCredentials. acc-key sec-key)
       (AmazonCloudFrontClient.)
       (delay)))

(defn invalidate-files! [{:keys [access-key secret-key distribution]} paths]
  (let [client (client access-key secret-key)
        paths  (-> (Paths.) (.withQuantity (count paths)) (.withItems (mapv (partial str "/") paths)))]
    (->> (str (System/currentTimeMillis))
         (InvalidationBatch. paths)
         (CreateInvalidationRequest. distribution)
         (.createInvalidation @client))))
