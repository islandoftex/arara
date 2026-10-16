+++
title = "Tabs"
description = "Tabbed content and keyboard navigation."
weight = 3
updated = 2026-10-06
+++

Tabs group related Markdown or code examples while keeping every panel readable when JavaScript is unavailable. These two groups share `sync="local-shell"`: selecting a platform in either group updates the other and remembers the choice across pages.

{% <devlab.tabs sync="local-shell" label="Development server command"> %}
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

{% <devlab.tabs sync="local-shell" label="Site check command"> %}
{% <devlab.tab name="Unix" value="unix" selected={true}> %}
```bash
zola check
```
{% </devlab.tab> %}
{% <devlab.tab name="Windows" value="windows"> %}
```powershell
zola.exe check
```
{% </devlab.tab> %}
{% </devlab.tabs> %}

{% raw %}
````jinja
{% <devlab.tabs sync="local-shell" label="Development server command"> %}
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
````
{% endraw %}

`devlab.tabs` parameters:

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `sync` | No | Empty | Shared persistence key. Groups with the same key synchronize matching tab values. |
| `label` | No | `Tabs` | Accessible name for the generated tab list. |

`devlab.tab` parameters:

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `name` | Yes | - | Visible tab label. |
| `value` | No | `name` | Stable value used for synchronization and persistence. Values must be unique inside a group. |
| `selected` | No | `false` | Initial tab when no saved selection matches. The first tab is the final fallback. |

Use at least two `devlab.tab` children directly inside `devlab.tabs`. An empty `sync` value keeps the group independent and does not write a preference. A non-empty key stores only the selected `value` in `localStorage`. Explicit values are recommended when labels are translated or may change.

The enhanced control follows the horizontal tabs keyboard pattern: Left and Right move and select, while Home and End jump to the first or last tab. Arrow direction mirrors in RTL layouts.

## Printing

The print stylesheet includes every panel and its label, regardless of the
selected tab. Tab buttons are omitted. Printing does not change the saved
screen selection.
