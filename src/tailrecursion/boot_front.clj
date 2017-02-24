(ns tailrecursion.boot-front
  {:boot/export-tasks true}
  (:require
    [boot.core :as boot]
    [boot.pod  :as pod]
    [boot.util :as util]))

(def ^:private deps
  '[[com.amazonaws/aws-java-sdk-cloudfront "1.11.95"]])

(defn- warn-deps [deps]
  (let [conflict (delay (util/warn "Overriding project dependencies, using:\n"))]
    (doseq [dep deps]
      (when (pod/dependency-loaded? dep)
        @conflict
        (util/warn "• %s\n" (pr-str dep))))))

(defn- pod-env [deps]
  (let [dep-syms (->> deps (map first) set)]
    (warn-deps deps)
    (-> (dissoc pod/env :source-paths)
        (update-in [:dependencies] #(remove (comp dep-syms first) %))
        (update-in [:dependencies] into deps))))

(boot/deftask burst
  [d distribution ID         str "AWS Cloudfront Distribution ID"
   a access-key   ACCESS_KEY str "AWS Access Key"
   s secret-key   SECRET_KEY str "AWS Secret Key"]
  (let [pod (pod/make-pod (pod-env deps))
        out (boot/tmp-dir!)]
    (boot/with-pre-wrap fileset
      (util/info "Invalidating files in the %s cloudfront distribution...\n" distribution)
      (let [uploaded? #(some (fn [[k]] (= (name k) "uploaded")) %)
            files    (boot/by-meta [uploaded?] (boot/output-files fileset))]
        (doseq [{:keys [path]} files]
          (util/info "• %s\n" path))
        (pod/with-call-in pod
            (tailrecursion.boot-front.client/invalidate-files! ~*opts* ~(mapv boot/tmp-path files))))
      fileset)))
