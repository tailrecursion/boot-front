(set-env!
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.8.0"  :scope "provided"]
                    [adzerk/bootlaces    "0.1.13" :scope "test"]])

(require
  '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0-SNAPSHOT")

(bootlaces! +version+)

(deftask develop []
  (comp (watch) (speak) (build-jar)))

(deftask deploy []
  (comp (speak) (build-jar) (push-snapshot)))

(task-options!
 pom  {:project     'tailrecursion/boot-front
       :version     +version+
       :description "Boot task for invalidating AWS CloudFront resources."
       :url         "https://github.com/tailrecursion/boot-front"
       :scm         {:url "https://github.com/tailrecursion/boot-cloudfront"}
       :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"} })
