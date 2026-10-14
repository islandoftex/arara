+++
title = "Writing rules"
description = "The Lua rule format, from a first simple rule to the typed helper boundary"
weight = 4
+++

Now that the vocabulary is in place, it is time to write our own rules. This
chapter teaches the Lua rule format — the format the official rules ship in,
the format the future speaks — from a first minimal rule to the full helper
surface, with the strictness the engine enforces on every rule it reads.

# Your first rule

Let us begin with the smallest rule that can honestly work. Suppose we want
a rule that greets the world through the terminal. Create a file named
`hello.lua` — the filename must match the identifier we intend to use — and
write:

```lua
return {
  identifier = "hello",
  name = "The hello rule",
  commands = {
    {
      name = "The greeting",
      command = function(ctx)
        return get_command("echo", "Hello, world!")
      end
    },
  },
  arguments = {}
}
```

The rule returns a table with five keys, and four of them are already at
work: the `identifier` says the rule answers to the name `hello`, the
`name` is what arara displays, the single subtask builds a command through
the `get_command` helper, and the empty `arguments` list says the rule
expects nothing from the user. A file carrying the directive is all it
takes:

```tex
% arara: hello
Hello world.
\bye
```

```sh
$ arara hello.tex
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

arara 8.0.0, the cool TeX automation tool.

Processing "hello.tex" (size: 20 B, last modified: 2026-10-09 10:12:45), please
wait.

(Hello) The greeting ................................................. SUCCESS

TOTAL SUCCESS · 1 file, 1 task

Elapsed time: 0.0521 seconds
```

Hurrah, the rule works! Now let us make it earn its keep — a rule that
greets the world is charming, but a rule that greets a *name* is useful.
Arguments enter the stage.

# The rule table

The rule is a Lua table with five keys, and the engine is strict about all
of them: an unknown key is a parse error, a wrong type is a parse error, and
a missing mandatory key is, unsurprisingly, a parse error. The engine tells
you which key and why, so writing rules is a conversation rather than a
guessing game. The keys, one by one:

- **`identifier`** — the rule's unique name, matched by directives. Lowercase
  letters without spaces, accents or punctuation, as good practice demands,
  and the filename follows it: `hello` lives in `hello.lua`. When arara
  cannot find a rule, the error names the file it was looking for, as seen
  in [Important concepts](Concepts.md).

- **`name`** — the task's display name, shown enclosed in parentheses in the
  console output.

- **`authors`** — an optional list of strings, so the console can name whom
  to blame when a task fails. One author is a list with one element; no
  authors is an empty list or the key left out entirely.

- **`commands`** — the list of subtasks. Each element is a table with up to
  three keys:

  - `name`, optional: the subtask's display name, shown after the task name.
    A subtask can go unnamed, but naming your subtasks is good manners — an
    unnamed subtask renders with the label arara reserves for shy tasks.

  - `command`, mandatory: the function that answers what the subtask does.
    It receives a context table and may answer in three shapes, covered in
    the next section.

  - `exit`, optional: the override for the exit status check, covered in
    [Important concepts](Concepts.md) — a function receiving the actual
    code and answering whether it counts as success.

- **`arguments`** — the rule's expected parameters, possibly an empty list.
  Each argument is a table with up to four keys:

  - `identifier`, mandatory: the argument's name, matched by the
    corresponding parameter in a directive. Two names are reserved and
    cannot be used: `files` and `reference`.

  - `flag`, the function evaluated when the user provides the value. It
    receives the context table and the actual value, and answers with the
    flags the value should produce.

  - `default`, a plain string used when the user does not provide the
    value — written as if the user had typed it in the directive, and then
    forwarded to the `flag` function for evaluation like any other value.

  - `required`, a boolean switch: when true, arara errors if the directive
    does not provide the value. When false — or absent — the argument is
    optional.

  {% <devlab.callout type="info" title="At least one of the two"> %}
  Both `flag` and `default` look optional, but an argument needs at least
  one of them — an argument with no handling at all is a parse error, as it
  makes no sense. `default` without `flag` is valid: the value goes straight
  to the command context as a single-element list.
  {% </devlab.callout> %}

