+++
title = "Callouts"
description = "Notes, warnings and tips in Markdown."
weight = 9
+++

Callouts support `info`, `warning`, `error`, and `tip` variants.

{% <devlab.callout type="info"> %}
**Note:** DevLab requires Zola 0.23.6 and tracks the current Zola release.
{% </devlab.callout> %}

{% <devlab.callout type="warning"> %}
Restart `zola serve` after changing settings that affect the whole site.
{% </devlab.callout> %}

{% <devlab.callout type="error"> %}
Do not publish a site that has not passed `zola check`.
{% </devlab.callout> %}

{% <devlab.callout type="tip" title="Fast feedback"> %}
Use `zola serve` while writing and keep the production build in CI.
{% </devlab.callout> %}

{% raw %}
```jinja
{% <devlab.callout type="tip" title="Fast feedback"> %}
Use `zola serve` while writing.
{% </devlab.callout> %}
```
{% endraw %}

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `type` | No | `info` | Visual variant and fallback title. |
| `title` | No | Empty | Custom title; replaces the type label. |

An unknown `type` falls back to `info`. `title` is rendered exactly as authored: the theme does not change word casing, so localized titles keep their capitalization.
