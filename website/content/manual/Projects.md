+++
title = "Projects"
description = "Projects"
weight = 6
+++

{% messagebox(title="Compatibility information") %}
The project feature has been introduced in version 7 of arara and is still
experimental. It will not vanish in the next versions but it might receive major
changes based on user feedback. If you have certain use cases or ideas for
projects, please open an issue in our
[repo](https://gitlab.com/islandoftex/arara).
{% end %}

Pass one or more `.proj.lua` files to execute project specifications. Ordinary `.lua`
files are regular inputs, using a configured file type. A malformed `.proj.lua`
specification fails explicitly; it is never scanned for directives.

{% messagebox(title="Migration from arara 7 to arara 8") %}
In arara 7, a single `.lua` argument was tried as a project specification and
fell back to ordinary input on any parsing failure. In arara 8, rename
`project.lua` to `project.proj.lua`, change `workingDirectory` to
`working_directory`, and change `fileType` to `file_type`. The old field names
are rejected. An unnamed single project takes its specification's basename
(`project` in this example); projects in a list still require explicit names.
{% end %}

# Rationale

Projects have been identified as the sweet spot between arara's already
make-like directive system and actual external specification of directives and
dependencies. Though currently unable to express dependencies between single
files, arara is able to handle dependencies between projects.

We have built projects with the following use cases in mind:

* You have dependencies between files and want to avoid abusing the `files`
  parameter of arara's directives.
* You want to compile multiple documents whenever you compile and want to avoid
  specifying all of them on the command-line.
* You want to use specific settings for some file but not for others
  (e.g. another pattern for the same file type).

However, the basic idea is to be explicit about what belongs to a project and
what does not. In that sense, a declarative notion of a project spanning
multiple files is what should be covered by this feature.

# File structure

A project specification file is a Lua file which contains a script returning a
list/table of project specfications or a project specification itself (if it is
too technical at this point, stay with us, examples will follow). A single
project minimally requires a list of files, so a minimal `project.proj.lua` could
look like

```lua
return {
  files = { ["file.tex"] = { } }
}
```

There are a number of optional parameters:

* `name`: The project's name which is used to identify the project e.g. when
  depending on it. Otherwise, it is used for display purposes on the
  command-line only. If not specified, the name is derived from the specification filename.
* `working_directory`: The working directory to execute the project
  in. Specifying a working directory with this option overrides any working
  directory given using the command-line option `-d`/`--working-directory`.
  Local configuration is also loaded from this directory for that project.
* `dependencies`: Other projects a project depends on. Make sure this project is
  actually specified. Project specification files with invalid dependencies will
  be treated as invalid.

A more complete example showcasing the list of project specifications approach
would be

```lua
return {
  {
    name = "My awesome book",
    working_directory = "book",
    files = {
      ["a.mp"] = {
        directives = { "% arara: metapost" },
        priority = 1
      },
      ["file.tex"] = { }
    }
  },
  {
    name = "My awesome book vol. 2",
    working_directory = "bookv2",
    files = { ["file.tex"] = { } },
    dependencies = { "My awesome book" }
  }
}
```

Note how the second project depends on the first one and the `a.mp` file has
additional parameters. For files, there are the following additional options:

* `priority`: An integer value that specifies a priority within the project. The
  default priority is `0`. If a file *A* has higher priority than file *B* it
  means that *A* will be executed before *B*.
* `directives`: Specify a list of directives that will be used instead of the
  directives specified in the file. They have to correspond to the file type's
  directives, i.e. they will be accepted if they would be accepted by the
  patterns of the file type.
* `file_type`: Specify the file type of the file. The same rules as for
  specifying a file type in the configuration file apply. The `extension` is
  mandatory. If the extension is not among the default set of extensions, a
  `pattern` is mandatory as well.

Apart from the return expression you are free to use Lua code to perform
computations or other actions within the project specification file. Please note
that for technical reasons arara's Lua interpreter only accepts Lua 5.2.

Project fields are checked strictly: unknown keys and values of the wrong type
are errors. `directives` and `dependencies` must be contiguous lists of strings;
`priority` must be a 32-bit integer. A list of projects requires an explicit,
nonblank, unique name for each entry. Errors identify the offending field.

Relative working directories resolve against the invocation directory (`-d` when
provided). File paths resolve against their project's working directory. An
omitted `file_type` uses that project's configured file types, including compound
extensions; an explicit `file_type` takes precedence. Unknown types fail before
execution. Files may be generated by an earlier project and need not exist when
the specification is read.

Dependencies must name existing projects and must not form cycles. Invalid
graphs fail before any project executes. Independent projects are considered
in name order. Files run by descending priority, with ties ordered by normalized
absolute path.

Multiple specifications share one dependency graph, so a dependency may name a
project in another specification, including one that returns a single project.
Names must be unique across the invocation. Each normalized absolute file path
may belong to only one project; repeated paths within a specification are also
errors. This identity is lexical: symbolic links are not resolved. All graph
and ownership checks finish before execution.

```sh
arara figures.proj.lua book.proj.lua
```

For multiple runs of one file, list the directives in execution order:

```lua
return {
  files = {
    ["book.tex"] = {
      directives = {
        "% arara: pdflatex: { synctex: no }",
        "% arara: pdflatex: { synctex: yes }"
      }
    }
  }
}
```

The file occurs once; its directives run in the order given.
