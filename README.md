# boot-front
a boot task for invalidating cloudfront paths.

[](dependency)
```clojure
[tailrecursion/boot-front "1.1.0"] ;; latest release
```
[](/dependency)

## overview
this task inspects the fileset's metadata to identify files that were uploaded
by preceeding tasks as indicated by a keyword with the name "uploaded", then
invalidates their corresponding paths (see [boot-bucket](https://github.com/tailrecursion/boot-bucket)).

## usage
excerpt of a build.boot file using boot-front with boot-bucket for deployment.
```clojure
(require
  '[adzerk.boot-cljs          :refer [cljs]]
  '[adzerk.boot-reload        :refer [reload]]
  '[hoplon.boot-hoplon        :refer [hoplon]]
  '[tailrecursion.boot-bucket :refer [spew]]
  '[tailrecursion.boot-front  :refer [burst]]
  '[tailrecursion.boot-static :refer [serve]])

(def buckets
  {:production "<appname>-production-application"
   :staging    "<appname>-staging-application"})

(def distributions
  {:production "<production-cloudfront-id>"
   :staging    "<staging-cloudfront-id>"})

(deftask develop
  "Continuously rebuild the application during development."
  [o optimizations OPM kw "Optimizations to pass the cljs compiler."]
  (let [o (or optimizations :none)]
    (comp (watch) (speak) (hoplon) (target) (reload) (cljs :optimizations o) (serve))))

(deftask build
  "Build the application with advanced optimizations."
  [o optimizations OPM kw "Optimizations to pass the cljs compiler."]
  (let [o (or optimizations :advanced)]
    (comp (speak) (hoplon) (cljs :optimizations o :compiler-options {:elide-asserts true}) (sift))))

(deftask deploy
  "Build the application with advanced optimizations then deploy it to s3."
  [e environment   ENV kw "The application environment to be utilized by the service."
   o optimizations OPM kw "Optimizations to pass the cljs compiler."]
  (assert environment "Missing required environment argument.")
  (let [b (buckets       environment)
        d (distributions environment)]
    (comp (build :optimizations optimizations) (spew :bucket b) (burst :distribution d))))

(task-options!
  serve {:port 3001}
  sift  {:include #{#"index.html.out/" #"<app-ns>/"} :invert true}
  spew  {:access-key (System/getenv "<AWS_ACCESS_KEY_ENV_VAR>")
         :secret-key (System/getenv "<AWS_SECRET_KEY_ENV_VAR>")}
  burst {:access-key (System/getenv "<AWS_ACCESS_KEY_ENV_VAR>")
         :secret-key (System/getenv "<AWS_SECRET_KEY_ENV_VAR>")})
```
