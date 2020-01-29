# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project tries to adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)
from version 5.0.0 on.

## [Unreleased]

#### Added

* Working directory support has been added. Users may now specify their working
  directory by passing a command-line option.
  (see #13)
* `arara` features support for processing multiple files. Prerequisite: They
  are located within the same working directory.
* The most important update release notes are shown when calling arara with the
  version flag.
  (see #25)
* A `metapost` rule has been added.

### Changed

* The build process is now based on Gradle instead of Maven. This includes new
  tasks providing all necessary tooling to create CTAN uploads.
  (see #8)
* The application has been rewritten in Kotlin and the code has been streamlined
  to its use cases. That includes we now run a test suite (which is far from
  exhaustive).
* The application is now hosted on [GitLab](https://gitlab.com/islandoftex/arara).
  From now on, support will be provided there.

### Deprecated

* The `<arara>` shorthand notation will be removed in the next major release.
  (see #26)

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

### Fixed

* `unsafelyExecuteSystemCommand` now respects `getCommandWithWorkingDirectory`
  (previously, working directories have been ignored; see #21).

## [4.0.0] - 2018-07-10

### Changed

* The rule format has been reworked (breaking change).

<!-- END CHANGELOG -->

For versions previous to version 5.0.0, there has not been any persistent record
of changes. Please refer to the commit history for details.

[Unreleased]: https://gitlab.com/islandoftex/arara/compare/v4.0...master
[4.0.0]: https://gitlab.com/islandoftex/arara/compare/v3.0...v4.0
[3.0]: https://gitlab.com/islandoftex/arara/-/tags/v2.0...v3.0
[2.0]: https://gitlab.com/islandoftex/arara/compare/v1.0...v2.0
[1.0]: https://gitlab.com/islandoftex/arara/-/tags/v1.0
