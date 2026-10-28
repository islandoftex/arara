+++
title = "Lua rules"
description = "Lua rule format and expressions"
weight = 12
+++

`arara document.tex` uses Lua rule files and expressions by default.
Lookup searches each configured rule path for `name.lua`, then
`arara-rule-name.lua`, before trying their YAML counterparts in that directory.
`--preferred-engine yaml` reverses the format preference. Each directive uses
its resolved rule's language for conditionals and option interpolation.
Configuration files may use Lua or YAML, independently of the selected engine;
see [Configuration file](@/manual/Configuration.md). Directive parameter maps
still use YAML. Project specifications remain separate `.proj.lua` files.

Rule validation and helper diagnostics use the selected language. Field names,
helper names, Lua type names and errors supplied by scripts remain literal.

# Rule structure

A rule is a Lua script returning a table:

```lua
return {
  identifier = "example",
  name = "Example",
  authors = { "Rule author" },
  commands = {
    { command = function(ctx)
        return get_command("program", ctx.options, ctx.reference.file_name)
      end,
      exit = function(code) return code == 0 end }
  },
  arguments = {
    { identifier = "options", default = {},
      flag = function(ctx, value) return value end }
  }
}
```

The table uses the same basic fields as a YAML rule:

| Field | Meaning |
| --- | --- |
| `identifier` | Rule name, matching the directive identifier |
| `name` | Name displayed while processing the rule |
| `authors` | Optional list of author names |
| `commands` | Non-empty list of command tables |
| `arguments` | Optional list of argument tables |

Save this example as `example.lua` in a configured rule directory. Its identifier
must match the name used in the directive:

```tex
% arara: example: { options: ['--draft'] }
```

Run the document with `arara --preferred-engine lua document.tex`. The program named
`program` in this example must be installed separately, just like the tools used
by the official rules.

# Commands and arguments

Commands return a command handle, a boolean or nested lists of these. `exit` is
optional and defaults to accepting zero. Each processed argument is a list of
strings. `reference` contains string fields `file_name`, `path`, `parent` and
`supplied_spelling`.

A command table requires `command = function(ctx) ... end`; its `name` and
`exit` fields are optional. A returned `true` represents a successful logical
task and `false` a failed one. Command handles invoke an external program. Nested
lists run their tasks in order; an empty list or `nil` contains no tasks.
A command string or number is not an executable result: use `get_command` for
an external program. The `exit(code)` function must return a boolean.

Use `get_command` to build an invocation from a program and its arguments. The
helper flattens argument lists, so the processed `ctx.options` list in the
example does not need to be joined into a shell command. Arguments containing
spaces remain individual arguments. Use `named_command("Cleaning aux", command)`
to give an individual command its own task label. Commands without a label use
the enclosing task name.

Arguments accept a literal `default`, a `flag(ctx, value)` function or both.
`required = true` requires an explicit directive parameter. Flag contexts contain
raw `parameters` and `reference`. Argument identifiers must be unique; `files`
and `reference` are reserved.

The two contexts serve different purposes:

| Context | Available values |
| --- | --- |
| `command(ctx)` | Processed argument lists, such as `ctx.options`, and `ctx.reference` |
| `flag(ctx, value)` | The current raw value, all raw values in `ctx.parameters`, and `ctx.reference` |

For instance, a flag can turn a directive switch into a command option:

```lua
{ identifier = "shell", default = false,
  flag = function(ctx, value)
    return is_true(value, "--shell-escape", "--no-shell-escape")
  end }
```

An omitted optional argument without a default produces an empty processed list;
its flag is not called. With a default, the flag receives that value instead.
An explicit value is required when `required = true`, even if a default is
also declared. Defaults are literal values, not functions. Without a flag, a
provided value or default is converted to one string. Flags may return scalar
values or nested lists; scalars become strings and `nil` produces an empty list.
For example, a flag returning `false` contributes the text `"false"`, whereas a
command returning `false` reports a failed task.

`ctx.reference` describes the file selected for the directive. Its `file_name`
is the name without parent directories; `path` and `parent` are absolute paths.
`supplied_spelling` retains the spelling of the selected path, which may already
be absolute after file resolution.
The special directive parameter `files` can select other references, as described
in [Important concepts](@/manual/Concepts.md).

# Helper methods

Shared helpers have camelCase and snake_case spellings, such as `getCommand`
and `get_command`. Paths returned by `to_file` expose the selected
[MPPPath properties and methods](@/manual/Methods.md#path-objects), such as
`path.exists` and `path:read_text()`. Session
operations use dot calls: `get_session().put("key", value)`.

Helpers validate their input types: `trim_spaces` requires a string, for
instance, and the append argument of `write_to_file` requires a boolean. The
error identifies the helper and the argument that did not match. Helpers taking
integer indices or counts require 32-bit integer values.
The helper reference and Lua examples are in [Methods](@/manual/Methods.md).

# Conditionals and options

Conditionals must return booleans and use Lua's `and`, `or` and `not` operators.
The existing loop limits apply. Options interpolate `{{ expression }}`:

```tex
% arara: example: { options: ['--count={{ 1 + 2 }}'] } if true
```

Scalar options and option lists retain their shape. A nil interpolation result
becomes empty text. Lua and MVEL interpolation tags cannot be mixed. Safe runs
leave options uninterpolated. Dry runs skip conditional evaluation and process
launches; rule scripts and command closures still evaluate.

The conditional operators keep the same execution order in both engines:
`if`, `unless` and `while` test before running a directive; `until` runs the
directive once before testing. For example:

```tex
% arara: pdflatex if missing('pdf') or changed('tex')
% arara: pdflatex unless unchanged('tex') and exists('pdf')
% arara: pdflatex until not found('log', 'undefined references')
```

These expressions apply to directives resolved to Lua rules. Parameter mappings between the rule
name and the conditional are still YAML, even though the conditional is Lua.

# The official rules

Every rule in the [official pack](@/manual/Rules.md) has a Lua counterpart with
the same arguments. Both formats are distributed together. Lua helper scripts
and external tools invoked by individual rules are separate from arara's Luak
rule interpreter.
