+++
title = "Search configuration"
description = "Index format, asset paths and language modules."
weight = 4
updated = 2026-10-06
+++

## Search

Enable Zola's JavaScript Elasticlunr index in `zola.toml`:

```toml
build_search_index = true

[search]
index_format = "elasticlunr_javascript"
include_title = true
include_description = true
include_content = true
```

The search field is rendered when index generation and `extra.devlab.search.enabled`
are both enabled. An explicitly different index format removes the control and
its scripts. DevLab uses `search_index.<lang>.js` for the active language.

| Field under `extra.devlab.search` | Default | Purpose |
| --- | --- | --- |
| `enabled` | `true` | Renders the search interface and its loader. |
| `index` | Empty | Overrides the generated JavaScript filename; does not convert index formats. |
| `language_scripts.<lang>` | German/Russian modules for `de`/`ru`; empty for other languages | Ordered local plugin paths for the language's index. |

The engine, language modules, index and adapter load on the first search focus
or keyboard shortcut. [Interface controls](@/docs/guides/interface-controls.md)
describes keyboard navigation. For loading failures, see
[Troubleshooting](@/docs/reference/troubleshooting.md#search-is-missing-or-returns-no-results).

## Search languages

English is built into Zola's browser engine. German and Russian load the shared
stemmer support and their matching language module. The generated index and
runtime pipeline must agree. See [Zola's search documentation](https://www.getzola.org/documentation/content/search/).

For another indexed language, put compatible Lunr modules in the site's
`static/` folder and configure their order:

```toml
[extra.devlab.search.language_scripts]
fr = ["js/lunr.stemmer.support.js", "js/lunr.fr.js"]
```

The engine loads before this list. The language index and search adapter follow
it. A configured list replaces that language's defaults. An empty list disables
its plugin loading, so the index must work without those plugins.

Some languages need additional tokenizers. Consult the upstream
[language modules](https://github.com/MihaiValentin/lunr-languages).
Interface translations and search pipelines are configured separately.