The `flag` function's return value follows a simple flattening rule: a
list is flattened with every element turned into a string, a single string
becomes a one-element list, and any other value becomes its string
representation. The command context always sees a list of strings.

# The command function

The `command` function is where the work happens. It receives a context
table — `ctx` in the examples — carrying the file under `ctx.reference` and
every argument value under its identifier, and it answers in one of three
shapes:

- **A command object**, obtained through `get_command` or
  `get_command_with_working_directory`: the subtask runs it as a system
  command, with the exit status checked afterwards. This is the shape most
  rules answer with.

  ```lua
  command = function(ctx)
    return get_command("pdflatex", ctx.reference.file_name)
  end
  ```

- **A boolean**, when the subtask computes rather than runs: `true` counts
  as success, `false` fails the run. No system command is executed.

  ```lua
  command = function(ctx)
    return true
  end
  ```

- **A list mixing both**, when the subtask must decide how many actions it
  takes: arara executes each element in order, checking every command and
  every boolean on the way.

  ```lua
  command = function(ctx)
    return { get_command("echo", "one"), get_command("echo", "two") }
  end
  ```

The `ctx.reference` value is a *file handle* — an opaque object carrying the
file's path, with the members the path model exposes: `file_name` for the
name, `parent` for the containing directory, `exists`, `is_directory` and
`is_regular_file` for the queries, and the operations `normalize`,
`resolve`, `resolve_sibling`, `read_text`, `read_lines` and `write_text`
among friends. Helpers that take a file accept the handle or a plain string,
so `get_basename("hello.tex")` and its handle twin do the same work.

# The helper surface

