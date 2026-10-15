+++
title = "DevLab Theme v0.7.0"
description = "An authoring release with source-aware Docs actions, synchronized tabs, structured project diagrams and stronger code blocks."
date = 2026-09-01

[taxonomies]
tags = ["release", "documentation", "components"]
authors = ["ripetitor"]

[extra]
release = "v0.7.0"
+++

This article describes its original release. Configuration examples may use
retired settings. For v0.9, follow the [upgrade guide](@/docs/guides/upgrading-v0-8.md)
and the current reference.

DevLab Theme `v0.7.0` focuses on the daily work around technical content. Maintainers can connect a Docs page to its source, writers gain reusable structures for commands and architecture, and readers get clearer code examples without adding a frontend build step to the site.

The new features are additive. Existing `v0.6.0` content continues to render, page provenance remains opt-in and pages that do not use diagrams do not download the diagram renderer.

## Documentation connected to its source

Docs pages can now show an authored update date, copy their current URL and link directly to contribution or issue workflows:

```toml
[extra.devlab.docs.page_actions]
show_last_updated = true
show_copy_link = true
edit_url = "https://codeberg.org/acme/project/_edit/main/content/"
report_url = "https://codeberg.org/acme/project/issues/new"
date_format = "%Y-%m-%d"
```

The update date is rendered only when a page defines Zola's native `updated` field. DevLab does not substitute a build timestamp or publication date. The edit link appends the real source-relative path, while the icon-only Copy link control preserves an active heading fragment when the Clipboard API is available. Editing lives beside the table of contents on wide screens, and the feedback destination closes the article as a dedicated discussion card.

Every action can be enabled independently. Omitting the complete table keeps the previous page footer unchanged.

## Commands that keep their context

Tabs now support an optional synchronization key. Groups sharing the key remember the selected value and update together, which is useful when one guide repeats operating-system, package-manager or deployment choices.

{% <devlab.tabs sync="release-shell" label="Preview command"> %}
{% <devlab.tab name="Unix" value="unix" selected={true}> %}
```bash
zola serve
```
{% </devlab.tab> %}
{% <devlab.tab name="Windows" value="windows"> %}
```powershell
zola.exe serve
```
{% </devlab.tab> %}
{% </devlab.tabs> %}

Without JavaScript, every panel remains readable. With enhancement enabled, the control follows the horizontal tabs keyboard pattern, mirrors arrow behavior in RTL layouts and stores only the selected value.

## Project structure without hand-drawn connectors

The new file-tree components replace fragile ASCII layouts with semantic folders and files. Folders use the browser's native disclosure control and remain collapsible without JavaScript.

{% <devlab.file_tree label="Release workspace"> %}
{% <devlab.folder name="devlab-theme" open={true}> %}
{{<devlab.file name="theme.toml" note="Release metadata" />}}
{% <devlab.folder name="content" open={true}> %}
{{<devlab.file name="docs" note="Product documentation" />}}
{{<devlab.file name="blog" note="Release notes" />}}
{% </devlab.folder> %}
{{<devlab.file name="THIRD_PARTY_NOTICES.md" note="Vendored dependency notices" />}}
{% </devlab.folder> %}
{% </devlab.file_tree> %}

Compact badges complement the tree when a status belongs inside a sentence: Release {{<devlab.badge text="Stable" tone="success" />}} API {{<devlab.badge text="Beta" tone="info" />}} Migration {{<devlab.badge text="Not required" />}}.

Badge text carries the meaning independently of color, and folder or file names use automatic text direction for mixed-language documentation.

## Architecture as authored content

`devlab.diagram` turns a compact Mermaid-compatible source into a responsive SVG that follows the active DevLab palette:

{% <devlab.diagram id="release-authoring-flow" label="Release authoring flow" description="Source content becomes checked documentation and then a tagged release."> %}
flowchart LR
  source["Authored content"]
  check["Zola check"]
  docs["Documentation"]
  release["Tagged release"]

  source --> check
  check --> docs
  docs --> release
{% </devlab.diagram> %}

The self-contained renderer is bundled with the theme and makes no font or CDN request. Its cost is isolated to pages that contain a diagram: approximately 1.6 MB minified or 495 KB gzip. The component provides a visible caption, an accessible description and a source fallback when rendering is unavailable.

Flowcharts, state, sequence, class, ER and XY diagrams are supported. Diagram input should remain trusted, repository-authored content rather than visitor input.

## Code blocks with a deliberate hierarchy

Fenced code now renders as a composed panel with a language toolbar, an icon-only Copy control, clearer line numbers and a visible highlighted-line treatment. Copy feedback changes the icon while retaining localized accessible labels and a polite live announcement.

```rust,linenos,hl_lines=3
fn release(version: &str) {
    check_site();
    publish_tag(version);
}
```

Long lines scroll inside the panel instead of widening the page. Only blocks that actually overflow enter the keyboard tab order, and copied text continues to exclude visual line numbers.

## Upgrading from v0.6.0

1. Update the pinned theme tag to `v0.7.0` using [Install and update](@/docs/getting-started/installation.md) after the tag is published.
2. Keep Zola `0.23.4`. This release does not change the current Zola target.
3. Add `devlab.docs.page_actions` only when the site has real source and feedback destinations.
4. Replace hand-written command switches, file trees or architecture drawings gradually. Existing Markdown does not require conversion.
5. Review site-owned Sass overrides that target code blocks, Docs page footers or component internals.
6. Account for the diagram payload before adding diagrams to frequently visited pages.
7. Run `zola check` and `zola build`, then inspect interactive components with a keyboard and at a narrow viewport.

The complete syntax and parameter contracts live in [Tera components](@/docs/reference/components/_index.md), while page actions are documented in the [configuration reference](@/docs/reference/configuration/reading.md#page-provenance-actions).

## Compatibility

DevLab Theme `v0.7.0` targets Zola `0.23.4`. Theme consumers still build with Zola alone: Node.js and npm are not runtime or site-build requirements, and the checked-in diagram renderer does not depend on a remote service.
