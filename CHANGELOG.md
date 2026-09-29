# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project tries to adhere to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html) from version 5.0.0
on.

> **Important**: This CHANGELOG documents the technical nature of changes.
> While it is written for users, it does not attempt to provide migration
> guides. For these, please read the announcement blog posts on our website
> at https://islandoftex.gitlab.io/arara/news/.

## [Unreleased]

## Added

* New Lua rule format: rules may now be written as Lua scripts returning a
  rule table (see the `lua` module's `LuaRuleParser` documentation). The
  helper methods known from the YAML format (`getCommand`, `isTrue`,
  `throwError`, ...) are available as Lua globals.
* New `-e`/`--engine` command line option to select the rule engine:
  `yaml` (default, deprecated) or `lua`.
* New Lua configuration file format: arara now also accepts
  `.araraconfig.lua`, `araraconfig.lua`, `.arararc.lua` and `arararc.lua`
  files (Lua scripts returning a table with the same keys as the YAML
  format, with compound names spelled in snake_case: `file_types`,
  `db_name`, `log_name`, `default_preamble` and
  `prepend_preamble_if_directives_given`; unknown configuration keys are
  rejected). YAML configuration
  files are looked up first; Lua configuration files are only considered
  if no YAML configuration file is found. A Lua configuration script runs
  with arara's values in scope, so paths and other strings are computed
  in plain Lua: the table `user` carries the `home` and `name` of the
  user, and `cwd` is the working directory the configuration is loaded
  for (bound when the script runs, at load). The `@{user.home}`-style
  placeholders of the YAML format do not apply to Lua configurations.
* All official rules have been converted to the Lua format and are shipped
  alongside their YAML counterparts (`rules/arara-rule-*.lua`).
* Under `--engine lua`, directive conditionals (`if`, `while`, `until`,
  `unless`) are evaluated as Lua expressions. Conditional helper binding
  errors surface behind the same localized evaluation error as the MVEL
  dialect produces for helper failures, with the binding message as the
  diagnostic detail. The condition has to yield a
  proper boolean (like the MVEL dialect), the same conditional helpers
  (`exists`, `found`, `changed`, `toFile`, ...) are available, and the
  engine's decision carries the usual strictness. Single-call conditions
  such as `if exists('log')` work in both dialects unchanged; compound
  conditions use Lua's `and`, `or` and `not` instead of `&&`, `||` and
  `!`. The default YAML engine keeps evaluating conditionals in MVEL.
* Under `--engine lua`, interpolation tags in directive `options` are
  interpolated as Lua expressions using the same helper registry as the
  conditionals. The tags use their own `{{ … }}` delimiters on purpose:
  the `@{ … }` orb tags belong to the deprecated YAML engine, and using
  them under the Lua engine reports an error pointing at the Lua syntax
  instead of producing literal text. Expressions may contain braces of
  their own, such as table constructors. Safe mode still skips
  interpolation entirely, and unterminated or malformed tags report the
  same interpolation error as the MVEL dialect.
* Under `--engine lua`, the helper boundary is typed: helper arguments
  have to match the declared type and mismatches raise Lua-style
  `bad argument #<n> to '<helper>'` errors behind the usual localized
  wrappers. Lua's implicit coercions apply to operators, not to
  arara's library functions; convert explicitly with `tostring` or
  `tonumber`. Numbers no longer silently become strings (`exists(123)`
  used to look for extension `123`), the `halt` exit code has to be a
  number, and a rule's `exit` closure has to return a proper boolean
  (returning the raw exit code made every code pass). Interpolation and
  flag results still stringify by design, and the named conversion
  helpers (`isTrue` and friends) keep interpreting values.
* Rule and project files of the Lua format are parsed strictly:
  wrongly typed entries fail loudly at parse time instead of being
  silently coerced, defaulted or dropped. Numbers no longer pass as
  strings (rule names, authors, file keys), the `required` key has to
  be a boolean, project file specifications and project list entries
  have to be tables, and directive and dependency entries have to be
  strings — while absent optional entries keep their documented
  defaults. The project parser also discriminates a single project
  from a project list by shape (a root `files` table) instead of
  retrying the failed single parse as a list, so every parse error
  now reports the actual violation.
* Under `--engine lua`, rule evaluation failures now surface through
  the same localized runtime errors as the YAML engine: command, flag
  and `exit` closures report their runtime failures behind the
  localized command, flag and exit errors, and a non boolean `exit`
  closure result reports the same localized error the YAML engine
  produces, with the arara-generated helper errors (such as the bad
  argument errors) available as the diagnostic detail. Author-authored
  `throwError` messages keep flowing raw in both dialects.
* Under `--engine lua`, files cross the helper boundary as file
  references: a file stored via the session comes back as a file
  reference instead of degrading into a path string, so conditional
  helpers check the file itself rather than interpreting the path as
  an extension (matching the object semantics of the YAML engine).
* Under `--engine lua`, rule commands and flags can use the session:
  `getSession()` exposes the same table of operations (`get`, `put`,
  `contains`, `remove`, `clear`) as directive conditionals, closing the
  gap to MVEL rules which always had session access.
* The notice printed when a provided Lua file fails to parse as a
  project specification is now localized; the parse details remain
  available in the log.
* The rule errors of the Lua engine now share the localized header with
  the YAML engine, unsupported rule file formats report a new localized
  message, and the previously untranslated project file message is now
  available in all supported languages.
* The log file (enabled via `-l`/`--log`) now records the selected rule
  engine, the exit value and duration of every system command,
  conditionally skipped directives and halt requests, and its line
  timestamps follow a locale-independent numeric format.
* The published Maven modules now ship the conventional artifact set:
  every library module publishes a sources jar alongside its main jar.
  The documentation travels through the aggregated HTML site instead
  of a javadoc jar: the javadoc format is bound to the JVM-only
  publishing era, and the unified site covers every module at once.
* The generated documentation now aggregates all modules into one
  unified arara site (next to the per-module sites), stamped with the
  Island of TeX footer.
* The public documentation is complete: every public member of every
  module is documented, and the documentation generation fails on any
  warning - a documentation gap breaks the build the same way a
  compiler warning or a lint finding does.

## Changed

* Replaced `luak` by `luaj`.
* The internal machinery is renamed for what it is: the terminal
  rendering surface is `Display` (was `DisplayUtils`), the directive
  pipeline is `DirectiveProcessing` (was `DirectiveUtils`), the
  configuration file lookup is `ConfigurationFiles` and the built-in
  defaults are `DefaultConfiguration` (both were `ConfigurationUtils`),
  and the log backend setup is `Logging` (was `LoggingUtils`). The
  names change nothing for rule authors; embedders importing the
  internal classes should rely on the `api` module, which keeps its
  stable surface.
* The interactive helpers (`showMessage`, `showOptions`, `showInput`
  and `showDropdown`, in both engines) render as terminal dialogs
  instead of Swing windows: wrapped blocks with a type tag, numbered
  entries, and range prompts, all honoring the configured terminal
  width. The choice interpretation asks again on input outside the
  offered range; a closed input while a numbered dialog waits is
  reported instead of guessed at. arara owns the terminal geometry:
  rules choose the type, the title, the text and the choices, and the
  terminal decides how they are laid out.
* The CLI module is no longer published to the Maven repository: it is
  the application and ships as the runnable jar, so the four library
  modules (`api`, `core`, `lua` and `mvel`) alone publish.
* Replaced the archived kaml YAML parser with kotaml, its supported
  fork (`io.heapy.kotaml`); the fork keeps the original package, so the
  API is unchanged. The stricter reader of the new version accepts all of
  arara's rules and configuration files without modification.
* Replaced the dormant yamlkt parser with kotaml as well, unifying all
  YAML handling (rules, configuration, directive parameters and the file
  database) on a single, actively maintained library. Directive
  parameters are now parsed through kotaml's YAML node model and
  converted to the plain values the interpreter expects; quoting always
  yields strings, `yes`/`no` remain strings (they are not YAML 1.2
  booleans), and null parameter values keep being rejected.
* Kotlin platform updated to 2.4.20, Gradle updated to 9.7.1, build
  plugins updated (shadow 9.6.1, spotless 8.10.2, versions 0.64.0 and
  Dokka 2.2.0, including the migration to the Dokka Gradle plugin v2
  configuration) and dependencies updated (kaml 0.104.0,
  kotlin-logging 8.0.4, kotlinx-serialization 1.11.0, mvel2 2.5.4.Final).
* The build has been migrated from Kotlin Multiplatform (with JVM-only
  targets) to plain Kotlin/JVM; source sets moved from
  `src/commonMain`/`src/jvmMain` to `src/main` (and tests accordingly).
* animate+convert rules: change default program from `convert` to `magick` 
  (`convert` was deprecated in ImageMagick v7)
* Message formatting no longer mangles values containing `$` or
  backslashes: the sequential `%s` replacer now inserts replacements
  verbatim, and it also accepts non string values like the underlying
  formatter.
* The Lua project parser's default name for nameless projects aligns
  with the command line's fallback (`Untitled`).
* A scalar `options` value in a directive is no longer silently
  replaced by an empty list when orb tag interpolation runs: values
  keep their shape, so a scalar option stays a scalar in normal runs
  exactly as it does in safe run mode.
* Error causes without a message of their own (such as a bare
  `NullPointerException`) now earn a details section in the terminal
  output showing the cause by its string representation, instead of
  the whole details section being silently omitted.
* A rule storing a non numeric value under the halt session key now
  reports a localized error instead of crashing with a raw number
  parsing exception.
* The safe-run mode now also rejects the class loading helpers
  (`loadClass` and `loadObject`), closing an asymmetry with the
  system command helper: loading arbitrary classes from user-provided
  archives was the more powerful escape hatch, yet it worked in safe
  mode while system commands did not.
* Classes loaded from user-provided archives can now resolve
  companion classes from the same archive: the class loader no longer
  closes once the class is loaded. Each load operates on a private
  temporary copy of the archive, so the user's file is never held
  open — also when arara runs as a long-lived library, a daemon or a
  server.
* The path model now rests on a multiplatform path algebra
  internally. On case-sensitive platforms the behavior is unchanged
  for all ordinary spellings — the reference semantics (identity,
  I/O and display) are identical, and the class keeps its public
  surface. Degenerate spellings narrow visibly: redundant separators
  and interior `.` segments collapse at construction, leading `..`
  on absolute paths is consumed eagerly, and the empty spelling now
  denotes the current directory — which also makes empty `PATH`
  entries resolve to the working directory during system command
  lookup, the POSIX-correct behavior.
* On Windows, path equality, hashing and ordering become
  case-sensitive: case-variant spellings of the same location were
  equal before and are distinct now, which affects the file database
  keys. Mixed-separator spellings such as `C:/x` are re-spelled with
  backslashes before parsing on that platform, so they keep their
  Windows shape under the new algebra.
* Symlinked files keep their follow-links semantics for existence,
  type, size and time queries: the new backend reads metadata
  without following links, and the model resolves the chains itself.
* Directory search results now come in a deterministic, sorted
  order — the platform listing order was arbitrary before. A search
  target that is itself a regular file is still returned as its own
  single match, and links to files keep matching like the files they
  point to.
* The command line no longer prints the logging facade's one-time
  startup announcement before its own output: the modernized facade
  announces the backend it detects once per run by default, and the
  entry point now turns the announcement off before any logger
  exists, restoring the quiet output surface the tool always had.
* Command execution failures now report the underlying exception
  itself as the diagnostic detail, instead of a string encoding of it;
  the terminal error output for failed commands is unchanged, and the
  exception details are preserved more faithfully.
* The message catalog properties for database load and save failures
  were renamed from their historical XML-era names
  (`ERROR_LOAD_COULD_NOT_LOAD_XML` and `ERROR_SAVE_COULD_NOT_SAVE_XML`)
  to format-neutral ones ending in `_DATABASE`, matching the messages
  they have always carried.
* The shared rule helpers (`RuleMethods`, `ConditionalMethods`,
  `MethodUtils`), the shared configuration model (`LocalConfiguration`,
  `SerialFileType`) and the interpreter exception moved from the MVEL
  module to the core and API modules; the Lua engine no longer depends
  on the MVEL module at all (version 9 groundwork, see `ROADMAP.md`).

## Deprecated

* The YAML/MVEL rule format and YAML configuration files are deprecated
  and will be removed in a future version; arara logs a warning when YAML
  rules are used. Please migrate to the Lua rule and configuration
  formats. (Directive options and conditionals are unaffected.) The
  removal playbook is documented in `ROADMAP.md`.

## Removed

* **Breaking:** The Swing dialogs are gone. The interactive helpers
  render on the terminal only, so scripts that relied on dialog
  windows, and headless runs that met them, see different behavior by
  design: a dialog is a conversation with the person at the terminal,
  and the safe mode rejects one headlessly.
* **Breaking:** The `width` parameter is removed from the interactive
  helpers (`showMessage`, `showOptions`, `showInput`, `showDropdown`),
  together with their four-argument call variants: the parameter was
  the pixel width of a Swing window, the terminal ignores it, and
  arara owns the terminal geometry. The common three-argument calls,
  such as `showMessage(1, 'Greetings', 'Hello')`, keep working; rules
  passing an explicit width fail at evaluation, and the fix is
  dropping the first argument.
* **Breaking:** The `laf` configuration key and the look and feel
  option are removed with the Swing dialogs. Both configuration
  parsers reject unknown keys, so configurations still carrying
  `laf:` (YAML) or `laf = ...` (Lua) fail with an unknown key error;
  remove the entry from the configuration file.
* **Breaking:** The message catalog is redesigned and leaves the public
  API module. Messages are now typed members of an internal interface -
  zero-argument messages as values, parameterized messages as functions
  with typed, named parameters - with one implementation per locale.
  The compiler enforces what the old catalog could not: a locale that
  misses a message fails the build instead of silently falling back to
  English, and a wrong argument is a compile error instead of runtime
  garbage. Translations may order their arguments freely; the positional
  placeholder machinery is gone. Console messages keep their exact
  texts; the change is invisible to users.
* **Breaking:** The twelve log message constants are removed from the
  public message class. Log lines are always English now, hardcoded
  at their sites, regardless of the configured language: log files are
  shared diagnostic artifacts, so they read the same on every machine
  and can be pasted as-is. Console output and error texts remain
  localized.
* Removed the experimental Kotlin DSL module (`kotlin-dsl`): the CLI
  never wired it up and its `.kts` rule format was never implemented, so
  the default YAML engine could discover `.kts` rule files that then
  crashed; `.kts` rule files are no longer part of the rule lookup.
* Removed the packaging and documentation build tasks (CTAN/TDS trees
  and zips, the documentation build and the `roast` bytecode check) and
  the `docs` Gradle module: the documentation workflow requires external
  tools (zola, weasyprint, htmlq) that are unavailable in this
  environment, and the packaging tasks depended on it. The manual sources
  remain in `docs/`.
* Removed the `korlibs` bundle.

## Fixed

* Every published jar now declares its own automatic module name
  (`org.islandoftex.arara.<module>`); previously all modules shared the
  same module name, which put two arara jars on the Java module path
  in conflict.
* YAML rules declaring a reserved argument identifier (`files` or
  `reference`) or repeating an argument identifier are now rejected
  as documented: the validation compared the argument objects
  against identifier strings, so both checks could never fire and
  such rules were silently accepted.
* Text reads that fail at the system level - a missing file, a
  directory, an unreadable file - now surface as arara's own I/O
  exception, as the path layer always documented, instead of crashing
  with a raw system exception. Three intended behaviors come alive
  with it: the `readFromFile` helper falls back to an empty list (its
  probe semantics), the `found` conditional reports the localized
  rule error, and an unreadable input file reports the extraction
  error.
* The `buildString` rule helper joins its arguments as documented: a
  missing spread operator previously collapsed all arguments into a
  single array element, so rule code evaluated through the MVEL engine
  received an unreadable array dump instead of the joined string. The
  Lua engine was unaffected, as its helper binding applies the spread.
* Project specifications whose dependencies contain a cycle now report a
  proper error naming the projects involved, instead of silently
  skipping them (Kahn's algorithm omits cyclic vertices from the
  topological order, so cyclic projects were simply not executed).
* Project specifications (`.lua` files) now honor configuration files:
  previously the configuration was loaded only for single file
  invocations, so rule paths, file types, language and the other
  settings were silently ignored when running a project.
* Error messages for YAML rules report the directory the rule is located
  in, consistent with the Lua engine and the interpreter's own reports;
  previously they printed the full rule file path in the location field.
* Malformed glob patterns in the `listFilesByPatterns` helper now produce
  a friendly arara error naming the pattern instead of a raw pattern
  syntax exception deep inside a rule evaluation.
* MVEL flag evaluations returning lists that contain null values no
  longer crash with a null pointer exception; null entries are skipped,
  mirroring the behaviour of the Lua engine.
* The MVEL helper registry now fails loudly when a helper method cannot
  be wired, instead of silently dropping every helper listed after the
  broken entry.
* The `loadClass`/`loadObject` rule helpers no longer leak a class loader
  (and thereby a file handle) on every invocation.
* Interactive rule dialogs now honor look-and-feel settings from the
  configuration file even when the dialog helper had been used before the
  configuration was loaded.
* Null-valued directive parameters are now treated as if the parameter had
  not been provided at all: a required argument reports the proper
  missing-argument error and an optional argument falls back to its
  default. The YAML directive syntax cannot express them (the parser
  rejects null values outright), but other parameter sources can supply
  them; previously they bypassed the required-argument check and failed
  deep inside the flag evaluation.
* The linear executor no longer leaks its current file and project state
  when a hook, directive processing or command execution fails;
  previously any such failure made the executor reject every future
  execution option change until the process was restarted.
* `AraraException` chains its underlying exception as the standard JVM
  cause, so stack traces and log reports now include the original failure
  instead of only the wrapping message.
* Restored the documented default parameters of `MPPPath.writeText` and
* The `removalFilter` of `Session.updateEnvironmentVariables` now receives
  the plain environment variable name (like the `additionFilter` already
  did) instead of the session-prefixed key, so custom filters can actually
  match.
* Lua project files report their errors with context: failures while
  extracting explicit directives name the offending file, and the Lua
  parsing error carries the underlying exception as its cause.
* Lua rules reject malformed list entries instead of silently dropping
  them: a non-string `authors` entry or a non-table `commands`/`arguments`
  entry now produces a parse error, matching the strictness of the YAML
  format they are slated to replace.
* Integral Lua numbers outside the integer range no longer wrap into
  unrelated values when converted (a Lua argument value of `3000000000`
  previously became `-1294967296`); they stay exact instead.
* Restored the documented default parameters of `MPPPath.writeText` and
  `Session.updateEnvironmentVariables`, which the Kotlin Multiplatform
  migration had dropped and which broke compilation.
* Unsupported rule and configuration file formats now raise a proper
  arara error instead of crashing with a `NotImplementedError`.
* The end-to-end test fixtures referenced the pre-migration
  `src/jvmTest` source set paths and are back on `src/test`.

## [7.2.0] - 2025-06-29

### Added

* The new command line parameter `-F` allows specifying multiple entries
  in the `key=value` format from a specified `.properties` file to be
  passed to arara's session map.
* A new `texfmt` rule to format LaTeX documents (thanks to William
  Underwood for suggesting it).
  (see #127)

### Fixed

* Resolved a potential issue with root path resolution in Windows under
  certain scenarios.
  (see #128)
* Updated `MPPPath` tests to improve coverage on Windows.
  (see #128)

### Changed

* Various build system improvements by @b-fein.
  (see !61, !62)
* Updated dependencies.
* `arara` now requires Java 11 or later to run.
* Kotlin Multiplatform updated to 2.2.0.
* Compatibility mode for Kotlin API and language updated to 2.2.
* Gradle updated to 8.14.2.

## [7.1.5] - 2024-05-06

### Fixed

* `arara` failed to run on Windows, macOS and Linux on certain JVM
  versions due to a wrong method call.
  (see #124)

## [7.1.4] - 2024-05-02

### Added

* A new `pdftocairo` rule to convert pdf's into various formats (thanks
  to @samcarter).
  (see !57)

### Fixed

* Documentation for imagemagick options (thanks to @samcarter).
  (see !58)

## [7.1.3] - 2024-02-13

### Added

* A new `rmdir` rule to remove directories (thanks to @hackbaellchen).
  (see !53)

## [7.1.2] - 2023-10-01

### Fixed

* `arara` failed to run on Windows, fixed by including JNA classes into the
  JAR (thanks to @hackbaellchen).
  (see #119)

## [7.1.1] - 2023-09-29

### Fixed

* `arara` would raise an error when running on Java 21.
  (see #118)

## [7.1.0] - 2023-05-05

### Added

* A new `mkdir` rule to create directories (thanks to @hackbaellchen).
  (see !48)
* The `MPPPath` public API has been extended with a new method:
  `createDirectories` which creates a directory and all relevant parents
  at the current path. It can be used in rules by going through
  `toFile(file).createDirectories()` access.

### Fixed

* Copyright dates have been outdated throughout the codebase.
  (see #108)

## [7.0.5] - 2023-04-17

### Fixed

* Various typos and style issues in the documentation (PDF and website). Thanks
  to @muzimuzhi for spotting and fixing most of these.
  (see !38, !39, !40, !43, !44, !46, #100, #102)
* A vulnerable (but in arara not realistically dangerous) log4j version caused
  errors in some screening tools and has been updated.
  (see #107)

## [7.0.4] - 2022-06-28

### Fixed

* `copy` and `move` rules were fixed to properly normalise relative paths.
  (see #91)

## [7.0.3] - 2022-05-12

### Fixed

* arara did not use the correct file separator on Windows.
  (see #84)

## [7.0.2] - 2022-05-05

### Fixed

* arara used Java File objects in rules instead of the new path API in nearly
  all cases except when using the `files` parameter. It is now again compliant
  with documented behavior and rules have been adjusted.

## [7.0.1] - 2022-05-03

### Fixed

* arara failed to load rules on Windows due to path problems when resolving the
  application's path.
  (see #83)
* CTAN demands source files for the PDF which we did not provide since having
  the HTML-based PDF. Now they are included.
* The CTAN zip missed the sources for the Lua DSL component.

## [7.0.0] - 2022-05-01

### Added

* arara now accepts `.lua` files as input files. These are treated as project
  specification and will let arara run all files specified in the project(s).
  When run as `arara project.yaml` you cannot run on multiple files. If you
  run Lua files with arara directives they will continue to work as before.
  (see #11)

### Changed

* The MVEL API now uses our `MPPPath` object instead of `java.io.File`. That
  way we are in control of the actual methods called which makes maintenance
  easier. There are substitutes for the most common file operations. Users
  of the `toFile()` method are safe as this is properly casted.
  (breaking change)
* The error message for IO errors has been changed to indicate potential
  encoding problems.
  (see #68)
* Use header-mode by default instead of scanning the whole document for
  directives. The `-H` flag has been removed, to get the old behavior back use
  `arara -w`. This change is experimental, and we might return to the old
  behavior based on user feedback.
  (see #63; breaking change)
* arara's manual is now built from the markdown sources powering the website
  using the [weasyprint](https://weasyprint.org) converter. Hence, all TeX files
  related to documentation and quickstart guide have been removed from the
  repository.
  (see #60)

### Fixed

* The manual on the website now offers scrolling on long table of contents.
  (see #80)

## [6.1.7] - 2022-04-16

### Added

* Documentation is now available on
  [arara's homepage](https://islandoftex.gitlab.io/arara/manual/)
  in addition to the PDF manual.

### Changed

* `xelatex` and `xetex` rules were updated to include the `unsafe` execution
  branch.
  (see #79)

### Fixed

* Fix shell script permissions for CTAN-distributed shell scripts.

## [6.1.6] - 2022-02-27

### Changed

* Updated dependencies.

## [6.1.5] - 2021-12-21

### Fixed

* Fix shipping a vulnerable `log4j` version.
  (see #78)

## [6.1.4] - 2021-12-14

### Fixed

* Fix shipping a vulnerable `log4j` version.
  (see #77)

## [6.1.3] - 2021-11-17

### Fixed

* The `indent` rule did not honour subdirectories. Only file names
  were used (paths were stripped) and thus the system command failed
  on such scenarios.
  (see #76)

## [6.1.2] - 2021-10-17

### Added

* The `indent` rule was updated to include three new switches.
  (see #73)

## [6.1.1] - 2021-06-26

### Changed

* Updated dependencies.

### Fixed

* The arara logo has been printed into the completion script output. This
  caused failures when sourcing directly. The logo has been removed from the
  completion script output.
  (see #70)
* Configuration files have been loaded after files have been resolved which
  conflicts with the configuration options for file resolution.
  (see #71)

## [6.1.0] - 2021-04-04

### Added

* The CLI now supports generating shell completions for bash, zsh and fish using
  `--generate-completions`.

### Fixed

* `arara` would raise a null pointer exception when running from the root
  directory in the underlying file system hierarchy.
  (see #69)

## [6.0.2] - 2021-03-31

### Fixed

* The `clean` rule did not use the list size property as required by v6.
  (see https://chat.stackexchange.com/transcript/message/57489291#57489291)
* Documentation for `Pair` now refers to its values as `first` and `second`
  property accessors.

## [6.0.1] - 2021-03-30

### Added

* The quickstart guide has been integrated in the website as a first step
  towards providing web based documentation.

### Fixed

* The help message did not mention the tool's purpose.
  (see #65)
* The `makeindex` rule did not use the list access syntax as required by v6.
  (see https://chat.stackexchange.com/transcript/message/57484552#57484552)

## [6.0.0] - 2021-02-26

### Added

* `arara` now features an API and a library to ease developing an own TeX
  compilation workflow.
* The new command line parameter `-P` allows specifying command-line options
  to be passed to arara's session map. In a rule, you may now retrieve `value`
  by calling `getSession().get('arg:key')` if you called arara with
  `arara -P key=value myfile.tex`.
  (see #38)
* Configuration files now honor an option `defaultPreamble` which allows to
  specify a preamble for `arara` to use even if there are no directives in
  the file nor preambles specified on the command-line. Preambles are resolved
  at execution time meaning preambles from local configurations will take
  precedence over global preambles.
  (see #55)
* The new configuration option `prependPreambleIfDirectivesGiven` allows you
  to specify a boolean value indicating whether preambles should be applied
  to all files or only those without directives. It defaults to `true` to avoid
  breaking existing workflows.
* `arara` now expands `@{}` orb tags within `options` parameter to directives.
  Supported methods are `getBasename`, `getSession` and `getOriginalReference`
  with their documented meanings.
  (see #38, #46)
* The methods above (`getBasename`, `getSession` and `getOriginalReference`)
  have also been added to the conditional context, i.e. you may use them in
  conditional expressions in directives.
* A new safe mode (`-S` or `--safe-run`) protects the system by disallowing
  certain user actions.
  (see #18)
  Currently, the following features are restricted:
  * file lookup will only perform explicit file resolution
  * `unsafelyExecuteSystemCommand` will raise an exception and abort the run
    (although rules are still allowed to construct arbitrary commands using
    a `return getCommand(…)` approach so this only disallows arbitrary system
    commands that would not get logged and are thus invisible to the user)
  * the `options` parameter does not expand orb tags in any directive.
* `-dev` formats are now available within the `latexmk` rule.
  (see #48)
* The rule context has now a `getOrNull(…)` method, which attempts to retrieve
  a list element based on an index (default is zero). If the index is out of
  bounds, a `null` value is returned instead.
* Multiple rules have been added. Most of them are untested and we are open for
  feedback and enhancements: `ghostscript`, `copy`, `move`, `ltx2any`, `llmk`,
  `spix`, `pdfcrop`, `sage`.
  (see #47 and https://github.com/cereda/arara/issues/83)
* A new quickstart guide has been written to complement to the reference manual.
  (see #9 and #59)
* A new website at https://islandoftex.gitlab.io/arara/ presents arara briefly
  and is to be extended with future releases.

### Changed

* `arara` is now split into an API, a core implementation (library) and the
  implementation of the executable (cli). Projects relying on code in the
  `arara` JAR distributions have to be updated.
  (breaking change)
* Rules included in the core distribution have been renamed to have a unique
  prefix in the texmf tree. File names should not be relied upon (see
  deprecation notice in v5).
  (see #32)
* Languages have to be passed as IETF BCP 47 codes. The old system has been
  removed. Hence, please use `en-QN` instead of `qn` etc.
* Localization is provided by classes as library instead of property files in
  arara's resources.
* If you pass an invalid language code, arara will now run in English and issue
  a log warning but not fail anymore. Failing due to the wrong language in a
  helper tool's (that is what arara is) output was considered inappropriate.
  (breaking change)
* The log file may now be specified as path anywhere on the file system.
  Warning: This behavior may be altered for a future safe mode.
  (see https://github.com/cereda/arara/issues/133).
* The method signatures of `loadObject` and `loadClass` have been altered to
  return a `Pair<ClassLoading.ClassLoadingStatus, Object>` instead of a
  `Pair<Integer, Object>`. You can now access the status values as enumeration.
* The implementation of methods available within rules has been moved to Kotlin
  causing `null` values to be handled differently. Previously undefined behavior
  will now cause an error.
  (breaking change)
* Preambles are now a client-only feature. This feature will neither make it
  to core nor to the mvel library because libraries should not deal with
  prepending content to a file which is what the preambles feature does.
* All `flag`s are consistently transformed into a `List<String>`. If a `List` is
  returned, it will be flattened and all values will be turned into string. If a
  string is returned, a single list with only that string will be returned.
  Other values are not encouraged but if returned transformed into a string. If
  you need interoperability of complex `command` code with older versions,
  use `isList(variable) ? variable[0] : variable` to get the value of previously
  non-List values.
  (breaking change)
* `default` values now are treated as values like user input. This means, no
  orb tags are interpreted within this field anymore. The `default` value will
  be processed by the `flag` if specified, just as user input would be. If no
  flag is present, it will be returned as a (list of; cf. previous item) string.
  Previous rules have to be rewritten if they make use of orb tags.
  (breaking change)
* The `dvisvgm` rule now supports `dvi`, `xdv`, `eps` and `pdf` extensions as
  input, as well as the inclusion of a page range. (see #53)
* The `xdvipdfmx` rule now supports both `dvi` and `xdv` extensions as input.
  (see #53)

### Fixed

* The exit status handling now conforms to the documentation. We only
  differentiate the exit values 0, 1 and 2 as detailed in the docs.
* The correct behavior of `currentFile` and `getOriginalReference` has been
  restored as version 5 incorrectly used the same object for both.
  (see #45)

### Removed

* Removed the `<arara>` shorthand notation which has been deprecated in version
  5.0.0.
  (see #26; breaking change)

## [5.1.3] - 2020-04-23

### Added

* New rule for the authorindex package/perl script).
  (see [this StackExchange post](https://tex.stackexchange.com/questions/539935/where-can-i-find-a-copy-of-the-arara-rule-file-authorindex-yaml-for-authorind?))

### Fixed

* In version 5.1.2, we introduced a different loading order for configuration
  files. This has fixed the loading of preambles but broke the loading of other
  settings. We accounted for that in v5.1.3.
  (see #44)

## [5.1.2] - 2020-04-19

### Added

* New rules for PythonTeX and PerlTeX have been added on user request.
  (see #42)

### Fixed

* The configuration file has been loaded too late. That's why preambles did
  not work any longer.
  (see #43)

## [5.1.1] - 2020-04-14

### Fixed

* TeX-related rules were updated to use `reference.getName()` instead of
  `reference` due to some issues when using absolute paths.

## [5.1.0] - 2020-04-03

### Added

* New rules for ConTeXt, fig2dev, qpdf, convert, texcount and detex.
  (see #10)

### Fixed

* `unsafelyExecuteSystemCommand` failed to execute in the current directory.

## [5.0.2] - 2020-03-07

### Fixed

* Running `arara` failed on Windows due to wrong path encodings.
  (see #35)

## [5.0.1] - 2020-03-05

### Changed

* The CI builds now produce a CTAN-ready zip file instead of running the
  documentation build separately

### Fixed

* Multiple rules did use a `file` reference where it should have used
  `reference` instead.
* Documentation still mentioned `file` although it should have been marked
  removed since version 5.0.0.

## [5.0.0] - 2020-03-03

### Added

* Working directory support has been added. Users may now specify their working
  directory by passing a command-line option.
  (see #13)
* `arara` features support for processing multiple files. Prerequisite: They
  are located within the same working directory.
* The most important update release notes are shown when calling arara with the
  version flag.
  (see #25)
* Multiple rules have been added. All of them are untested and we are open for
  feedback and enhancements: `asymptote`, `luahbtex`, `metapost`, `pbibtex`,
  `platex`, `ptex`, `upbibtex`, `uplatex`, `uptex`, `xindex`.
  (see #10)

### Changed

* The build process is now based on Gradle instead of Maven. This includes new
  tasks providing all necessary tooling to create CTAN uploads.
  (see #8)
* The application has been rewritten in Kotlin and the code has been streamlined
  to its use cases. That includes we now run a test suite (which is far from
  exhaustive).
* The application is now hosted on [GitLab](https://gitlab.com/islandoftex/arara).
  From now on, support will be provided there.
* The file lookup now performs an additional lookup step. File names ending on a
  period will ignore the period (use case: command-line completion by various
  shells) in the second lookup. So `arara file.` will resolve `file..tex`
  etc. first and then (if no luck yet) try to resolve `file.tex`.
* `arara` does not use a XML database anymore. File changes are recorded in a
  YAML database instead.
* The `biber` rule is now aware of the biber option `tool`.
  (see https://github.com/cereda/arara/issues/136)

### Deprecated

* The `<arara>` shorthand notation will be removed in the next major release.
  (see #26)
* The file names of distributed rules being public should not be relied upon
  anymore because there are plans to rename them.
  (see #32)

### Removed

* Removed triggers and introduced session namespaces as a replacement
  (see #15, #2, #17; breaking change)
* Removed velocity and with it all support for templating languages for now.
  It is not planned to reintroduce such a dependency to the core. But rules
  supporting templating tools are welcome.
  (see #8; breaking change)
* Removed string-based commands. Guessing the intention of a string is a strong
  kind of opinionating syntax which will not be followed further.
  (see #2; breaking change)
* Removed support for `user.dir`; no lookup will be performed on this variable.
  (see #16; breaking change)
* Removed auxiliary tools such as the separate CTAN packager, language checker
  and rule converter. They have been merged into the test suite or build process
  or lost relevance.
* Removed `file` reference. The replacement `reference` is correctly typed
  and available since version 4.0.

### Fixed

* `unsafelyExecuteSystemCommand` now respects `getCommandWithWorkingDirectory`
  (previously, working directories have been ignored; see #21).

## [4.0.0] - 2018-07-10

### Added

* Support for multiline directives was added.
* The exit status checking can be adjusted.
* Partial directive extraction mode has been added (only scan until the first
  non-comment line is encountered).
* The user may define preambles to share common preambles between files that
  require the same compilation setup.
* Support for running code from `class` and `jar` files has been added.
* `Command`s and `Trigger`s have been introduced as abstraction layers.

### Changed

* arara follows a REPL workflow (rule evaluation on demand as opposed to prior
  to execution).
* The rule format has been reworked, e.g. `command` has become `commands`
  (breaking change).
* The lookup strategy for configuration files has been altered and extended.

### Deprecated

* String-based commands require guesswork and opinionated syntax. Hence, they
  will be removed in version 5.0.

### Removed

* Cross-platform installers have been retired. `arara` is available in all major
  TeX distributions.

<!-- END CHANGELOG -->

## [3.0] - 2013-02-07

### Added

* Localized messages in English, Brazilian Portuguese, German, Italian, Spanish, French, Turkish and Russian.
* Friendly and very detailed messages instead of generic ones.
* An optional configuration file is now available in order to customize and enhance the application behaviour.
* Now rules are unified in a plain format. No more compiled rules.
* Rules can allow an arbitrary number of commands instead of just one.
* Built-in functions in the rule context to ease the writing process.

### Fixed

* Improved error analysis for rules and directives.
* Improved rule syntax, new keys added.
* Improved expansion mechanism.

## [2.0] - 2012-07-24

### Added

* Added the `--timeout n` flag to allow setting a timeout for every task. If
     the timeout is reached before the task ends, `arara` will kill it and
     interrupt the processing. The `n` value is expressed in milliseconds.
* There's no need of noninteractive commands anymore. `arara` can now handle
     user input through the `--verbose` tag. If the flag is not set and the
     command requires user interaction, the task execution is interrupted.
* Added the `@{SystemUtils}` orb tag to provide specific operating system
     checks. The orb tag maps the `SystemUtils` class from the amazing
     [Apache Commons Lang](http://commons.apache.org/lang/) library and
     all of its methods and properties.

### Fixed

* Fixed the `--verbose` flag to behave as a realtime output.
* Fixed the execution of some script-based system commands to ensure
     cross-platform compatibility.

## [1.0.1] - 2012-04-24

### Added

* Added support for `.tex`, `.dtx` and `.ltx` files. When no extension is
     provided, `arara` will automatically look for these extensions in this
     specific order.
* Added the `--verbose` flag to allow printing the complete log in the
     terminal. A short `-v` tag is also available. Both `stdout` and `stderr`
     are printed.
* Fixed exit status when an exception is thrown. Now `arara` also returns a
     non-zero exit status when something wrong happened. Note that this
     behaviour happens only when `arara` is processing a file.

## [1.0] - 2012-04-24

* Initial release

---

For versions previous to version 5.0.0, there has not been any persistent record
of changes. Please refer to the commit history for details.

[Unreleased]: https://gitlab.com/islandoftex/arara/compare/v7.2.0...master
[7.2.0]: https://gitlab.com/islandoftex/arara/compare/v7.1.5...v7.2.0
[7.1.5]: https://gitlab.com/islandoftex/arara/compare/v7.1.4...v7.1.5
[7.1.4]: https://gitlab.com/islandoftex/arara/compare/v7.1.3...v7.1.4
[7.1.3]: https://gitlab.com/islandoftex/arara/compare/v7.1.2...v7.1.3
[7.1.2]: https://gitlab.com/islandoftex/arara/compare/v7.1.1...v7.1.2
[7.1.1]: https://gitlab.com/islandoftex/arara/compare/v7.1.0...v7.1.1
[7.1.0]: https://gitlab.com/islandoftex/arara/compare/v7.0.5...v7.1.0
[7.0.5]: https://gitlab.com/islandoftex/arara/compare/v7.0.4...v7.0.5
[7.0.4]: https://gitlab.com/islandoftex/arara/compare/v7.0.3...v7.0.4
[7.0.3]: https://gitlab.com/islandoftex/arara/compare/v7.0.2...v7.0.3
[7.0.2]: https://gitlab.com/islandoftex/arara/compare/v7.0.1...v7.0.2
[7.0.1]: https://gitlab.com/islandoftex/arara/compare/v7.0.0...v7.0.1
[7.0.0]: https://gitlab.com/islandoftex/arara/compare/v6.1.7...v7.0.0
[6.1.7]: https://gitlab.com/islandoftex/arara/compare/v6.1.6...v6.1.7
[6.1.6]: https://gitlab.com/islandoftex/arara/compare/v6.1.5...v6.1.6
[6.1.5]: https://gitlab.com/islandoftex/arara/compare/v6.1.4...v6.1.5
[6.1.4]: https://gitlab.com/islandoftex/arara/compare/v6.1.3...v6.1.4
[6.1.3]: https://gitlab.com/islandoftex/arara/compare/v6.1.2...v6.1.3
[6.1.2]: https://gitlab.com/islandoftex/arara/compare/v6.1.1...v6.1.2
[6.1.1]: https://gitlab.com/islandoftex/arara/compare/v6.1.0...v6.1.1
[6.1.0]: https://gitlab.com/islandoftex/arara/compare/v6.0.2...v6.1.0
[6.0.2]: https://gitlab.com/islandoftex/arara/compare/v6.0.1...v6.0.2
[6.0.1]: https://gitlab.com/islandoftex/arara/compare/v6.0.0...v6.0.1
[6.0.0]: https://gitlab.com/islandoftex/arara/compare/v5.1.3...v6.0.0
[5.1.3]: https://gitlab.com/islandoftex/arara/compare/v5.1.2...v5.1.3
[5.1.2]: https://gitlab.com/islandoftex/arara/compare/v5.1.1...v5.1.2
[5.1.1]: https://gitlab.com/islandoftex/arara/compare/v5.1.0...v5.1.1
[5.1.0]: https://gitlab.com/islandoftex/arara/compare/v5.0.2...v5.1.0
[5.0.2]: https://gitlab.com/islandoftex/arara/compare/v5.0.1...v5.0.2
[5.0.1]: https://gitlab.com/islandoftex/arara/compare/v5.0.0...v5.0.1
[5.0.0]: https://gitlab.com/islandoftex/arara/compare/v4.0...v5.0.0
[4.0.0]: https://gitlab.com/islandoftex/arara/compare/v3.0...v4.0
[3.0]: https://gitlab.com/islandoftex/arara/compare/v2.0...v3.0
[2.0]: https://gitlab.com/islandoftex/arara/compare/v1.0.1...v2.0
[1.0.1]: https://gitlab.com/islandoftex/arara/compare/v1.0...v1.0.1
[1.0]: https://gitlab.com/islandoftex/arara/-/tags/v1.0
