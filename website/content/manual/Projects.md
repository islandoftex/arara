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

Scripts receive `user.home`, `user.name` and `cwd`, as in
[Lua configuration](@/manual/Configuration.md#lua-configuration). For project
specifications, `cwd` is the invocation's selected working directory. Configuration
scripts receive their project's resolved working directory instead. Each load has
fresh globals; variables and changes to `user` do not carry between scripts.

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
  directives specified in the file. Omit this field to read the file's directives;
  an empty list does not fall back to its header. They have to correspond to the
  file type's directives, i.e. they will be accepted if they would be accepted by the
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
`priority` must be a 32-bit integer. Every project requires at least one file
and a nonblank name, whether supplied explicitly or derived from its filename.
A list of projects requires an explicit, nonblank, unique name for each entry.
Errors identify the offending field.

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

Specifications and loose inputs may be combined:

```sh
arara figures.proj.lua book.proj.lua appendix.tex
```

Loose inputs form one anonymous project, displayed as `Single files`, which runs
after all specification projects. It uses the invocation directory and its
local configuration. Specification dependencies can name only specification
projects. When loose inputs are present, `Single files` is reserved as their
project name. Repeated loose inputs and paths shared with a specification are
errors, including aliases found through extension completion. Command-line
options override each project's local configuration; configuration does not
leak between projects.

# Build directories

A project can compile in a separate directory, keeping generated files out of
its source directory:

```lua
return {
  files = { ["book.tex"] = {} },
  build = { directory = "build/book" }
}
```

The build directory resolves against `working_directory`. Configuration is
still loaded from the source directory. Rules receive references to staged
files, and commands run in the build directory. Outputs remain there; arara
does not copy them back or add tool-specific output options.

By default, arara stages the source tree, respecting `.gitignore` files in each
directory and the source root's `.dockerignore`. An exclusion in either format
wins. Git metadata and project build directories are always excluded. Compilation
targets are always included, even when ignored. Inputs are staged after the
project's dependencies finish, so earlier projects may generate them.

To select inputs explicitly, provide `inputs`. Compilation targets are included
in addition to the listed files, directories and globs; ignore files do not
filter explicit selections. An empty list stages only compilation targets.
Globs are relative to the source directory: `*` and `?` match within a path
segment, character classes such as `[a-z]` match one character, and `**` matches
across directories. A selection that matches nothing is an error.

```lua
build = {
  directory = "build/book",
  inputs = {
    "chapters/**",
    "figures",
    "*.bib",
    { source = "../shared/styles", destination = "styles" }
  }
}
```

Inputs outside the source directory require a `source`/`destination` mapping.
To add mappings while keeping automatic source-tree selection, put the same
mapping tables in `build.mappings` and omit `inputs`.
Destinations must stay within the build directory. Conflicting mappings and
paths that would overwrite an existing output are errors. Projects cannot share
or nest build directories, and a build directory cannot contain a source directory.

Symbolic links are preserved by default and rebased to their staged targets,
including links originally written as absolute paths. Targets must also be
selected; external targets require an explicit mapping. Set `symlinks = "copy"`
in `build` to copy target contents instead. Cycles are errors in both modes.
Creating preserved links requires operating-system support and permission.

The console reports validation, preparation and cleanup. Styled output shows
live feedback when animations are enabled and the terminal supports it. The
preparation result reports the number of files actually copied and the elapsed
time; diagnostic logs include these measurements as well.

Only new or changed inputs are copied on subsequent runs. Missing staged copies
are restored, and inputs deleted or excluded from the source are removed from
the workspace. Generated outputs are retained. Staging uses its own records in
arara's change database format and does not update the state used by `changed()`.
Do not edit the `.arara-workspace` directory inside the build directory.

To remove the workspace, including generated outputs, without compiling:

```sh
arara --clean-build project.proj.lua
```

Only a directory marked as belonging to that project can be cleaned. A later
run recreates it and stages its inputs again. An existing, nonempty directory
without a matching marker is never adopted. Dry runs map the execution paths
without creating or updating the workspace; rules that inspect staged contents
therefore need an existing workspace.

## Continuous compilation

Use `--watch` with one or more Lua project specifications to compile immediately
and rebuild when their inputs change:

```sh
arara --watch book.proj.lua
```

Ordinary file arguments and `--clean-build` cannot be combined with `--watch`.
Each project watches its source tree, compilation targets, build-directory
mappings and additional `watch.paths`. External paths are allowed. Shared inputs
trigger every project watching them; dependencies are rebuilt before their
dependents. Independent projects retain their previous results.

```lua
return {
  name = "book",
  files = { ["book.tex"] = {} },
  watch = {
    paths = { "../bibliography" },
    include = { "figures/**/*.pdf" },
    exclude = { "drafts/**" }
  }
}
```

Paths resolve against `working_directory`. Globs use slash-separated paths
relative to that directory; inside an additional external directory they are
relative to that external directory. Git and Docker ignore files apply by default,
including ignore files in additional watched directories.
Known generated files, including PDFs, auxiliary files and tool logs, are
excluded; `include` overrides these defaults and ignore files. `exclude` takes
precedence over `include`, but compilation targets always remain watched.
Build directories, Git metadata, arara's logs and change databases are never
watched as source inputs. Watch selections do not change which inputs are staged
in a build directory. Exclude additional generated source files to avoid rebuild
loops when tools write into the source tree.

Content changes, additions and deletions trigger builds; changing timestamps
alone does not. Watch state is separate from the database used by `changed()`.
Symbolic links are followed with cycle detection. Broken links are reported,
and watching continues so they can be repaired.

`--watch-debounce` sets the settling delay in milliseconds (default: 300).
Changes during a build are collected for a subsequent build; builds never
overlap. Failed projects block their dependents while independent projects
continue. Watching remains active after failures.

Project specifications, configuration lookup locations and rule directories are
also watched. Changes reload the complete graph and trigger a full build.
Invalid specifications or configuration pause new builds until corrected;
a running build finishes with its original settings.

`--watch-mode` selects `auto` (default), `native` or `polling` monitoring.
Automatic mode falls back to polling if native notifications are unavailable.
All modes reconcile file contents periodically; `--watch-interval` sets the
maximum wait between scans in milliseconds (default: 1000).

The first Ctrl+C stops scheduling builds and lets the current build finish.
A second Ctrl+C interrupts active tools. On exit, arara summarizes the build
cycles. A graceful exit succeeds if every project last built successfully and
configuration remains valid; repaired failures do not make the exit fail.
