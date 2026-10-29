+++
title = "Developing your own rule"
description = "A Lua rule development quickstart"
slug = "rule-development"
weight = 12
+++

Welcome back, Peter! Our friend the anteater already knows how to compile a
[document with arara](@/Quickstart.md). Today, he would like to teach arara a
new trick: running a tool through a rule of his own. We will write one Lua rule
and extend it as we go. No previous experience with arara's rule format is
required, though a little familiarity with Lua tables and functions will help.

***Peter:** I have been using the supplied rules. Why would I write another one?*

Perhaps you have a tool that is not in the rule pack, or you want a convenient
set of defaults for your own workflow. We will use PDFLaTeX because its job is
already familiar. The official `pdflatex` rule remains the right choice for
ordinary documents; our `petertex` rule is a small workshop example. You need
arara 8 and a TeX installation providing `pdflatex` to run it.

# A place for our rule

Create a fresh directory with the following files:

```text
rule-workshop/
  arararc.lua
  article.tex
  rules/
    petertex.lua
```

Work from `rule-workshop` when running the commands below. Put this in
`arararc.lua` so arara can find our local rules:

```lua
return {
  paths = { cwd .. "/rules" }
}
```

Here, `cwd` is the working directory supplied to the configuration script.
The configured paths precede the inherited rule paths, so the supplied rules
remain available. We choose a new name, `petertex`, to keep our experiment
separate from the official `pdflatex` rule.

Now put a small document in `article.tex`:

```tex
% arara: petertex
\documentclass{article}
\begin{document}
Hello, Peter!
\end{document}
```

***Peter:** The directive is ready, but arara still needs to know what
`petertex` means, right?*

Exactly! Save the following as `rules/petertex.lua`:

```lua
return {
  identifier = "petertex",
  name = "Peter's PDFLaTeX",
  commands = {
    {
      name = "PDFLaTeX engine",
      command = function(ctx)
        return get_command("pdflatex", "-interaction=nonstopmode",
          ctx.reference.file_name)
      end
    }
  }
}
```

The script returns a table describing the rule. Its `identifier` matches the
name in the directive and the filename `petertex.lua`. The `name` fields label
the rule and its task in arara's output.

The `command` function receives a context, which we call `ctx`. Its `reference`
describes the file selected by the directive. For this document,
`ctx.reference.file_name` is `article.tex`. We pass the executable and each
argument separately to `get_command`; this builds a command handle for arara to
run. By default, exit code zero means success and any other code means failure.

Try inspecting the command first:

```sh
arara --dry-run article.tex
```

A dry run evaluates our rule but does not launch PDFLaTeX. To actually compile
the document, run:

```sh
arara article.tex
```

You should now have `article.pdf`. Congratulations, Peter: that is a complete
rule already!

# Giving the user a choice

***Peter:** I like `nonstopmode`, but sometimes I want PDFLaTeX to stop and ask
about an error. Must I edit the rule every time?*

No need! Arguments translate directive parameters into command-line arguments.
For example, add an `arguments` table alongside `commands`, with this entry:

```lua
arguments = {
  {
    identifier = "interaction",
    default = "nonstopmode",
    flag = function(ctx, value)
      if not contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
        throw_error("Choose batchmode, nonstopmode, scrollmode or errorstopmode.")
      end
      return "-interaction=" .. value
    end
  }
}
```

The default is used when the directive omits `interaction`. The `flag` function
checks the supplied value and turns it into a program argument. A misspelling
produces an error before the command is launched.

In the command function, replace the fixed `"-interaction=nonstopmode"` string
with `ctx.interaction`. The command context contains the **processed argument
lists**, whereas `value` in the flag function is the original value. For
`interaction: errorstopmode`, the processed list contains
`"-interaction=errorstopmode"`. `get_command` flattens that list for us.

Peter can now change the directive without editing the rule:

```tex
% arara: petertex: { interaction: errorstopmode }
```

The plain `% arara: petertex` directive still uses `nonstopmode`.

# Switches, names and extra options

***Peter:** What about switches such as draft mode, or an output name containing
spaces? And do I have to anticipate every option PDFLaTeX might accept?*

Let us add three kinds of argument:

- `draft` is a switch. Its flag returns `-draftmode` when enabled and nothing
  when disabled. Returning an empty list is a convenient way to omit an option.
- `synctex` is another switch. We translate its boolean value into `-synctex=1`
  or `-synctex=0` explicitly.
- `jobname` is an optional string. Its flag is only called when the parameter
  is supplied, because we give it no default. It becomes one `-jobname=...`
  argument, including any spaces in the name.

For less frequently used options, we also accept `options`, a list of strings.
Each string is one program argument. We check the list and its elements rather
than joining them into a shell command.

Here is the complete rule with these additions. Replace `rules/petertex.lua`
with it:

