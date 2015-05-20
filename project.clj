(defproject re-select "0.1.0"
  :description "selectize for reagent"
  :url "https://github.com/zweifisch/re-select"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :clojurescript? true
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [reagent "0.5.0"]]
  :profiles {:dev
             {:dependencies [[org.clojure/clojurescript "0.0-3255"]]
              :plugins [[lein-cljsbuild "1.0.5"]]}})
