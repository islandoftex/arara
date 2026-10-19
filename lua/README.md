# Lua rules

`LuaRuleParser.parse(path, identifier)` reads a Lua script returning a table.
`identifier` must match the requested rule; `name` and at least one command are
required. `authors` and `arguments` are optional lists.

Each command has a `command(ctx)` function, an optional name and an optional
`exit(code)` function (default: `code == 0`). Results are booleans, command handles
or nested lists of either. The context holds processed argument lists and
`reference = { file_name, path, parent }`.

Arguments have a unique `identifier`, optional boolean `required`, a literal
`default`, and/or `flag(ctx, value)`. Flag contexts contain raw `parameters` and
`reference`; flags return strings or lists. `files` and `reference` are reserved.
Helpers accept both camelCase and snake_case spellings. Paths and commands are
opaque handles; reference fields are strings.

`RuleCommand` describes a command. Kotlin implementations with a directly
executable lambda implement `ExecutableRuleCommand`; interpreted commands use
`core.rules.CommandEvaluator`.