```lua
return {
  identifier = "petertex",
  name = "Peter's PDFLaTeX",
  commands = {
    {
      name = "PDFLaTeX engine",
      command = function(ctx)
        return get_command("pdflatex", ctx.interaction, ctx.draft,
          ctx.synctex, ctx.jobname, ctx.options, ctx.reference.file_name)
      end
    }
  },
  arguments = {
    {
      identifier = "interaction",
      default = "nonstopmode",
      flag = function(ctx, value)
        if not contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
          throw_error("Choose batchmode, nonstopmode, scrollmode or errorstopmode.")
        end
        return "-interaction=" .. value
      end
    },
    {
      identifier = "draft",
      default = false,
      flag = function(ctx, value)
        if type(value) ~= "boolean" then
          throw_error("draft must be true or false.")
        end
        if value then return { "-draftmode" } end
        return {}
      end
    },
    {
      identifier = "synctex",
      default = false,
      flag = function(ctx, value)
        if type(value) ~= "boolean" then
          throw_error("synctex must be true or false.")
        end
        if value then return "-synctex=1" end
        return "-synctex=0"
      end
    },
    {
      identifier = "jobname",
      flag = function(ctx, value)
        if type(value) ~= "string" or trim_spaces(value) == "" then
          throw_error("jobname must be a non-empty string.")
        end
        return "-jobname=" .. value
      end
    },
    {
      identifier = "options",
      default = {},
      flag = function(ctx, value)
        if not is_list(value) then
          throw_error("options must be a list of strings.")
        end
        local count = 0
        for _, option in ipairs(value) do
          if type(option) ~= "string" then
            throw_error("Each option must be a string.")
          end
          count = count + 1
        end
        if count ~= #keys(value) then
          throw_error("options must be a list, not a map.")
        end
        return value
      end
    }
  }
}
```

Lua uses tables for both lists and maps. The final check compares the entries
visited by `ipairs` with all the table's keys, so a named field cannot be
silently ignored as though it were an empty options list.

Try this directive in `article.tex`:

```tex
% arara: petertex: { synctex: true, jobname: 'Peter notes', options: ['-file-line-error'] }
```

It selects SyncTeX output, changes the output basename to `Peter notes`, and
adds PDFLaTeX's file-and-line error reporting. The quotes around `'Peter notes'`
belong to the directive syntax; they are not part of the filename. Do not add
shell quotes inside the string. The rule already keeps `-jobname=Peter notes`
as one argument on its way to the program.

Similarly, `options: ['-output-directory=build notes']` supplies one argument,
not two. Create that output directory before running PDFLaTeX. The rule passes
extra options through unchanged, so the caller is responsible for their meaning
and for any capabilities they enable in PDFLaTeX.

A dry run is useful when trying these variations. It still evaluates Lua rule
code, so it is not a sandbox: keep command-building functions free of file
writes or other side effects.

# One rule, several files

***Peter:** Can I compile two documents with this rule without adding my own
file loop?*

Yes! `files` is a special directive parameter handled by arara itself. Create
two documents named `chapter one.tex` and `chapter two.tex` in the workshop
directory, then use this directive in `article.tex`:

```tex
% arara: petertex: { files: ['chapter one.tex', 'chapter two.tex'], synctex: true }
```

arara applies the rule to each selected file, replacing `ctx.reference` for each
invocation. Do not declare a rule argument named `files` or `reference`; both
names are reserved. Each document keeps its own output basename because we have
not supplied a shared `jobname`.

We can also run the rule twice on the current file, with different arguments:

```tex
% arara: petertex: { draft: true }
% arara: petertex: { synctex: true }
```

The first pass uses draft mode; the second writes the PDF with SyncTeX data.
These are two directives, not two command functions inside our rule.

# Taking the rule home

***Peter:** I think I can adapt this to another tool now! Where should I keep a
rule that I want to use in more than one project?*

Keep a project-specific rule in the project's rule directory. For a personal
collection, put the rules in a directory of your own and add it to the `paths`
setting in your [configuration](@/manual/Configuration.md). There is no need to
modify arara's installed rule pack. When sharing a rule, describe the external
tool it requires, its parameters and defaults, and any special exit codes.

The same command-building pattern works for a script invoked through `texlua`:

```lua
get_command("texlua", "helper.lua", ctx.options, ctx.reference.file_name)
```

Here, `texlua` runs the external script; arara still loads the rule itself as
Lua. For a tool with subcommands, put the subcommand in its own argument slot:

```lua
get_command("tool", "build", ctx.options, "--input", ctx.reference.file_name)
```

Well done, Peter! We have gone from a single command to a reusable rule with
validated parameters, defaults and multiple-file support. The
[Lua rule reference](@/manual/Lua.md) explains the remaining fields, including
custom exit predicates, and [Methods](@/manual/Methods.md) describes the available
helpers. Start small, inspect the command, and add the next option when your
workflow needs it.
