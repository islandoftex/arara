+++
title = "Configuration"
description = "Both configuration formats, every key, and where arara looks for them"
weight = 5
+++

arara provides a persistent model of modifying the underlying execution
behaviour or enhancing the execution workflow through the concept of a
configuration file. This chapter provides the basic structure of that file,
in both of its formats, as well as details on the file lookup in the
operating system.

Please note that a configuration file is not required for arara to work. If
you do not specify one, arara will use the default values detailed below. If
you specify a configuration file, it will overwrite the defaults with the
options given, and the parts omitted from the file will follow the default
values.

# File lookup

Our tool looks for the presence of at least one of a set of very specific
files before execution. arara first looks for YAML configuration files, in
the lookup order `.araraconfig.yaml` → `araraconfig.yaml` →
`.arararc.yaml` → `arararc.yaml`. If no YAML configuration file is found at
all, arara then looks for Lua configuration files with the same base names,
i.e., `.araraconfig.lua` → `araraconfig.lua` → `.arararc.lua` →
`arararc.lua`. A directory that happens to carry one of these names is not
a configuration file, and the tool tells you so rather than tripping over
it.

arara provides two approaches regarding the location of a configuration
file, and they dictate how the execution behaves from a user perspective:

- **Global configuration file**: the configuration file lives in the home
  directory of the current user. All subsequent executions of arara will
  read this file and apply the specified settings. This approach has the
  lowest lookup priority: a local configuration always supersedes a global
  counterpart.

- **Local configuration file**: the configuration file lives in the
  working directory associated with the current execution, which can also
  be interpreted as the one relative to the processed file. This approach
  offers a project-based solution for complex workflows — a thesis, a
  book. However, arara must be executed within the working directory, or
  the local lookup will fail. This approach has the highest priority, and
  it always supersedes a global configuration.

{% <devlab.callout type="warning" title="Beware of empty configuration files"> %}
A configuration file should never be empty, otherwise arara will complain
about it. Make sure to populate it with at least one key, or do not write a
configuration file at all. The available keys are described below.
{% </devlab.callout> %}

If the logging feature is properly enabled, arara will indicate in the
corresponding log file whether a configuration file was used during the
execution and, if so, the corresponding canonical, absolute path. Logging
is detailed in [Logging](Logging.md).

# The YAML configuration

The YAML configuration file begins with the `!config` keyword, mandatory on
the first line, and holds the keys described as follows. All of them are
optional: a file with just the `!config` marker and one key is a valid
configuration.

- *(string list)* `paths`: when looking for rules, arara always searches
  the default rule path — the `rules/` directory beside the tool itself.
  This key specifies a list of directories, as plain strings, in which our
  tool should also search for rules. The default path is appended to the
  list, and the search happens from the first to the last element, in
  order.

  ```yaml
  paths:
  - '/home/islandoftex/rules'
  - '/opt/islandoftex/rules'
  ```

  Three variables are available in this context, resolved through orb tags
  — the `@{ … }` expressions of the default engine's expression language,
  covered in [Engines](Engines.md):

  - `user.home` holds the absolute path of the current user's home
    directory: `paths: [ '@{user.home}/rules' ]`.

  - `user.name` holds the current user account name:
    `paths: [ '/home/@{user.name}/rules' ]`.

  - `application.workingDirectory` holds the absolute path of the working
    directory of the current execution: `paths: [
    '@{application.workingDirectory}/rules' ]`.

  {% <devlab.callout type="tip" title="Keep paths inline"> %}
  Do not use folded or literal styles for scalars in a path. The orb tag
  resolution for a path should be kept as simple as possible, so always
  use the inline style.
  {% </devlab.callout> %}

- *(string)* `language` (default: `en`): sets the language of all
  subsequent executions, according to the provided language code. The
  `--language` command line option overrides this setting.

  ```yaml
  language: nl
  ```

- *(integer)* `loops` (default: `10`): redefines the maximum number of
  loops arara will allow for potentially infinite iterations. Any positive
  integer works. The `--max-loops` command line option overrides this
  setting.

  ```yaml
  loops: 30
  ```

- *(boolean)* `verbose` (default: `false`): sets the standing choice for
  whether the command output echoes. The flags negotiate with this choice
  at every invocation: `--verbose` lifts a quiet configuration,
  `--silent` negates a chatty one, and when both are given, the verbose
  request wins.

  ```yaml
  verbose: true
  ```

- *(boolean)* `logging` (default: `false`): activates the logging feature
  as the default behaviour. The `--log` command line option overrides this
  setting.

  ```yaml
  logging: true
  ```

- *(boolean)* `header` (default: `true`): sets whether directive
  extraction reads the header only — the run of blank and matching lines
  that opens the file, covered in [Important concepts](Concepts.md). When
  false, arara reads the entire file. The `--whole-file` command line
  option overrides this setting.

  ```yaml
  header: false
  ```

- *(string)* `logname` (default: `arara.log`): renames the log file,
  according to the provided value, used as given — a plain name lands in
  the working directory, a path can point anywhere on the file system.
  There is no orb tag evaluation here. The log file is written if, and
  only if, logging is enabled for the run.

  ```yaml
  logname: mylog.log
  ```

- *(string)* `dbname` (default: `arara.db`): renames the file database,
  the internal machine-owned cache that remembers which files changed,
  covered in [The file database](Database.md). The value is used as given,
  and the database anchors at the working directory of the run — or the
  working directory of each project, when projects carry their own. There
  is no orb tag evaluation here either.

  ```yaml
  dbname: mydb.db
  ```

