+++
title = "File tree"
description = "Files and expandable directories."
weight = 5
+++

File trees explain a project layout without depending on hand-drawn connector characters. Folders use the browser's native disclosure control, so they remain collapsible without JavaScript.

{% <devlab.file_tree label="Theme structure"> %}
{% <devlab.folder name="devlab-theme" open={true}> %}
{{<devlab.file name="theme.toml" note="Theme metadata" />}}
{% <devlab.folder name="templates" open={true}> %}
{{<devlab.file name="base.html" />}}
{% <devlab.folder name="components" open={false}> %}
{{<devlab.file name="core.html" note="URL and icon helpers" />}}
{{<devlab.file name="content.html" note="Public content components" />}}
{% </devlab.folder> %}
{% </devlab.folder> %}
{% <devlab.folder name="sass" open={false}> %}
{{<devlab.file name="main.scss" />}}
{% </devlab.folder> %}
{{<devlab.file name="zola.toml" note="Demo configuration" />}}
{% </devlab.folder> %}
{% </devlab.file_tree> %}

{% raw %}
```jinja
{% <devlab.file_tree label="Theme structure"> %}
{% <devlab.folder name="devlab-theme" open={true}> %}
{{<devlab.file name="theme.toml" note="Theme metadata" />}}
{% <devlab.folder name="templates" open={true}> %}
{{<devlab.file name="base.html" />}}
{% <devlab.folder name="components" open={false}> %}
{{<devlab.file name="core.html" note="URL and icon helpers" />}}
{{<devlab.file name="content.html" note="Public content components" />}}
{% </devlab.folder> %}
{% </devlab.folder> %}
{% <devlab.folder name="sass" open={false}> %}
{{<devlab.file name="main.scss" />}}
{% </devlab.folder> %}
{{<devlab.file name="zola.toml" note="Demo configuration" />}}
{% </devlab.folder> %}
{% </devlab.file_tree> %}
```
{% endraw %}

Component parameters:

| Component | Parameter | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `devlab.file_tree` | `label` | No | `File tree` | Visible caption for the complete structure. |
| `devlab.folder` | `name` | Yes | - | Folder name. |
| `devlab.folder` | `open` | No | `true` | Initial state of the native disclosure control. |
| `devlab.file` | `name` | Yes | - | File name. |
| `devlab.file` | `note` | No | Empty | Short annotation aligned with the file. |

Place `devlab.folder` and `devlab.file` directly inside a tree or another folder. Folder and file names use `dir="auto"`, so localized names remain readable inside both LTR and RTL pages.
