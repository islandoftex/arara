+++
title = "Configuration file"
description = "Configuration file"
weight = 4
+++

arara provides a persistent model of modifying the underlying execution
behaviour or enhancing the execution workflow through the concept of a
configuration file. This chapter provides the basic structure of that file, as
well as details on the file lookup in the operating system.

Please note that a configuration file is not required for arara to work. If you
do not specify one, arara will use the default values as detailed below. If you
specify a configuration file, it will overwrite the defaults with the options
given. Parts that have not been overwritten (omitted from the configuration
file) will follow the default values.

{% messagebox(title="Migration from arara 7 to arara 8") %}
Replace YAML `logging: true` with `logformat: text`; use `logformat: none` to
disable logging. Remove the obsolete `laf` key, as dialogs now use the terminal;
see [Dialog boxes](@/manual/Methods.md#dialog-boxes).
{% end %}

# File lookup

arara searches the project's working directory first, then the user's home
directory. Within each directory it checks Lua before YAML, in this order:
`.araraconfig`, `araraconfig`, `.arararc`, `arararc`, each with the selected
format's `.lua` or `.yaml` suffix. Only regular files qualify. The first match
is loaded; an invalid configuration is an error, without falling back to another
file. Configuration format is independent of the `--preferred-engine` rule selector.

{% messagebox(title="Configuration lookup in version 8") %}
Local configuration takes priority over global configuration, and Lua takes
priority over YAML within each directory. Only the first file is loaded;
settings from different configuration files are not merged.
{% end %}

arara provides two approaches regarding the location of a configuration
file. They dictate how the execution should behave and happen from a user
perspective, and are described as follows.

- **Global configuration file:** For this approach, the configuration file
  should be located at `HOME` which is the home directory of the current
  user. All subsequent executions of arara will read this configuration file
  and apply the specified settings accordingly. However, it is important to
  note that this approach has the lowest lookup priority, which means that a
  local configuration, presented as follows, will always supersede a global
  counterpart.

- **Local configuration file:** For this approach, the configuration file should
  be located at `USER_DIR` which is the working directory associated with the
  current execution. This directory can also be interpreted as the one
  relative to the processed file. This approach offers a project-based
  solution for complex workflows, e.g, a thesis or a book. Lookup uses the
  selected working directory, including `--working-directory` and each Lua
  project's `working_directory`. Local configuration takes priority over global
  configuration. Projects load their own settings without inheriting settings
  from the previously processed project.

{% messagebox(title="Beware of empty configuration files") %}
A configuration file must contain a Lua script returning a table or valid YAML.
An empty Lua configuration is written as `return {}`.
{% end %}

If the logging feature is properly enabled, arara will indicate in the
corresponding `arara.log` file whether a configuration file was used during the
execution and, if so, the corresponding canonical, absolute path. Logging is
detailed later on, in [Logging](@/manual/Logging.md).

# Lua configuration

A Lua configuration returns a table. The script receives `user.home`, `user.name`
and `cwd` (the project's working directory). Each load uses fresh globals.
Paths are computed with Lua expressions; YAML `@{...}` placeholders remain literal.

```lua
return {
    paths = { cwd .. "/rules", user.home .. "/arara-rules" },
    file_types = { { extension = "tex" }, { extension = "src", pattern = "^@" } },
    loops = 10,
    verbose = false,
    header = true,
    language = "en",
    database = "arara.yaml",
    log = { format = "text", file = cwd .. "/arara.log" },
    preamble = {
        default = "build",
        prepend = "without-directives",
        definitions = { build = { "% arara: pdflatex", "% arara: pdflatex" } }
    }
}
```

All settings are optional. Omitted scalar settings retain their inherited values;
rule paths and file types precede the inherited lists. Preamble definitions belong
to the current configuration. `preamble.prepend` accepts `always` (the default)
or `without-directives`. Definitions are lists of directive lines.

| Lua key | YAML equivalent |
| --- | --- |
| `paths`, `language`, `loops`, `verbose`, `header`, `animations` | Same name |
| `terminal_width` | `terminalWidth` |
| `file_types` | `filetypes` |
| `database` | `dbname` |
| `log.format`, `log.file` | `logformat`, `logname` |
| `preamble.default`, `preamble.definitions` | `defaultPreamble`, `preambles` |
| `preamble.prepend` | `prependPreambleIfDirectivesGiven` |

Validation errors use the selected language; `--language` also applies while
loading configuration. Field names and script-supplied error text remain literal.

Types are strict: booleans are not strings, loops must be positive integers,
and lists must have contiguous numeric indices starting at one. Unknown keys
are errors, including unknown keys in groups and file type declarations.
File types require a nonblank `extension`; `pattern` may be omitted for built-in
extensions. Database and log paths follow the YAML options described below.

Set Lua `terminal_width = 80` or YAML `terminalWidth: 80` to choose the maximum
console width (at least 20 columns). `--terminal-width 80` overrides configuration
for every project. This wraps arara's messages, not external tool output. Styled
output also respects the terminal's actual width. Without an explicit value,
plain output retains its 65-column width and styled output follows the terminal.

# YAML configuration

The following list describes the basic structure of an arara configuration file
by presenting the proper elements (or keys, if we consider the proper YAML
nomenclature). Observe that elements marked as **[M]** are mandatory (i.e, the
configuration file *has* to have them in order to work). Similarly, elements
marked as **[O]** are optional, so you can safely ignore them when writing a
configuration file for our tool.

- **[M]** `!config`: This keyword is mandatory and must be the first line of a
  configuration file. It denotes the object mapping metadata to be internally
  used by the tool.

- **[O]** *(string list)* `paths`: When looking for rules, arara always searches
  the default rule path, which consists of a special subdirectory named
  `rules/` inside another special directory named `ARARA_HOME` (the place
  where our tool is installed). If no rule is found, the execution halts with
  an error. The `paths` key specifies a list of directories, represented as
  plain strings, in which our tool should search for rules. The default path
  is appended to the list. Then the search happens from the first to the last
  element, in order.

    ```yaml
    paths:
    - '/home/islandoftex/rules'
    - '/opt/islandoftex/rules'
    ```

  There are three variables available in the `paths` context and are described
  as follows (note that MVEL variables and orb tags are discussed in
  [MVEL](@/manual/MVEL.md)). A variable will be denoted by `⋄ variable` in
  this list.

  - `⋄ user.home`: This variable, as the name implies, holds the
    value of the absolute, canonical path of `HOME` which is the home
    directory of the current user, as plain string. Note that the specifics of
    the home directory (such as name and location) are defined by the operating
    system involved.

    ```yaml
    paths:
    - '@{user.home}/rules'
    ```

  - `⋄ user.name`: This variable, as the name implies, holds the
    value of the current user account name, as plain string. On certain
    operating systems, this value is used to build the home directory
    structure.

    ```yaml
    paths:
    - '/home/@{user.name}/rules'
    ```

  - `⋄ application.workingDirectory`: This variable, as the name
    implies, holds the value of the absolute, canonical path of the working
    directory associated with the current execution, as plain string.

    ```yaml
    paths:
    - '@{application.workingDirectory}/rules'
    ```

  Observe that the `⋄ user` and `⋄ application` variables
  actually holds maps. However, for didactic purposes, it is easier to use the
  property navigation feature of MVEL and consider the map references as three
  independent variables. You can use property navigation styles
  interchangeably.

{% messagebox(title="Avoid folded and literal styles for scalars in a path") %}
Do not use folded or literal styles for scalars in a path! The orb tag
resolution for a path in plain string should be kept as simple as possible, so
*always* use the inline style.
{% end %}

- **[O]** *(string)* `language` (default: `en`): This key sets the language of
  all subsequent executions of arara according to the provided language code
  value, as plain string. The default language is set to English. Also, it is
  very important to observe that the `--language` command line option can
  override this setting.

    ```yaml
    language: nl
    ```

- **[O]** *(integer)* `loops` (default: `10`): This key redefines the maximum
  number of loops arara will allow for potentially infinite iterations. Any
  positive integer can be used as the value for this variable. Also, it is
  very important to observe that the `--max-loops` command line option can
  override this setting.

    ```yaml
    loops: 30
    ```

- **[O]** *(boolean)* `animations` (default: `true`): Enables live progress for
  long-running styled tasks on interactive terminals. Set it to `false` to keep
  task output static. Lua configurations use the same key.

    ```yaml
    animations: false
    ```

- **[O]** *(boolean)* `verbose` (default: `false`): This key activates or
  deactivates the verbose mode of arara as default mode, according to the
  associated boolean value. Also, it is very important to observe that the
  `--verbose` command line option can override this setting if, and only if,
  this variable holds `false` as the value. Similarly, the `--silent` command
  line option can override this setting if, and only if, this variable holds
  `true` as the value.

    ```yaml
    verbose: true
    ```

- **[O]** *(string)* `logformat` (default: `none`): Selects `none`, `text` or
  `json`. `none` disables logging. `--log-format` overrides configuration;
  `-l` / `--log` selects text unless an explicit `--log-format` is supplied.

    ```yaml
    logformat: text
    ```

- **[O]** *(boolean)* `header` (default: `true`): This key modifies the
  directive extraction, according to the associated boolean value. If enabled,
  arara will extract all directives from the beginning of the file until it
  reaches a line that is not empty and it is not a comment. Otherwise, the
  tool will resort to the default behaviour and extract all directives from
  the entire file. It is very important to observe that the `--whole-file`
  command line option can override this setting if, and only if, this variable
  holds `true` as the value.

  {% messagebox(title="New default") %}
  Prior to version 7.0 the default has been `false`.
  {% end %}

    ```yaml
    header: false
    ```

- **[O]** *(string)* `logname` (default: `arara`): This key modifies the default
  log file name, according to the associated plain string value, plus the
  `log` extension. The value cannot be empty or contain invalid
  characters. There is no orb tag evaluation in this specific context, only a
  plain string value. The log file is written when the selected format is
  `text` or `json`.

  {% messagebox(title="Log paths") %}
  From version 6.0 on, the log file may now be specified as path *anywhere* on
  the file system. However, keep in mind that this behavior may be altered for
  future updates in safe mode.
  {% end %}

    ```yaml
    logname: mylog
    ```

- **[O]** *(boolean)* `warnDeprecatedYaml` (default: `true`): Emit log and
  summary compatibility warnings when deprecated YAML/MVEL rules are used.
  In Lua configuration, use `warn_deprecated_yaml = false` to suppress them.
  YAML/MVEL rules will be discontinued in v9; YAML configuration remains supported.

- **[O]** *(string)* `dbname` (default: `arara.yaml`): The database file path,
  including its extension. Lua configurations use `database`. Relative paths
  resolve against the project's working directory; absolute paths are used as
  written. The parent directory must exist. No placeholder expansion or extension
  appending is performed.

    ```yaml
    dbname: cache/hashes.yaml
    ```

- **[O]** *(string map)* `preambles`: This key holds a string map containing
  predefined preambles for later use with the `--preamble` option (see
  [Command line](@/manual/CLI.md)). Note that each map key must be
  unique. Additionally, it it is highly recommended to use lowercase letters
  without spaces, accents or punctuation symbols, as key values. Only
  directives, line breaks and conditionals are recognized.

    ```yaml
    preambles:
      twopdftex: |
        % arara: pdftex
        % arara: pdftex
    ```

  {% messagebox(title="Literal style when defining a preamble") %}
  When defining preambles in the configuration file, *always* use the literal
  style for scalar blocks. The reason for this requirement is the proper
  retention of line breaks, which are significant when parsing the strings into
  proper directive lines. Using the folded style in this particular scenario
  will almost surely be problematic.
  {% end %}

- **[O]** *(string)* `defaultPreamble`: This key allows to specify a preamble
  for arara to use even if there are no directives in the file nor preambles
  specified on the command-line. Preambles are resolved at execution time,
  which means that preambles from local configurations will take precedence
  over global preambles.

    ```yaml
    defaultPreamble: twopdftex
    ```

- **[O]** *(boolean)* `prependPreambleIfDirectivesGiven`: This key allows you to
  specify a boolean value indicating whether preambles should be applied to
  all files or only those without directives. It defaults to `true` to avoid
  breaking existing workflows.

    ```yaml
    prependPreambleIfDirectivesGiven: false
    ```

- **[O]** *(file type list)* `filetypes`: This key holds a list of file types
  supported by arara when searching for a file name, as well as their
  corresponding directive lookup patterns. In order to properly set a file
  type, the keys used in this specification are defined inside the `filetypes
  →` context and presented as follows.

  - **[M]** `filetypes → extension`: This key, as the name implies,
    holds the file extension, represented as a plain string and without the
    leading dot (unless it is part of the extension). An extension is an
    identifier specified as a suffix to the file name and indicates a
    characteristic of the corresponding content or intended use. Observe that
    this key is mandatory when specifying a file type, as our tool does not
    support files without a proper extension.

    ```yaml
    extension: c
    ```

  - **[M|O]** `filetypes → pattern`: This key holds the directive
    lookup pattern as a regular expression (which is, of course, represented
    as a plain string). When introducing a new file type, arara must know how
    to interpret each line and how to properly find and extract directives,
    hence this key. Observe that this key is marked as optional and
    mandatory. The reason for such an unusual indication highly depends on the
    current scenario and is illustrated as follows.

    - The `pattern` key is entirely *optional* for known file types (henceforth
      named *default* file types), in case you just want to modify the file name
      lookup order. It is important to observe that default file types already
      have their directive lookup patterns set, which incidentally are the same,
      presented as follows.

      ```
      ^\s*%\s*
      ```

    - The `pattern` key is *mandatory* for new file types and for overriding
      existing patterns for default file types. Make sure to provide a valid
      regular expression as key value. It is very important to note that,
      regardless of the underlying pattern (default or provided through this
      key), the special `arara:` keyword is immutable and thus included by our
      tool in every directive lookup pattern.

      ```yaml
      pattern: ^\s*//\s*
      ```

    For instance, let us reverse the default file name lookup order presented in
    [Command line](@/manual/CLI.md). Since the default lookup patterns will be
    preserved, the corresponding `pattern` keys can be safely omitted. Now it is
    just a matter of rearranging the entries in the desired order, presented as
    follows.

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    - extension: ltx
    - extension: dtx
    - extension: tex
    ```

    If a default file type is included in the `filetypes` list but others from
    the same tier are left out, these file types not on the list will implicitly
    have the lowest priority over the explicit list element during the file name
    lookup, although still respecting their original lookup order modulo the
    specified file type. For instance, consider the following list:

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    ```

    According to the previous example, three out of five default file types were
    deliberately left out of the `filetypes` list. As expected, the two default
    file types provided to this list will have the highest priority during the
    file name lookup. It is important to note that arara will always honour the
    original lookup order for omitted default file types, yet favouring the
    explicit elements. The following list is semantically equivalent to the
    previous example.

    ```yaml
    filetypes:
    - extension: ins
    - extension: drv
    - extension: tex
    - extension: dtx
    - extension: ltx
    ```

    The following example introduces the definition of a new file type to
    support `c` files. Observe that, for this specific scenario, the `pattern`
    key is mandatory, as previously discussed. The resulting list is presented
    as follows, including the corresponding regular expression pattern.

    ```yaml
    filetypes:
    - extension: c
      pattern: ^\s*//\s*
    ```

    Configured file types always come first, in their declaration order;
    the default types follow in their original order. This also applies when
    the configuration contains only new file types. The following list has
    the same lookup priority as the previous example.

    ```yaml
    filetypes:
    - extension: c
      pattern: ^\s*//\s*
    - extension: tex
    - extension: dtx
    - extension: ltx
    - extension: drv
    - extension: ins
    ```

    {% messagebox(title="Do not escape backslashes") %}
When writing a file type pattern, there is no need for escaping backslashes
as one does for strings in a typical programming language (including MVEL
expressions). In this specific scenario, key values are represented as
plain, literal strings.

However, please note that character escaping might be required by the
underlying regular expression in some scenarios (i.e, a literal dot in the
pattern). It is highly recommended to consult a proper regular expression
documentation for a comprehensive overview.
    {% end %}

Since arara allows four different names for configuration files, as well as
global and local approaches, it is highly advisable to run our tool with the
`--log` command line option enabled, in order to easily identify which file was
considered for that specific execution. The logging feature is discussed later
on, in [Logging](@/manual/Logging.md).

### Console settings

`console` selects `plain` (default) or `styled` in Lua and YAML configurations.
`--console plain` or `--console styled` overrides configuration. Styled output uses
terminal colors when available and ASCII markers without ANSI escapes in pipes.
Rule-engine selection is independent of the console presentation.
Styled mode also frames terminal dialogs, shows command details with `--verbose`,
and displays captured output from failed commands when it was not streamed live.
Its closing summary lists successful, failed, and unprocessed files.
