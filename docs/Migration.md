+++
title = "Migration"
description = "Moving from version 7, converting rules, and the deprecation timeline"
weight = 12
+++

If you are reading this chapter with a version 7 workflow on your screen,
welcome — the migration is smaller than it looks. Version 8 changed a
lot under the hood, but it kept its promises to the working user: the
directives are the same, the conditionals are the same, and the old rule
format still runs, warning politely, while you move at your own pace.
This chapter is the map of that move.

# What version 8 changed

The changes a version 7 user will notice, each with its chapter:

- **The invocation is a set of projects** — specifications merge into one
  ordered run, loose files ride as the anonymous project, and every
  project carries its own working directory and configuration. The
  details live in [Projects](Projects.md); the old single-file model is a
  special case of the new one, so existing workflows keep working.

- **The console has two presentations** — the plain silhouette you know
  is the default, and the styled one is new. Nothing to migrate; an
  option to adopt, covered in [The console](Console.md).

- **The file database changed its format** — the memory of `changed()`
  moved from a YAML file to a private record format named `arara.db`, as
  covered in [The file database](Database.md). An old database file is
  not imported and not read: remove it, and the next run rebuilds the
  memory from scratch, every file counting as changed once.

- **The log is always English** — log lines are hardcoded at their sites,
  so a log can be shared and pasted as-is, as covered in
  [Logging](Logging.md). The console still speaks your language.

- **A new rule format arrived** — Lua rules, the format the official
  rules now ship in alongside the YAML ones you know. The YAML format is
  deprecated but fully functional, and the rest of this chapter is about
  moving from it.

# Converting rules

The two rule formats carry the same five keys, so the conversion is a
translation, not a redesign. The YAML shape first, as a reminder:

```yaml
!config
identifier: pdflatex
name: PDFLaTeX
commands:
- name: PDFLaTeX engine
  command: >
    @{
      return getCommand('pdflatex', file)
    }
arguments:
- identifier: shell
  flag: >
    @{
      isTrue(parameters.shell, '--shell-escape', '--no-shell-escape')
    }
```

And the same rule in Lua, the shape taught in
[Writing rules](Rules.md):

```lua
return {
  identifier = "pdflatex",
  name = "PDFLaTeX",
  commands = {
    {
      name = "PDFLaTeX engine",
      command = function(ctx)
        return get_command("pdflatex", ctx.file)
      end
    }
  },
  arguments = {
    {
      identifier = "shell",
      flag = function(ctx, value)
        return is_true(value, "--shell-escape", "--no-shell-escape")
      end
    }
  }
}
```

The correspondence, key by key:

| Version 7, YAML | Version 8, Lua |
|---|---|
| `!config` header | not needed |
| `identifier: foo` | `identifier = "foo"` |
| `commands: [ { command: "@{ MVEL }" } ]` | `commands = { { command = function(ctx) … end } }` |
| `flag: "@{ MVEL }"` | `flag = function(ctx, value) … end` |
| `default: 'value'` | `default = "value"` |
| `exit: expression` | `exit = function(code) … end` |
| `parameters.shell` in MVEL | `ctx.shell` / the `value` parameter |
| `getCommand`, `isTrue`, camelCase | `get_command`, `is_true`, or the same camelCase names — both spellings work |

The official converter ships with the tool — `scripts/yaml2lua.py`, a
Python script that reads a YAML rule and writes its Lua twin:

```sh
$ python3 scripts/yaml2lua.py pdflatex.yaml
```

Every official rule has already been converted this way, so the shipped
pack is the best gallery of what idiomatic Lua rules look like.

{% <devlab.callout type="warning" title="The conditionals differ too"> %}
A rule's *directive conditionals* follow the engine, not the rule file:
under the default engine they speak MVEL with `&&`, `||` and `!`, under
`--engine lua` they speak Lua with `and`, `or` and `not`. The
correspondence table lives in
[Important concepts](Concepts.md), and a conditional written for one
engine will not parse under the other — convert the conditionals along
with the rules.
{% </devlab.callout> %}

# The old project specifications

Version 7 introduced project support with a provisional shape: a single
`.lua` file passed as argument, parsed as a project — and when the parse
failed, quietly treated as an ordinary input file. Version 8 made the
suffix the law: a `.proj.lua` file is *always* a specification, a parse
failure is a hard error, and no fallback quietly reinterprets the file.
The upgrade path is a rename:

```sh
$ mv project.lua project.proj.lua
```

and a decision, which the new law asks you to make explicitly: if the
file was meant as a specification, rename it; if it was meant as a plain
document with directives in it, move it to the ordinary `.lua` type and
let it ride as input. The parse will no longer guess on your behalf —
which is the point.

The specification's keys grew as well: `working_directory` anchors a
project's files, configuration and database; `priority` orders files
within a project; `dependencies` order projects across the run. The full
model lives in [Projects](Projects.md).

# The deprecation timeline

The YAML engine is deprecated, and the tool says so at every use — the
console shows the warning, the log records it, and the deprecation is
scheduled honestly:

- **Today**: the YAML engine is the default, fully functional, with the
  warning at every run. All official rules ship in both formats.

- **The next major version**: the YAML engine is removed, Lua becomes the
  default, and the YAML rules stop loading — the `--engine yaml` flag
  answers with an error instead.

There is no deadline pressure beyond the next major version, but there
is no reason to wait either: the converter is one command, the dialect
correspondence is one table, and a project that moves today reads
quieter logs tomorrow. If you are starting fresh, start in Lua —
[Writing rules](Rules.md) teaches the format the future speaks.
