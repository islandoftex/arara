+++
title = "Reader and chapter order"
description = "Book layouts, section introductions, mobile reading and print."
weight = 4
updated = 2026-10-06
+++

Create `content/book/_index.md`:

```toml
+++
title = "My book"
sort_by = "weight"
template = "reader.html"
page_template = "reader.html"
+++
```

Create a chapter such as `content/book/first.md`:

```md
+++
title = "First chapter"
weight = 1
+++

Write the chapter here.
```

Subsections set `template = "reader.html"` for their introductions. Chapters
inherit the closest `page_template`. Give every chapter a `weight` when its
section uses weight sorting. Zola excludes pages without sorting data.

## Register the book

```toml
[extra.devlab.collections.book]
section = "book/_index.md"
title = "My book"
```

Add `{ collection = "book" }` to the existing navigation array for a header link.

The automatic sequence is the root introduction, its directly sorted pages,
then each subsection's introduction and descendants. Transparent sections do
not add their own introduction. The same sequence drives the chapter list and
Previous/Next links, including links between a section and a page.

## Choose an explicit order

After creating the listed sections and chapters, set their complete sequence:

```toml
[extra.devlab.collections.book]
section = "book/_index.md"
reading_order = [
  "book/_index.md",
  "book/first.md",
  "book/part/_index.md",
  "book/part/chapter.md",
  "book/last.md",
]
```

Entries are content source paths, not URLs. They must be unique and belong to
the collection. Missing primary-language entries fail the build. Missing
translations are omitted, leaving a valid sequence for each language.
An empty list restores automatic order. Explicit order controls the Reader
layout. Ordinary Docs keeps its documentation tree navigation.

## Reading controls

Reader inherits the [collection options](@/docs/reference/collections.md),
including breadcrumbs, TOC, pagination and page actions. A compact inline TOC
appears below 1181px. Edit remains available below the text when the desktop TOC
is hidden. These controls also work on regular documentation pages.

`--reader-width` controls the maximum reading-column width. Its fallback is
48rem. The `print.css` stylesheet removes navigation and controls and wraps
code when printing any page. Printing includes all tab variants with their labels and removes the tab controls
and announcement banner. Use the browser's Print action to print a chapter.
