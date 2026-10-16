+++
title = "DevLab Theme v0.2.0"
description = "A foundation release with a calmer design, server-first navigation and a stable theme contract."
date = 2026-07-13

[taxonomies]
tags = ["release"]

[extra]
release = "v0.2.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.2.0` establishes the foundation for a maintainable Zola-native documentation theme without adding a frontend toolchain.

## Highlights

- A calm surface system replaces the experimental liquid-glass treatment.
- The documentation workspace now uses a full-height sidebar, readable article column and compact table of contents.
- Previous and next documentation links are rendered by Zola and work without JavaScript, including across nested sections.
- Mobile navigation manages focus, keeps keyboard navigation inside the drawer and restores focus after closing.
- Search exposes an accessible combobox and listbox contract with live result feedback.
- Template extension hooks allow custom metadata, CSS, scripts and body integrations without copying `base.html`.
- Structured `extra.devlab` settings become the primary configuration contract while `0.1.x` flat fields remain supported.
- Blog pagination, release badges and published or updated metadata turn Blog into a practical Updates channel.
- Copy feedback is announced to assistive technology and interface transitions respect reduced-motion preferences.

## Compatibility

Existing `0.1.x` configuration continues to work. New sites should use the structured groups documented under `extra.devlab`.

The DevLab `v0.2.0` templates can build with Zola `0.22.0` when a site uses `config.toml` or passes `--config zola.toml` explicitly. This repository and its documentation use `zola.toml`, whose automatic discovery starts with Zola `0.22.1`. The supported native workflow is therefore Zola `0.22.1` or newer: `zola check` followed by `zola build`.
