+++
title = "DevLab Theme v0.1.1"
description = "A small hotfix for documentation navigation."
date = 2026-07-03

[taxonomies]
tags = ["release"]

[extra]
release = "v0.1.1"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.1.1` is a small hotfix for documentation navigation.

## Before

Documentation groups had to be listed by hand in section frontmatter. Nested folders could exist in `content/docs/`, but the sidebar, breadcrumbs and previous/next links did not understand them as one navigation tree.

## After

The docs navigation now follows the Zola section structure automatically.

Create a folder with `_index.md`, add pages inside it, and the theme will render it as an expandable group. Nested sections work the same way, and breadcrumbs plus previous/next links follow the same tree.

No manual page list is needed for regular documentation navigation.
