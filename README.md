# re-select

selectize for reagent

[![Clojars Project](http://clojars.org/re-select/latest-version.svg)](http://clojars.org/re-select)

## Usage

```clojure
(require '[re-select.core :refer [selectize]])

[selectize {:options (for [[name abbr] us-states] {:value abbr :label name})
            :multi true
            :max-items 3
            :value initial-value-in-atom
            :on-change (fn [value] (prn value))}]
```

## License

Copyright © 2015 Feng ZHOU

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
