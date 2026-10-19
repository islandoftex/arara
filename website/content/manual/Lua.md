+++
title = "Lua rules"
description = "Lua rule format and expressions"
weight = 13
+++

`arara --engine lua document.tex` selects Lua rule files and expressions.
The default engine remains `yaml`. Lua lookup searches each configured rule path
for `name.lua`, then `arara-rule-name.lua`; it does not fall back to YAML.
Configuration files and directive parameter maps still use YAML. Project
specifications remain separate `.proj.lua` files.

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

Commands return a command handle, a boolean or nested lists of these. `exit` is
optional and defaults to accepting zero. Each processed argument is a list of
strings. `reference` contains string fields `file_name`, `path` and `parent`.

Arguments accept a literal `default`, a `flag(ctx, value)` function or both.
`required = true` requires an explicit directive parameter. Flag contexts contain
raw `parameters` and `reference`. Argument identifiers must be unique; `files`
and `reference` are reserved.

Shared helpers have camelCase and snake_case spellings, such as `getCommand`
and `get_command`. Paths returned by `to_file` are opaque handles. Session
operations use dot calls: `get_session().put("key", value)`.

Conditionals must return booleans and use Lua's `and`, `or` and `not` operators.
The existing loop limits apply. Options interpolate `{{ expression }}`:

```tex
% arara: example: { options: ['--count={{ 1 + 2 }}'] } if true
```

Scalar options and option lists retain their shape. A nil interpolation result
becomes empty text. Lua and MVEL interpolation tags cannot be mixed. Safe runs
leave options uninterpolated. Dry runs skip conditional evaluation and process
launches; rule scripts and command closures still evaluate.

Lua counterparts are available for the TeX engines: `context`, `csplain`, `etex`,
`latex`, `luahbtex`, `lualatex`, `luatex`, `pdfcsplain`, `pdflatex`, `pdftex`,
`platex`, `ptex`, `tex`, `uplatex`, `uptex`, `xelatex` and `xetex`. Their arguments
match the [official rule descriptions](@/manual/Rules.md).

Bibliography, glossary and index rules also have Lua counterparts: `authorindex`,
`bib2gls`, `biber`, `bibtex`, `bibtex8`, `bibtexu`, `makeglossaries`,
`makeglossarieslite`, `makeindex`, `nomencl`, `pbibtex`, `songidx`, `texindy`,
`upbibtex`, `xindex` and `xindy`.
