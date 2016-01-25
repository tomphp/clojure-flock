(defproject cljgl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.lwjgl.lwjgl/lwjgl "2.9.3"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.9.3" :classifier "natives-osx" :native-prefix ""]]
  :main ^:skip-aot cljgl.core
  :target-path "target/%s"
  :jvm-opts [] 
  :profiles {:uberjar {:aot :all}})
