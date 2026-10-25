+++
title = "Migration from arara 7"
description = "Updating existing rules, configuration and projects"
+++

arara 8 brings Lua rules and configuration alongside the existing YAML/MVEL
workflow. This post covers the changes to consider when updating from arara 7
to the upcoming release.

<!-- more -->

Please consult our [changelog](@/CHANGELOG.md) for the complete list of changes.
The manual describes each feature in detail; the examples below focus on
updating existing rules, configuration and projects.

# Runtime and rule engines

arara 8 requires Java 21 or later. Check the Java installation used by the
launcher, as it may differ from the one selected in an editor.

Lua is now the default rule engine. The official rules have Lua counterparts.
Existing custom YAML/MVEL rules are detected automatically; they are deprecated
and will be discontinued in v9. Each YAML rule used produces a log warning,
and the summary includes a compatibility warning. Set
`warn_deprecated_yaml = false` in Lua configuration or `warnDeprecatedYaml: false`
in YAML configuration to suppress both. Choosing Lua does not require
converting a YAML configuration file, and choosing a Lua configuration does not
change the rule engine.

Directive parameter mappings still use YAML. Their scalar types matter: quote a
value that must remain a string. For example, `"True"` remains text while plain
`True` is a boolean. Integers outside the supported `Long` range remain strings
rather than being truncated. Null parameter values are rejected.

# Custom rules

YAML rules continue to work. Custom rules which relied on the unfinished Kotlin
scripting DSL need to be written in a supported format instead; `.kts` rule
lookup has been removed.

To adopt Lua, translate the rule's expressions and functions as well as its
outer structure. For example, this YAML command:

```yaml
commands:
- command: >
    @{
        return getCommand('pdflatex', options, reference.fileName);
    }
arguments:
- identifier: options
  default: ''
  flag: "@{ return parameters.options; }"
```

has the following Lua counterpart inside the returned rule table:

```lua
commands = {
  { command = function(ctx)
      return get_command("pdflatex", ctx.options, ctx.reference.file_name)
    end }
},
arguments = {
  { identifier = "options", default = "",
    flag = function(ctx, value) return value end }
}
```

Keep the rule's `identifier`, `name` and optional `authors` alongside these
fields. The complete structure is described in [Lua rules](@/manual/Lua.md).
The paired sources in the [official rule pack](@/manual/Rules.md) provide worked
examples for individual tools.

| Lua | YAML/MVEL |
| --- | --- |
| `command = function(ctx) ... end` | Command expression |
| `flag = function(ctx, value) ... end` | Flag expression using `parameters.options` |
| `ctx.options` | Processed `options` in a command |
| `ctx.reference.file_name` | `reference.fileName` |
| `exit = function(code) return code == 0 end` | `exit: value == 0` |
| `get_command`, `throw_error` (camelCase also works) | `getCommand`, `throwError` |

Convert directive conditionals too when switching engines: MVEL's `&&`, `||`,
`!` and `!=` become Lua's `and`, `or`, `not` and `~=`. Option interpolation uses
`{{ ... }}` with Lua instead of `@{ ... }` with MVEL. The expression inside the
tag must use the selected language.

# Dialogs and halt requests

Dialogs now use the terminal. Remove the `laf` configuration key and the old
pixel-width arguments to dialog helpers. Rename `showDropdown` to `showList`
(`show_dropdown` to `show_list` in Lua). Choices are numbered from one; zero
cancels a selection. Closed input reports an error. See
[Dialog boxes](@/manual/Methods.md#dialog-boxes).

Rules must call `halt(status)` to stop the current file. The old session entry
`arara:<file>:halt` is no longer read. A successful halt does not erase an
earlier command failure when execution has continued after errors.

# Configuration and logging

Local configuration takes priority over configuration in the user's home
directory. Within one directory, Lua takes priority over YAML. If both files
exist, check that the selected file contains the settings you intend to use;
they are not merged.

Logging now uses one format setting. Replace YAML `logging: true` with
`logformat: text`, or Lua `log.enabled = true` with `log.format = "text"`:

```yaml
!config
logformat: text
logname: arara.log
```

```lua
return { log = { format = "text", file = "arara.log" } }
```

Use `none` to disable logging or `json` for JSON Lines output. On the command
line, `-l` / `--log` still enables text logging; `--log-format` selects any of
the three formats. Application log labels are English independently of the
console language. See [Logging](@/manual/Logging.md).

The configured database path is now honored in full, including parent
directories. A relative path is resolved against the project's working
directory; an absolute path is used as given. Ensure the parent directory
exists. Existing YAML databases remain readable; they do not need conversion.
See [The file database](@/manual/Database.md).

# Project specifications

Rename an arara 7 project specification from `project.lua` to
`project.proj.lua`. An ordinary `.lua` argument is now an input file, and a
malformed `.proj.lua` file is an error rather than a request to scan for
directives. Rename the fields `workingDirectory` to `working_directory` and
`fileType` to `file_type`; the old names are rejected.

Project names and normalized file paths must be unique across the invocation.
To run several sets of directives on one file, keep one file entry and use its
`directives` list:

```lua
return {
  files = {
    ["main.tex"] = {
      directives = {
        "% arara: pdflatex: { synctex: no }",
        "% arara: pdflatex: { synctex: yes }"
      }
    }
  }
}
```

Each project's working directory selects its local configuration. Projects do
not inherit settings from the previously processed project. See
[Projects](@/manual/Projects.md) for dependencies, priorities and mixed runs.

# Library users

The Kotlin rule API separates interpreted commands from executable Kotlin
commands: programmatic command lambdas belong to `ExecutableRuleCommand`.
`MPPPath.toJVMPath()` and `toJVMFile()` return absolute, normalized paths.
Code that needs a relative spelling should compute that relationship explicitly
rather than relying on the original constructor argument being returned.

The message catalog is no longer part of the public API module. Consult the API
reference for the current interfaces when updating an embedding application;
[arara as a library](@/manual/Library.md) describes the module boundaries.
