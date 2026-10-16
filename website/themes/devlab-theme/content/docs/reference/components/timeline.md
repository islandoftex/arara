+++
title = "Timeline"
description = "Events with dates and status labels."
weight = 12
+++

A timeline presents a plan or a bring-up roadmap as stages with a status and an optional date. It reuses the inline badge tones, so status text stays readable without JavaScript.

{% <devlab.timeline> %}
{% <devlab.timeline_item title="Bring-up" status="done" date="2026-08"> %}
UART, U-Boot, kernel, Device Tree, microSD and eMMC.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Hardware validation" status="active" date="2026-09"> %}
Ethernet, USB, Wi-Fi, Bluetooth, PCIe/NVMe, HDMI, audio and sensors.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Preview" status="planned" date="Q4 2026"> %}
Test image with known limitations, SHA-256 and pinned component versions.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Supported" status="planned" label="Release"> %}
Regression matrix on a release candidate before the stable release.
{% </devlab.timeline_item> %}
{% </devlab.timeline> %}

{% raw %}
```jinja
{% <devlab.timeline> %}
{% <devlab.timeline_item title="Bring-up" status="done" date="2026-08"> %}
UART, U-Boot, kernel, Device Tree, microSD and eMMC.
{% </devlab.timeline_item> %}
{% <devlab.timeline_item title="Hardware validation" status="active" date="2026-09"> %}
Ethernet, USB, Wi-Fi and Bluetooth.
{% </devlab.timeline_item> %}
{% </devlab.timeline> %}
```
{% endraw %}

| Component | Parameter | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `devlab.timeline` | - | - | - | List container for stage items; takes no parameters. |
| `devlab.timeline_item` | `title` | Yes | - | Stage name. |
| `devlab.timeline_item` | `status` | No | `planned` | `done`, `active`, `planned` or `blocked`; unknown values fall back to `planned`. |
| `devlab.timeline_item` | `date` | No | Empty | Free-form date or period label such as `2026-09` or `Q4 2026`, rendered as plain text. |
| `devlab.timeline_item` | `label` | No | Status name | Visible badge text; pass a localized or project-specific name. |
| `devlab.timeline_item` | `heading_level` | No | `3` | Heading level for stage names, from 2 to 6. Match it to the surrounding outline. |

Place `devlab.timeline_item` directly inside `devlab.timeline`. Stage bodies render Markdown, and each status is carried by the badge text, so colour is never the only signal. Use `heading_level` when the timeline sits under a heading that is not an `h2`.