The engine binds a family of helpers into every rule, each under its
historic camel case name and its Lua-idiomatic snake case alias — `getCommand`
and `get_command` are the same helper, and the official rules ship in the
snake case spelling. The boundary is typed: a helper that expects a string
will refuse a number with Lua's own complaint — `bad argument #2 to
'session.put' (value expected, got nil)` — the same device Lua's standard
library uses, so the errors read familiar.

The helpers, organized by family:

## Commands and system access

- `get_command(argument, ...)` builds a command object from the arguments,
  to be run in the current working directory. It is the bread and butter of
  rule writing.

- `get_command_with_working_directory(directory, argument, ...)` builds the
  same command, to be run in the given directory instead.

- `unsafely_execute_system_command(command)` runs a raw command string and
  answers with the exit code and the captured output. It is exactly what
  the name says — unsafe — and the safe run mode forbids it loudly, as
  covered in [The command line](CommandLine.md).

- `halt(status)` requests the run to stop with the given exit status. The
  status reaches the process unchanged, a handy device for scripted
  workflows.

- `throw_error(message)` raises the message as the rule's failure — the
  polite way for a rule to say it cannot proceed.

## Booleans and the platform

- `is_true(value, whenTrue, whenFalse)` answers `whenTrue` if the value is
  an affirmative word, `whenFalse` otherwise — the classic helper for
  yes-or-no arguments, seen in Concepts turning `shell: yes` into a command
  flag.

- `is_false(value, whenTrue, whenFalse)` is its pessimistic twin.

- `is_empty(value, whenTrue, whenFalse)` and
  `is_not_empty(value, whenTrue, whenFalse)` answer by the emptiness of the
  value. Both helpers take the answers optionally: with no answers, they
  answer a plain boolean.

- `is_windows()`, `is_linux()`, `is_mac()`, `is_unix()` and `is_cygwin()`
  answer the platform questions — either as plain booleans, or as a
  two-argument dispatch answering the first value on the matching platform
  and the second otherwise. The portable rules are written with them:

  ```lua
  command = function(ctx)
    return is_windows(
      get_command("cmd", "/c", "echo", "Hello"),
      get_command("echo", "Hello")
    )
  end
  ```

- `is_verbose_mode()` answers whether the current run asked for the command
  output.

## Strings

- `trim_spaces(string)` removes the spaces from both ends.

- `build_string(prefix, number)` joins a prefix and a number — the helper
  behind rules that number their passes.

- `replicate_pattern(pattern, list)` applies a format pattern over a list,
  the workhorse of the `files` parameter replication.

- `join(list, separator)` joins a list of strings with a separator, and
  `keys(map)` answers the list of a map's keys — two small Lua utilities
  the engine provides for building tidy error messages.

## Files and directories

- `get_basename(path)` answers the file name without its extension.

- `get_filetype(path)` answers the file type the path resolves to, the
  type lookup seen in [Important concepts](Concepts.md).

- `get_original_file()` and `get_original_reference()` answer the file the
  run started with — the pair that keeps a replicated directive honest
  about which file it came from, as covered in Concepts.

- `normalize_path(path)`, `create_directories(path)` and
  `remove_directory(path)` perform the operations their names indicate —
  the first answers a normalized handle, the two others act.

- `write_to_file(path, content)` and `read_from_file(path)` write and read
  text, and `is_subdirectory(path)` answers whether the given directory
  sits inside the working directory of the current project.

- `list_files_by_extensions(directory, extensions, recursive)` and
  `list_files_by_patterns(directory, patterns, recursive)` answer the
  directory listings, by extension or by glob pattern.

- `is_on_path(application)` answers whether the system can find the
  application — a good prelude to a friendlier error than the shell's.

## The session

- `get_session()` answers the run's session table, with the operations
  `get`, `put`, `contains`, `remove` and `clear`. The session holds the
  `environment:` and `arg:` families of keys described in
  [Important concepts](Concepts.md), and the log trails every mutation.

  ```lua
  command = function(ctx)
    local session = get_session()
    if session.contains("arg:builddir") then
      return get_command("pdflatex", "-output-directory=" ..
        session.get("arg:builddir"), ctx.reference.file_name)
    end
    return get_command("pdflatex", ctx.reference.file_name)
  end
  ```

  {% <devlab.callout type="info" title="The dot, not the colon"> %}
  The session's operations are function-valued members of the table —
  plain functions bound to it, not methods carrying a self — so the dot
  call `session.contains(…)` is the correct spelling; no colon is
  needed, and none would work.
  {% </devlab.callout> %}

## Dialogs

Rules can hold a conversation with the person running the tool — a
message, a choice of options, a text input, a dropdown — through the
dialog helpers. The conversation survives every execution mode and
both presentations, as covered in [The console](Console.md): the
plain one renders the classic terminal blocks, and the styled one the
type-tinted dialog boxes, both sharing the same reading semantics.

The first argument of every dialog helper is the message type — an
integer code that drives the visual tone of the box:

| Code | Meaning |
|---|---|
| 1 | error — reporting a condition the rule detected, something wrong that does not stop the run |
| 2 | information — a neutral note about what the rule is about to do, an observation |
| 3 | warning — proceed with caution, a deprecated option, a long task ahead |
| 4 | question — the *I'm asking* type, the natural companion of `show_options`, `show_input` and `show_dropdown` |
| anything else | plain — no tone, just the words |

- `show_message(type, title, text)` shows a message — no answer is
  expected, no answer is returned. It is the polite tap on the
  shoulder: a warning before a long task, a note about a deprecated
  option, a greeting for the person who wrote the rule.

  ```lua
  show_message(3, 'A reminder', 'The draft mode is on.')
  ```

- `show_options(type, title, text, ...buttons)` shows a numbered list
  of buttons and returns the index of the chosen one, starting from
  one. An empty button list offers nothing, so it yields zero. The
  answer is what a rule branches on:

  ```lua
  command = function(ctx)
    local answer = show_options(4, 'Proceed?',
      'Shall I typeset the document?', 'Yes', 'No')
    if answer == 1 then
      return get_command('pdflatex', ctx.reference.file_name)
    end
    return nil
  end
  ```

- `show_input(type, title, text)` shows a text field and returns the
  answer, trimmed of its surrounding spaces. It is the open question —
  a file name, a version number, whatever the rule needs and cannot
  guess:

  ```lua
  command = function(ctx)
    local name = show_input(4, 'File name',
      'Which file shall I typeset?')
    return get_command('pdflatex', name)
  end
  ```

- `show_dropdown(type, title, text, ...elements)` shows a dropdown
  list and returns the index of the chosen one, starting from one.
  Choosing nothing yields zero, and an empty element list offers
  nothing, so it yields zero as well. The dropdown is the polite
  cousin of the options — a longer list, the same conversation:

  ```lua
  command = function(ctx)
    local choice = show_dropdown(4, 'Engine',
      'Which engine shall I use?',
      'pdfTeX', 'LuaTeX', 'XeTeX')
    if choice == 1 then
      return get_command('pdflatex', ctx.reference.file_name)
    elseif choice == 2 then
      return get_command('lualatex', ctx.reference.file_name)
    elseif choice == 3 then
      return get_command('xelatex', ctx.reference.file_name)
    end
    return nil
  end
  ```

{% <devlab.callout type="info" title="Two contexts, one conversation"> %}
The `show_message` helper is a command helper — it acts, so it lives
in the rule context alone. The other three are conditional helpers —
they return answers, so they are available in both the rule context
and the conditional context, the same family as `missing` and
`found`. A rule can ask a question inside an `if` and branch on the
answer.
{% </devlab.callout> %}

## The conditional family

The helpers of *directive conditionals* — `missing`, `changed`, `found`,
`unchanged` and `exists` — live in the conditional context rather than the
rule context, together with the file helpers `to_file(path)` and
`current_file()`. They are the same family Concepts introduced:

```tex
% arara: pdflatex if missing('pdf') or changed('tex')
```

Each helper answers the extension-shorthand form — `missing('pdf')` looks
for the file with the same base name as the current file and the given
extension — or the explicit form with a file handle, as in
`exists(to_file('hello.tex'))`. The `found` helper takes a second
argument, the text to look for inside the file:

```tex
% arara: pdflatex until found('log', 'undefined references')
```

{% <devlab.callout type="info" title="Two contexts, two surfaces"> %}
The rule context and the conditional context each bind their own helper
surface, because they answer different questions: rules act, conditionals
observe. A helper available in one context is not automatically available
in the other — the engine tells you plainly when you reach for a helper
that is not there.
{% </devlab.callout> %}

{% <devlab.callout type="info" title="The old YAML dialect"> %}
Every helper in this chapter has a camel case twin from the deprecated YAML
era — `get_command` was `getCommand`, `is_true` was `isTrue` — and the
YAML rules spell their commands in MVEL, the expression language covered in
[Engines](Engines.md). If you are tending to old rules before retiring
them, [Migration](Migration.md) holds the complete correspondence between
the two dialects.
{% </devlab.callout> %}

# Strictness, honestly

The Lua engine reads rules the way a good colleague reviews code: it
rejects what it does not understand, and it says why. An unknown key in the
rule table is a parse error naming the key. A wrongly typed value is a
parse error naming the expected shape. A helper called with the wrong type
answers Lua's own `bad argument` complaint, naming the helper, the position
and the expected type. None of this is the engine being pedantic — it is
the engine making your mistakes findable, which is the kindest thing a tool
can do for a rule writer.

That is the whole format: a table of five keys, a context, three answer
shapes, and a family of helpers with honest errors. The official rules —
all of them, ported to this format — are the best apprenticeship a rule
writer can ask for, and the next chapter's configuration keys will show you
where arara looks for your rules when they are not beside the tool itself.
