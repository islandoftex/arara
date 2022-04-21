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

Instead of passing one or multiple files with directives to the arara
command-line you may pass one `.lua` file (the exact extension is
important) with a project specification. arara will process the Lua script and
then execute all the projects as specified in the Lua script.

# Rationale

Projects have been identified as the sweet spot between arara's already
make-like directive system and actual external specification of directives and
dependencies. Though currently unable to express depdencies between single
files, arara is able to handle dependencies between projects.

TODO: expand on potential use cases

# File structure

A project specification file is a Lua file which contains a script returning a
list/table of project specfications or a project specification itself (if it is
too technical at this point, stay with us, examples will follow). A single
project minimally requires a list of files, so a minimal `project.lua` could
look like

```lua
return {
  files = { ["file.tex"] = { } }
}
```

There are a number of optional parameters:

* `name`: The project's name which is used to identify the project e.g. when
  depending on it. Otherwise, it is used for display purposes on the
  command-line only. If not specified, there will be a placeholder name for
  untitled projects.
* `workingDirectory`: The working directory to execute the project
  in. Specifying a working directory with this option overrides any working
  directory given using the command-line option `-d`/`--working-directory`.
* `dependencies`: Other projects a project depends on. Make sure this project is
  actually specified. Project specification files with invalid dependencies will
  be treated as invalid.

A more complete example showcasing the list of project specifications approach
would be

```lua
return {
  {
    name = "My awesome book",
    workingDirectory = "book",
    files = {
      ["a.mp"] = {
        directives = { "metapost" },
        priority = 1
      },
      ["file.tex"] = { }
    }
  },
  {
    name = "My awesome book vol. 2",
    workingDirectory = "bookv2",
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
  directives specified in the file.
* `fileType`: Specify the file type of the file. The same rules as for
  specifying a file type in the configuration file apply. The `extension` is
  mandatory. If the extension is not among the default set of extensions, a
  `pattern` is mandatory as well.

Apart from the return expression you are free to use Lua code to perform
computations or other actions within the project specification file. Please note
that for technical reasons arara's Lua interpreter only accepts Lua 5.2.
