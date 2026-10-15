# Lunr language support

Unmodified German, Russian and shared stemmer modules from
[MihaiValentin/lunr-languages](https://github.com/MihaiValentin/lunr-languages/tree/41efd0e54bfd12e7ddee841ef9d80092d6aa7853),
revision `41efd0e54bfd12e7ddee841ef9d80092d6aa7853` (upstream package version 1.22.0).

License: MPL-1.1. See `LICENSE` and the notices in each source file. The original
sources stay here for attribution and source availability. The minified browser
files are in `static/vendor/lunr-languages/`. These are language
plugins for Zola's generated Elasticlunr engine, not another search engine.
The small `js/search-language-support.js` adapter supplies the stop-word factory
these modules expect from the generated engine. German and Russian search load
the shared support and their own language module on first interaction. English
needs no additional module. Other languages provide their own ordered script list.

## Recorded downloads

| File | SHA-256 |
| --- | --- |
| `lunr.stemmer.support.js` | `4b4c8cc2fd10dc37e4b33b57fc73659f8df833803c7d04bd84e53f01b16add86` |
| `lunr.ru.js` | `f0a30138f02e6d3909f9c9348f2befa8d1b606cd2f0506ae7bc0907d3831405d` |
| `../../static/vendor/lunr-languages/lunr.stemmer.support.min.js` | `5ff7b6bd53462911fc1311c09f654085b748338aced249e719c0b16fda0b90fb` |
| `../../static/vendor/lunr-languages/lunr.ru.min.js` | `a6d6a514a1acd902fab619e8ba55ff90ceb5cece128b60c0b6199de58bfc2acd` |
| `lunr.de.js` | `82d32012024b7aaed5ae11d4cf9780181e9dea70ba006e104beed2f80c7910ff` |
| `../../static/vendor/lunr-languages/lunr.de.min.js` | `9682b22880003b6e5a4211448f1317d55236a9b891cce6624647e111e8bf82e0` |
| `LICENSE` | `1a2c0a1e111a34ce11f56d6b7ee6270efe616dfaa90c5858312d5052e5778d72` |
