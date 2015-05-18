# re-select

selectize for reagent

## Usage

```clojure
[selectize {:options (for [[name abbr] us-states] {:value abbr :label name})
            :multi true
            :max-items 3
            :on-change (fn [value] (prn value))}]
```

## License

Copyright © 2015 Feng ZHOU

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
