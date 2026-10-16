+++
title = "Badges"
description = "Status labels and semantic colors."
weight = 6
+++

Badges display short status labels inside text or headings.

Release {{<devlab.badge text="Stable" tone="success" />}}
API {{<devlab.badge text="Beta" tone="info" />}}
Migration {{<devlab.badge text="Required" tone="warning" />}}
Version {{<devlab.badge text="Unsupported" tone="danger" />}}
Label {{<devlab.badge text="Metadata" />}}

{% raw %}
```jinja
Release {{<devlab.badge text="Stable" tone="success" />}}
```
{% endraw %}

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `text` | Yes | - | Visible badge text. |
| `tone` | No | `neutral` | `neutral`, `info`, `success`, `warning`, or `danger`; invalid values fall back to `neutral`. |

Tone communicates supporting status, but the visible text must carry the meaning on its own. Do not rely on color to distinguish states.