- *(string)* `console` (default: `plain`): chooses the console
  presentation — `plain` or `styled`, the two faces covered in
  [The console](Console.md). The `--console` command line option overrides
  this setting, and an unoffered name is an error rather than a silent
  fallback.

  ```yaml
  console: styled
  ```

- *(boolean)* `animations` (default: `true`): silences the live feedback
  of the styled presentation for those who prefer stillness. The key does
  nothing under the plain presentation, which never breathes in the first
  place.

  ```yaml
  animations: false
  ```

- *(string map)* `preambles`: holds the predefined preambles for later use
  with the `--preamble` option. Each map key must be unique, and lowercase
  letters without spaces, accents or punctuation make good keys. Only
  directives, line breaks and conditionals are recognized — a line other
  than a directive is discarded.

  ```yaml
  preambles:
    twopdftex: |
      % arara: pdflatex
      % arara: pdflatex
  ```

  {% <devlab.callout type="tip" title="Literal style, always"> %}
  When defining preambles in the YAML configuration, always use the literal
  style for scalar blocks. The line breaks are significant when parsing
  the string into directive lines, and the folded style will almost surely
  lose them.
  {% </devlab.callout> %}

- *(string)* `defaultPreamble`: names the preamble arara applies even when
  no preamble is requested on the command line. Preambles are resolved at
  execution time, so a local configuration's preambles take precedence
  over global ones.

  ```yaml
  defaultPreamble: twopdftex
  ```

- *(boolean)* `prependPreambleIfDirectivesGiven` (default: `true`): the
  preamble policy. When true, the historical behaviour, the default
  preamble applies to all files. When false, the default preamble spares
  the files that carry directives of their own — an explicit request with
  `--preamble` always applies, policy or not.

  ```yaml
  prependPreambleIfDirectivesGiven: false
  ```

- *(file type list)* `filetypes`: holds the file types arara supports when
  searching for a file name, each entry a table with two keys:

  - `extension`, mandatory: the file extension as a plain string, without
    the leading dot.

  - `pattern`, the regular expression deciding which lines can hold
    directives — optional for the default types, which already carry
    their own pattern, and mandatory for new types and for overrides.
    Regardless of the pattern, the special `arara:` keyword is immutable
    and included by the tool in every lookup.

  ```yaml
  filetypes:
  - extension: ins
  - extension: drv
  ```

  ```yaml
  filetypes:
  - extension: c
    pattern: ^\s*//\s*
  ```

  The lookup order is a single tier: the types you declare come first, in
  the order you declared them, and the built-in defaults follow in their
  own order — the one-tier truth already taught in
  [Important concepts](Concepts.md). A declared type wins over a default
  of the same extension simply by standing ahead of it.

  {% <devlab.callout type="info" title="Do not escape backslashes"> %}
  In YAML, a file type pattern is a plain, literal string — there is no
  need for escaping backslashes as one does in a programming language.
  Character escaping required by the regular expression itself, such as a
  literal dot, still applies. Consult a proper regular expression
  documentation for the details.
  {% </devlab.callout> %}

# The Lua configuration

A configuration file may also be written as a Lua script returning a
table — but mind this from the start: the Lua configuration is *its own
vocabulary*, not the YAML keys spelled in Lua. The names are
Lua-idiomatic, and grouped keys gather what the YAML format spreads
apart. A complete example:

```lua
return {
  paths = { user.home .. "/rules", "/opt/islandoftex/rules" },
  file_types = {
    { extension = "tex" },
    { extension = "texw", pattern = "^\\s*%\\s+" }
  },
  language = "en",
  loops = 10,
  verbose = true,
  header = true,
  database = "arara.db",
  log = {
    enabled = false,
    file = "arara.log"
  },
  console = {
    mode = "plain",
    animations = true
  },
  preamble = {
    default = "twopdftex",
    prepend = "always",
    definitions = {
      twopdftex = { "% arara: pdflatex", "% arara: pdflatex" }
    }
  }
}
```

The correspondences worth teaching by name:

| The YAML surface | The Lua surface |
|---|---|
| `filetypes` | `file_types` |
| `dbname` | `database` |
| `logging` and `logname` | the `log` group: `enabled` and `file` |
| `console` as a plain string, and `animations` | the `console` group: `mode` and `animations` |
| `preambles`, `defaultPreamble` and `prependPreambleIfDirectivesGiven` | the `preamble` group: `definitions`, `default` and `prepend` |

The `paths`, `language`, `loops`, `verbose` and `header` keys carry the
same names in both formats, and the `prepend` policy keeps its two
names: `always`, the historical default, and `without-directives`,
which spares the files that carry directives of their own.

{% <devlab.callout type="warning" title="Mind the backslashes, again"> %}
Unlike YAML, Lua strings do process escapes, so a pattern like
`^\s*%\s+` has to be written as `"^\\s*%\\s+"`. The doubled backslashes
are the classic first scratch of every new Lua configuration — the same
lesson the file type section of [Important concepts](Concepts.md) teaches.
{% </devlab.callout> %}

The Lua format holds one advantage no YAML key can offer: the script runs
with the values `user.home`, `user.name` and `cwd` in scope, so paths can
be computed in plain Lua — `user.home .. "/rules"` needs no orb tag, no
placeholder, no interpolation. In fact, no placeholder expansion applies
to a Lua configuration at all: what the script computes is what arara
uses.

Remember that Lua configuration files are only considered if no YAML
configuration file is found at all — the YAML names always win the
lookup.

Since arara allows several different names for configuration files, two
formats, as well as global and local approaches, it is highly advisable
to run our tool with the `--log` command line option enabled, in order to
easily identify which file was considered for that specific execution. The
logging feature is discussed in [Logging](Logging.md).
