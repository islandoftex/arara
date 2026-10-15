# The website assembly

The arara documentation lives once, in `docs/`, and two pipelines
read it there. The LaTeX build turns the chapters into the typeset
PDF book; `assemble-website.py` turns the very same chapters into
the website's manual pages. One source, two books, never forked.

## What the script does

The chapters already carry their Zola front matter — titles,
descriptions and reading weights, in the canonical order of
`docs/tex/arara-manual.tex` — so the lift is nearly verbatim. Three
transforms, each applied only to the assembled copies, reconcile
the chapters with the web:

1. **Literal braces neutralized.** Zola 0.23 runs every page through
   Tera before parsing the Markdown, so arara's own `{{ … }}`
   interpolation delimiters — which the chapters document — would be
   read as template expressions. Each run is wrapped in a Tera raw
   block and renders literally.
2. **Headings demoted.** The chapters typeset internal sections as
   level-1 headings, which the PDF maps onto the section hierarchy
   beneath each chapter title. On the site the page title is the only
   H1, so every heading drops one level, fence-aware so code blocks
   never confuse the walk.
3. **Nested callouts freed.** Callouts nested inside list items emit
   block-level divs that, under the render-then-parse model, tear the
   surrounding list apart. Every nested callout moves to the top
   level, its inner structure intact.

The sources are never touched: the PDF pipeline keeps its typeset
nesting and heading levels, the repository's browsable Markdown keeps
its pristine delimiters, and the whole story lives in this one
script — the designated seam between the two pipelines.

## Usage

Point the script at any checkout of the site:

    python3 scripts/assemble-website.py --site <path-to-the-site>

The `--site` argument names the Zola checkout — the directory
containing `config.toml` — and is always given explicitly, so the
script presumes nothing about the caller's layout. The `--repo`
argument defaults to the repository this script lives in. In CI,
the `pages` job runs the same script against the fetched website
checkout, so the local site and the deployed site are always built
from identical hands.

## The contract

The outputs — `content/manual/`, `content/quickstart.md` and
`content/changelog.md` in the website worktree — are generated files:
gitignored on the website branch, never committed, rebuilt from
`docs/` on every run. The sources stay the single truth; run the
script whenever `docs/` changes and the site follows the manual.
