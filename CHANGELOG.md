# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project tries to adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
from version 5.0.0 on.

## [Unreleased]

### Changed

* Updated dependencies.

## [6.1.5] - 2021-12-21

### Fixed

* Fixed shipping a vulnerable `log4j` version.
  (see #78)

## [6.1.4] - 2021-12-14

### Fixed

* Fixed shipping a vulnerable `log4j` version.
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

[Unreleased]: https://gitlab.com/islandoftex/arara/compare/v6.1.5...master
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
