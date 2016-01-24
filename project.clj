(defproject cljgl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.lwjgl/lwjgl "3.0.0b"]
                 [org.lwjgl/lwjgl-platform "3.0.0b" :classifier "natives-osx"]]
  :main ^:skip-aot cljgl.core
  :target-path "target/%s"
  :jvm-opts ["-XstartOnFirstThread"] 
  :aliases {"dumbrepl" ["trampoline" "run" "-m" "cljgl.core/-main"]}
  :profiles {:uberjar {:aot :all}})
