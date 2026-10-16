+++
title = "Interface controls"
description = "Search shortcuts, result navigation and color modes."
weight = 7
updated = 2026-10-06
+++

## Search shortcuts

[Enable search](@/docs/reference/configuration/search.md), then use `Ctrl+K`
on Windows/Linux or `Cmd+K` on macOS to focus the header field.

| Key | Action |
| --- | --- |
| `ArrowDown` | Select the next result. |
| `ArrowUp` | Select the previous result. |
| `Enter` | Open the selected result. |
| `Escape` | Close results; press again to clear the query and leave search. |

Result links also work with the mouse. On small screens the shortcut hint is hidden.

## Search states

An empty focused field prompts for a query. A completed query with no matches
shows the no-results message. A missing or unusable library/index shows the
unavailable message.

These messages use the active [interface catalog](@/docs/reference/languages.md#interface-catalog):
`search_start`, `search_no_results` and `search_unavailable`.

## Color modes

The header button cycles through `system → light → dark → system`.

| Mode | Behavior |
| --- | --- |
| `system` | Follows the device's `prefers-color-scheme`. |
| `light` | Uses the light palette. |
| `dark` | Uses the dark palette. |

A saved browser preference takes priority over the configured default.
[Appearance settings](@/docs/reference/configuration/appearance.md) control the
initial mode and button visibility. [Brand and colors](@/docs/customization/branding.md)
explains palette overrides.

## Header separator

On the homepage, Blog and Downloads layouts, the line below the header appears after scrolling and disappears when you return to the top. Docs, Reader and ordinary content pages keep the line visible from the start. With JavaScript disabled, the line stays visible on every page.

## Reduced motion

With `prefers-reduced-motion: reduce`, navigation animations and the drawer's
sliding transition are disabled.
