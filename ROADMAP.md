# Pulling the plug on YAML rules and configuration files

This document is the removal playbook for the deprecated YAML *rule* and
*configuration* formats. It lists, file by file, what has to happen when we
finally remove them in favor of the Lua formats introduced in version 8.0.
The version 9 outlooks at the end record the decisions beyond the removal
(the conditional dialect, the path model and the class loading lifecycle).

> **Scope.** Only rules (`arara-rule-*.yaml`, user rules) and configuration
> files (`.araraconfig.yaml`, `araraconfig.yaml`, `.arararc.yaml`,
> `arararc.yaml`) are removed. YAML itself does **not** leave the code base:
> directives embed YAML maps as their parameter syntax, e.g.
>
> ```tex
> % arara: pdflatex: { shell: yes }
> ```
>
> and that syntax is stable, documented user interface. See
> [What stays](#what-stays) before deleting anything.

## What stays

These components look YAML- or MVEL-related but are **out of scope**; do not
remove them as part of this effort:

> **One YAML library.** Since yamlkt was replaced, the code base uses a
> single YAML parser:
>
> | Library | Coordinates | Used by | Fate |
> | --- | --- | --- | --- |
> | **kotaml** | `io.heapy.kotaml` (a supported fork of the archived kaml; the fork keeps the original `com.charleskorn.kaml` package) | YAML rules (`mvel/…/rules/Rule.kt`), YAML config (`core/…/configuration/LocalConfiguration.kt`), directive parameters (`cli/…/DirectiveUtils.kt`), file database (`core/…/Database.kt`) | rules and config are removed with them; directive parameters and the database **stay** |
>
> Whenever this document says the YAML *library for rules and
> configuration* is removed, it means the kotaml dependency **of the
> mvel module only**. Directives keep being parsed as YAML, and the file
> database keeps being serialized as YAML, both via kotaml in the cli
> and core modules.

1. **Directive parameter parsing.** `cli/…/ruleset/DirectiveUtils.kt` parses
   the `{ key: value }` part of a directive with **kotaml**
   (`Yaml.default.parseToYamlNode` plus the `YamlNodeConversion` object).
   Directives are YAML by specification, independently of the rule format
   the directive eventually resolves to. Keep `libs.kotaml` in
   `cli/build.gradle.kts`.
2. **The file database.** `core/…/files/Database.kt` serializes arara's
   hash database with kotaml. Unrelated to rules/config; keep `libs.kotaml`
   in `core/build.gradle.kts`. (If the database format should ever change,
   that is a separate decision.)
3. **MVEL directive conditionals.** The `if`/`while`/`until`/`unless`
   conditionals of a directive (`% arara: pdflatex if found('log', '!')`)
   are MVEL expressions and are *not* deprecated. (A Lua-based
   counterpart tied to the `--engine` flag exists, and conditionals turn
   Lua with the phase 1 default flip; see the version 9 outlook at the
   end of this document.) Consequently the
   following must survive the removal:
   - `mvel/…/rules/DirectiveConditionalEvaluator.kt`
   - `core/…/rules/ConditionalMethods.kt`, `core/…/utils/MethodUtils.kt`
     (already moved out of the mvel module)
   - `mvel/…/utils/MvelState.kt` (conditional methods, preambles)
   - `api/…/AraraExceptionWithHeader.kt` (already moved out of the
     mvel module)
   - the `org.mvel:mvel2` dependency
4. **Preambles.** Preambles are plain strings of directives; they are
   stored on `MvelState` for historic reasons only. They keep working with
   both engines.
5. **`RuleMethods`.** The helper methods are engine-agnostic (the Lua
   bridges in `lua/…/rules/LuaRuleEnvironment.kt` call them); they have
   been **moved to `core/…/rules/` ahead of the removal** (see step 2.4),
   so they survive the deletion untouched.

## Prerequisites for removal

Do not start the removal before all of the following hold:

- [ ] At least one stable release has shipped with the Lua engine
      available (`--engine lua`), the deprecation warning for YAML rules
      (`cli/…/ruleset/RuleUtils.kt`) and the CHANGELOG/manual notices.
- [ ] A release has shipped with **Lua as the default engine** (see
      phase 1) so users had a cycle to notice breakage while YAML was
      still available as an opt-in fallback.
- [ ] The official rules in the Lua format have reached TeX Live/CTAN
      (the former packaging tasks, including the TDS tree builder that
      copied the whole `rules/` directory, were removed from the build;
      the `oc1` tag retains them for the release workflow).
- [ ] The migration path is documented for users: no conversion tool
      ships (the converters were scaffolding for this fork's own rules
      and were removed), so the manual carries a worked YAML-to-Lua
      example and the key mapping table; its Configuration chapter
      documents the Lua configuration format.

## Phase 1 — flip the default engine

Small, reversible release:

1. `cli/…/ruleset/RuleEngine.kt`: change the companion default from
   `var current: RuleEngine = YAML` to `LUA`.
2. `cli/…/CLI.kt`: change `.default(RuleEngine.YAML)` to
   `.default(RuleEngine.LUA)` on the `--engine` option and update its help
   text ("'yaml' (deprecated) or 'lua' (default)").
3. Update the usage block and the `--engine` bullet in
   `website/content/manual/CLI.md`, plus a CHANGELOG entry.
4. Consider upgrading the one-time `logger.warn` in `RuleUtils` to a
   visible `DisplayUtils` warning when the YAML engine is selected
   explicitly.

## Phase 2 — remove YAML rules

### 2.1 CLI module

- `cli/…/ruleset/RuleFormat.kt`: delete the `MVEL("yaml")` constant
  (and the now-unneeded `@Deprecated` annotation and
  `@Suppress("DEPRECATION")` sites that reference it).
- `cli/…/ruleset/RuleEngine.kt`: delete the `YAML` constant; at this point
  the enum only distinguishes `LUA` (and, one day, the Kotlin DSL). Either
  keep it as the extension point for the DSL or delete the enum together
  with the `--engine` option (phase 4).
- `cli/…/ruleset/RuleUtils.kt`: remove the `RuleFormat.MVEL.extension`
  branch, `warnAboutYamlDeprecation()` and the
  `org.islandoftex.arara.mvel.rules.Rule` import. Replace the branch with a
  friendly error for leftover `.yaml` rules, e.g. "YAML rules are no
  longer supported since version X; see the manual's migration section
  for rewriting the rule in the Lua format", so users with custom rules
  get guidance instead of a rule-not-found error.
- `cli/…/interpreter/Interpreter.kt`:
  - delete `evaluateMvelCommand(…)`, `mvelExitCheck(…)` and the
    `is SerialRuleCommand ->` branch of `executeCommand` (keep the
    `LuaRuleCommand` branch and the `else` error);
  - delete `resultToList(…)` if nothing else uses it;
  - remove `parameters.plus(MvelState.ruleMethods)` from the flag
    evaluation `context` in `parseArguments` — Lua argument processors
    ignore the method map, it was only consumed by MVEL flags;
  - drop the imports of `SerialRuleCommand`, `TemplateRuntime` and — if
    the preamble handling is the only remaining consumer — narrow the
    `MvelState` usage accordingly.
  - **Keep** `DirectiveConditionalEvaluator` and everything conditional-
    related (see [What stays](#what-stays)).

### 2.2 MVEL module

- Delete `mvel/…/rules/Rule.kt`, `mvel/…/rules/RuleArgument.kt` and
  `mvel/…/rules/SerialRuleCommand.kt`.
- Remove `libs.kotaml` from `mvel/build.gradle.kts` only. The version
  catalog entry **stays**: since the tree unified on a single YAML
  library, kotaml also parses the directive parameters (cli) and the
  file database (core), so `grep -rn "com.charleskorn" --include="*.kt" .`
  keeps matching those two surviving consumers (see
  [What stays](#what-stays)). Remove the
  `kotlin("plugin.serialization")` plugin from the mvel module if no
  `@Serializable` class remains there.
- The module keeps its name (MVEL conditionals still justify it) unless
  you want to fold the survivors into `core` — in that case move
  `DirectiveConditionalEvaluator`, `MvelState` and the `mvel2` dependency
  (`ConditionalMethods`, `MethodUtils` and `AraraExceptionWithHeader`
  already live in core and the api module), then
  delete the module from `settings.gradle.kts` (module list) and from the
  root `build.gradle.kts` (spotless targets for `mvel/build.gradle.kts`
  and `mvel/src/**/*.kt`, detekt source list), and adjust the publication.

### 2.3 Configuration model

- `LocalConfiguration` and `SerialFileType` already live in
  `core/…/configuration/` (moved ahead of the removal; the Lua parser
  constructs the class directly). At removal time:
  - delete the kotaml-based `companion object { fun load(…) }` and the
    `@Serializable` annotations;
  - drop the `!config` header check, which was YAML-specific.
- `cli/…/configuration/ConfigurationUtils.kt`:
  - shrink `configFileForProject` to a single Lua search:
    `configFileWithExtension(project, "lua")` — the YAML-first indirection
    (`listOf("yaml", "lua").firstNotNullOfOrNull { … }`) goes away;
  - in `loadLocalConfiguration`, remove the `"yaml" -> …` branch; as with
    rules, prefer a helpful error over silence when a
    `.araraconfig.yaml`-style file is still present: detect the YAML names
    and tell the user to migrate (the manual's Configuration chapter has
    the key-by-key mapping; the configurations are one-liners to rewrite
    by hand);
  - update the class KDoc describing the lookup order.

### 2.4 Rule helper methods

`RuleMethods` must outlive MVEL rules because the Lua bridges delegate
to it. It already lives in `core/…/rules/`, moved ahead of the removal
together with `ConditionalMethods`, `MethodUtils` and
`AraraExceptionWithHeader`, and the `lua` module no longer depends on
the mvel module at all. At removal time:

- remove the entries that `MvelState.ruleMethods` reflected into the
  MVEL evaluation context, and — once no consumer is left — the
  `ruleMethods` map in `MvelState` itself (`conditionalMethods` stays).

### 2.5 Rule files and build glue

- Delete `rules/arara-rule-*.yaml` (74 files); the `.lua` twins remain.
- Root `build.gradle.kts`: delete the `yaml { target("rules/*.yaml") … }`
  spotless block (the `format("luaRules")` block stays). Keep the Kotlin
  spotless block untouched.
- The conversion scripts are already gone: they were scaffolding for
  translating this fork's own rules and never shipped, so users write
  Lua rules from scratch, guided by the manual. The whole-ruleset
  structure validation the Lua checker once provided lives in the
  `lua` module's test suite, which parses every shipped rule through
  the real parser in every gate.
- CTAN/TDS: the packaging tasks, including the TDS tree builder that
  copied `rules/` wholesale, were removed from this build; the release
  workflow owns packaging, so verify the shipped tree there once after
  the deletion.
- CI: no pipeline file lives in this tree; run the full gates —
  `spotlessCheck` in particular — after removing the yaml block.

### 2.6 Tests

- The end-to-end fixtures under `cli/src/test/resources/executiontests/`
  consist of YAML rules (`echoone.yaml`, …) and YAML configuration files
  (`arararc.yaml`) per scenario — port them to the Lua formats by hand
  (they are small echo-style rules; the configs are one-liners).
  The `directive-with-mvel-options` and `conditionals` scenarios stay:
  directive options and conditionals are out of scope.
- Note that the `arara.yaml` file handled by the "track changes" case in
  `cli/src/test/…/ExecutionTest.kt` is the *database* (see
  [What stays](#what-stays)), not a rule or configuration file — leave it
  alone.
- Delete the YAML rule/configuration parser tests in the mvel module (if
  present) and make sure `lua/src/test/…/rules/LuaRuleParserTest.kt` and
  `lua/src/test/…/configuration/LuaLocalConfigurationTest.kt` cover any
  behavior the deleted tests asserted.
- Grep the test sources for `!config` and `arara-rule-` + `.yaml`.

### 2.7 Documentation

- `website/content/manual/YAML.md`: delete the chapter (it documents the
  YAML rule/config syntax); check `_index.md`/menu weights.
- `website/content/manual/MVEL.md`: **keep**, but rewrite the framing:
  MVEL is now only the directive-conditional language. Move rule-specific
  MVEL content out or drop it.
- `website/content/manual/Concepts.md`: make the Lua format the primary
  description of a rule; remove the YAML explanation and the "Lua rule
  format" message box (it becomes the main text).
- `website/content/manual/Configuration.md`: same for the configuration —
  the Lua section becomes the main text, the YAML keys/`!config` parts go,
  the lookup-order paragraph shrinks to the four `.lua` names.
- `website/content/manual/CLI.md`: update/remove the `--engine`
  description depending on phase 4.
- `website/content/manual/Methods.md`: still accurate (the helpers exist
  as Lua globals); adjust examples from MVEL to Lua syntax.
- Add a prominent CHANGELOG entry under a new major version and a news
  post with the migration guide.

## Phase 3 — grep checklist

Before tagging the release, all of these should come back empty (or only
match this file and the CHANGELOG/news). The kotaml greps are scoped to
the mvel module on purpose: the library keeps the original
`com.charleskorn.kaml` package, so the cli and core consumers of the
directive and database surfaces match it forever (see the guard rails
below):

```sh
grep -rn "com.charleskorn"         --include="*.kt" mvel/src
grep -n "libs.kotaml"              mvel/build.gradle.kts
grep -rn "SerialRuleCommand"       --include="*.kt" .
grep -rn "RuleFormat.MVEL"         --include="*.kt" .
grep -rn "RuleEngine.YAML"         --include="*.kt" .
grep -rn "mvel.rules.Rule"         --include="*.kt" .
grep -rn "MvelState.ruleMethods"   --include="*.kt" .
grep -rn "!config"                 --include="*.kt" --include="*.md" .
ls rules/*.yaml 2>/dev/null
```

And these must still match (guard rails — if any comes back empty, scope
was exceeded):

```sh
grep -rn "com.charleskorn" cli/src               # directive parameters
grep -rn "com.charleskorn" core/src              # file database
grep -rn "libs.kotaml" cli/build.gradle.kts core/build.gradle.kts
grep -rn "DirectiveConditionalEvaluator" cli/src # MVEL conditionals
grep -rn "TemplateRuntime\|MVEL" mvel/src        # conditional evaluation
```

In particular, `% arara: pdflatex: { shell: yes }` must still parse after
the removal — the end-to-end tests in
`cli/src/test/resources/executiontests/directive-with-options/` cover
exactly this and must keep passing.

## Phase 4 — optional cleanup (separate release)

- Retire the `--engine` option (`cli/…/CLI.kt`, `RuleEngine`) once only one
  engine exists. (An earlier revision of this document suggested repurposing
  the option for the Kotlin DSL; that module was removed from the tree in this
  fork as it was never wired into the application, so the option has no third
  engine to select.) Removing a CLI
  option is a user-visible break: deprecate it for one release (accept
  and warn) before dropping it.
- Delete the migration error messages (the conversion scripts were
  already removed before the first release).
- Revisit the module layout: with rules and configuration gone, the mvel
  module is a small "directive conditionals" library — folding it into
  core removes a publication artifact (coordinate `arara-mvel` consumers,
  if any, via the CHANGELOG).

## Version note

Removing a supported file format is a breaking change: schedule phase 2
for the next **major** version after the phase 1 release, per the semantic
versioning policy stated in `CHANGELOG.md`.

## Version 9 outlook — replacing MVEL conditionals

Directive conditionals follow the rule engine: with `--engine lua` they
are evaluated as Lua expressions (`LuaConditionalEvaluator`), with the
default YAML engine they keep the MVEL dialect. The Lua dialect offers
the same helpers, requires a proper boolean result like MVEL, and
inverts `unless`/`until` identically. Simple, single-call conditions
such as `if exists('log')` work in both dialects unchanged; compound
conditions use Lua's `and`, `or` and `not` instead of `&&`, `||` and
`!`.

The Lua helper boundary is typed and will stay so in version 9: helper
arguments must match the declared type, mismatches raise Lua-style
`bad argument #<n> to '<helper>'` errors, and results keep their types
(conditions and `exit` closures require proper booleans). Lua's
implicit coercions apply to operators, not to arara's library
functions — this mirrors how Lua's own standard library type-checks
its arguments, keeps `'true'` a string and `'1'` out of numeric slots,
and matches the strict scalar contract the directive parameter parser
already follows. Rule and project file parsing follows the same
contract: absent optional entries keep their documented defaults,
wrongly typed entries fail at parse time, and nothing is silently
coerced, defaulted or dropped.

Rule evaluation failures surface through the same localized runtime
errors the MVEL dialect uses (command, flag and `exit` closures), with
arara-generated helper errors available as the diagnostic detail;
author-authored `throwError` exceptions keep flowing raw in both
dialects.

Project specification parsing keeps its English detail messages on
purpose: the command line demotes a failed project parse to the log
(treating the file as regular input, with a localized notice), so the
details stay at the log-grade English level while the user-facing
surface speaks the user's language.

Resolved: `toLua` wraps file objects as file userdata, so a file stored
via `session.put` comes back as a file reference and the helpers
dispatch to the file overloads, exactly like the MVEL dialect keeps
the object. Round-trips preserve the file; string forms remain
available through the usual helpers (`getBasename`, `toFile` paths and
the `reference` table).

Consequences for the plan above:

- the phase 1 default flip turns conditionals Lua as well, so the
  release notes of that release must tell users to port compound
  conditionals (`&&`/`||`/`!` become `and`/`or`/`not`);
- the helpers moved to a neutral home ahead of schedule:
  `ConditionalMethods` and `RuleMethods` live in `core/…/rules/`,
  `MethodUtils` in `core/…/utils/`, the shared configuration model in
  `core/…/configuration/` and `AraraExceptionWithHeader` in the `api`
  module — the Lua engine no longer depends on the mvel module at all
  (the former transitional `lua` -> `mvel` edge is gone);
- the remaining MVEL surface after that move is empty: the orb tag
  interpolation of directive `options` also has a Lua counterpart tied
  to the engine (`LuaOrbTags`), including the safe mode passthrough.
  Once the conditional and rule helpers have moved to a neutral home,
  the `mvel` module can be deleted entirely.

## Version 9 outlook — the path model

The path class wrapping the JVM path API keeps two representations: the
path exactly as provided (backing `isAbsolute`, `startsWith`, the
`resolve` inputs and the JVM escape hatches) and an absolutized,
normalized reference of it (backing identity, all I/O and display). The
duality is load-bearing, not incidental — it is how relative spellings
and location identity coexist.

Two candidates are recorded for the next major version:

- the class name is a fossil of the removed Kotlin Multiplatform setup;
  a rename is public API breakage and belongs to a major version;
- re-basing the class on a multiplatform path library such as okio was
  investigated, found feasible, and approved by the technical leads:
  the swap lays the foundation for a possible return to a true
  multiplatform architecture. The work proceeds on the dedicated
  `okio-migration` branch, tagged in the same sequence as the main
  line, and merges back only after the full suite passes on a real
  Windows machine; the default line stays release-ready at its
  current tip throughout.

The decision packet, verified against the okio sources directly:

- the path algebra is pure common code: the JVM actual of the path
  class delegates every member (root, segments, name, parent,
  resolution, normalization, equality, hashing, ordering) to common
  internals, so path arithmetic executes no platform code; the JVM
  extras are the `File` and `java.nio` conversions and the platform
  directory separator.
- the file system face keeps the platform seam in its abstract
  members (canonicalization, metadata, listing, opening, sourcing,
  sinking, directory creation, deletion, moving, symlinks); the
  rest composes in common code. On a modern JVM the system
  implementation extends the `java.io` adapter and overrides only
  the metadata, atomic move and symlink operations with `java.nio`
  — reads, writes, listings and deletions go through `java.io`
  under the hood.
- metadata is read without following symlinks, and the link target
  is reported separately; the path model therefore resolves link
  chains itself before answering existence, type, size and time
  queries, so the current follow-links semantics survive the swap.
- equality, hashing and ordering compare the stored spelling bytes
  and are always case-sensitive. The reference duality normalizes
  before comparing, which makes reference equality provably
  identical between the two backends on case-sensitive platforms;
  the remaining deltas are Windows-shaped: case-folded equality
  today versus case-sensitive equality after the swap, and
  drive-relative spellings no longer resolving against the
  per-drive working directory. Mixed-separator spellings such as
  `C:/x` would be the one delta that breaks correctness rather
  than shape — the TeX toolchain reports forward slashes on
  Windows, while the new algebra recognizes a Windows path by its
  first backslash — so the model re-spells drive-prefixed and UNC
  spellings with backslashes on Windows before parsing.
  Degenerate provided spellings narrow everywhere: interior `.`
  segments and redundant separators collapse at construction,
  leading `..` on absolute paths is consumed eagerly, and the
  empty string becomes `.`.
- the public bridges stay: the surface is frozen, every consumer
  keeps working through them, and the swap is contained in the
  path model itself; removing the bridges belongs to the same
  major-version decision as the class rename.

The costs stay as recorded — the file database keys and the
displayed path forms on Windows change, per the mechanisms above —
and the merge criteria are explicit: all gates green on Linux for
every step of the branch, the full suite green on a real Windows
machine, and the report reviewed. Only then the branch merges into
the default line; which release carries the swap remains a calendar
call.

The branch work is complete and awaits the Windows verification:
the model and the common-able helpers run on the new backend, the
remaining consumers stay on the frozen bridges by design, and the
Windows-shaped spellings — drive letters, UNC paths, case
sensitivity and the first-separator rule — are pinned as contracts,
with the in-memory file system serving the model operations for
hosts that cannot represent them and the symlink probes skipping
wherever the host denies link creation.

The verification environment itself produced one more finding. The
formatter derives its line-ending expectation from the host whenever
no policy is declared: in a working copy without a git repository
it ignores the git configuration entirely and expects the
platform's native endings, so a byte-identical Windows copy fails
the rule and the Kotlin formatting steps; with a repository present
it honors the local git configuration instead. The upstream
continuous integration — a three-operating-system matrix whose first
step pins the checkout line endings before checking out, and which
this copy does not carry — relies on exactly that mechanism, and
its attributes file pins only binary media to the large-file
storage, holding no line-ending policy of its own (the upstream
build also keeps its Kotlin formatter step disabled, where this
tree runs it selectively). The verification therefore follows the
upstream mechanism rather than inventing a repository-level one:
the branch is cloned with the checkout configuration carried on
the clone invocation itself, the two settings are pinned locally
inside the clone, and the full gates then run unchanged. A
repository-level line-ending policy was considered and closed as
won't fix: the environment-level mechanism matches the upstream
practice and stands as this tree's resolution, and no
repository-level policy will be added.

The rule formatting steps join the on-demand regime as part of the
same step: both rule sets are converter-generated artifacts, so
their formatting is validated only when requested — behind the
renamed rule-validation flag and its new Lua sibling, matching the
task-family naming.

The Windows verification refined one contract: spellings that are
neither drive-prefixed nor UNC take the host's directory separator
at construction — the same canonicalization the previous platform
backend performed — while the re-spelling gate keeps
forward-slashed drive and UNC spellings correct on Windows, as the
per-host pins express. The refinement came from the verification
itself: the degenerate-spelling pins had been written against the
library's development sources instead of the shipped artifact,
and the artifact is what defines the contract.

The second verification wave reached the core and Lua suites and
flushed out three platform-blind tests, none a defect of the
migration: the path lookup test asked for the echo utility, which
is a shell built-in on Windows with no executable of its own, and
the two Lua argument tests interpolated drive spellings into
single-quoted Lua string literals, where the backslash is an
escape character — the same tests would have failed identically
under the previous backend. The fixes teach the tests the
platform instead of narrowing the contract, and the migration code
itself now stands validated on real Windows across the API, core
and Lua modules; the MVEL and command line suites run on the next
pass.

The third verification wave reached the command line suite and
flushed out the last six failures, all of them fixture authoring
of the same shape: four test rules called the echo program
directly, which is a shell built-in on Windows with no program of
its own, instead of following the platform-aware pattern their
sibling fixtures already carry. The interpreter launches programs
directly — no shell in between, whatever the terminal hosting the
build — so a command has to exist as a program. The four rules
join their siblings in the pattern, and the end-to-end machinery
itself is validated on real Windows: the halt and session
scenarios, the orb tags, the file database change tracking and
the Lua engine happy paths all passed. No production code has
changed across the three verification waves; every finding was
test or fixture authoring.

The verification campaign closed with two build-hygiene notes, both
predating the migration and surfaced by running the build on
Windows. The jar merger note has since been remedied in this tree:
the Kotlin metadata entries now warn and process instead of being
silently dropped before the merger sees them, and the rebuilt jar
was proven entry-identical to its predecessor and smoke-verified
across the whole matrix. The remedy carried one operational
finding with it: duplicate-strategy changes to the jar merger are
invisible to both the up-to-date check and the build cache — the
configuration is not part of the task fingerprint — so changing
this configuration means deleting the output and bypassing the
cache to get a genuine rebuild. The deprecated Gradle features note has since been investigated
and attributed: the build's single deprecation comes from the
detekt plugin, which resolves its default reports directory
through the deprecated reporting extension method at apply time —
its bytecode says so, and the census and isolation runs exonerate
every other root plugin, the version-checking plugin and this
tree's own scripts. Nothing can be fixed in-tree while the
plugin's current release is also its latest published one; the
summary line stays as expected noise, the upgrade report watches
for the next stable plugin release, and the note becomes
mandatory only when the project targets the next major Gradle
release.

## Version 9 outlook — the class loading lifecycle

The class loading helper backing the conditional `loadClass` and
`loadObject` helpers closes the archive loader once the requested class
is loaded: the loader keeps the underlying archive open, and closing it
releases the file. The loaded class, however, keeps referencing its
defining loader forever, and the JVM resolves classes lazily — a class
referenced only inside a method body is looked up through the defining
loader at the moment the method first runs. A closed loader can no
longer serve such lookups, so an archive with more than one class
fails with a class-not-found error at the first call reaching a
companion class. The failure is selective: static initializers run
while the loader is still open, so simple classes pass every test.

The trade-off only exists on Windows, where an open archive handle
locks the file; on Linux and macOS an open file can be deleted at any
time. Three resolutions work on all three platforms:

- keeping the loader open for the duration of the run, the ecosystem
  default for plugin-style loading: multi-class archives resolve
  correctly, and the user's archive stays locked on Windows for one
  arara run only;
- copying the archive to a temporary file once and loading from the
  copy: the user's archive is never locked on any platform, and the
  disposable copy is cleaned up by the operating system's temporary
  file policy;
- an in-memory loader reading the archive bytes once: no file locks
  anywhere, at the cost of a small custom class loader to maintain.

Resolved: the technical leads approved the resolution, and the
long-lived embedding consideration — a daemon holding a user archive
locked indefinitely is a nuisance no argument about short command line
runs can excuse — settled the variant choice. The temporary-copy design
became the primary: each load copies the archive to a private
temporary file, the class loader operates on the copy and stays open
for the lifetime of the JVM (lazy resolution of companion classes
keeps working), the user's archive is never held open on any platform,
and a shutdown hook closes the loaders and deletes the copies at exit.
Repeated loads of the same archive deliberately create independent
loaders, so a rebuilt archive is served fresh on the next call.

The safe-run asymmetry is closed as part of the same resolution: both
class loading helpers reject safe mode with the same contract the
system command helper upholds, mirroring its guard placement and
message style. The safe mode promise now covers the most powerful
escape hatch as well.

## Build modernization record

The post-migration housekeeping continues with a planned sequence of
small, verified build steps. The decisions so far:

- **The smoke suite is codified.** `scripts/smoke.sh` builds the runnable
  jar, provisions the scratch matrix from the repository fixtures (the
  three authored scenarios — the file-database double run and the
  official rule through both dialects — are versioned in `scripts/smoke`,
  alongside the official rule pair staged under its installed naming),
  discovers the jar by pattern instead of a version-pinned name, and runs
  the full scenario set on the underlying JVM and again on a Java 11
  runtime. The floor runtime is discovered among the toolchain caches,
  the environment or the path, and provisioned on demand from the Adoptium
  API into a user cache that survives temporary-directory wipes. A
  dedicated provisioning plugin was considered and shelved in favour of
  the API path.
- **The Java floor stays at 11.** The dependency graph's real bytecode
  ceiling is kotaml at exactly class file 55; kotlin-logging ships Java 8
  classes with a root-level `module-info.class` at major 61 — an inert
  descriptor on the class path that a compatibility checker misread as a
  Java 17 requirement (reported upstream, fixed by GradleUp/tapmoc#109).
  Nothing in the graph requires more than Java 11.
- **Compatibility wiring goes through tapmoc.** Its `java(11)` and
  `kotlin(2.4.0)` calls replace the manual jvmTarget, apiVersion,
  languageVersion and release wirings; the Kotlin pins move to the
  compiler's own minor; the toolchain blocks vanish (the daemon JVM
  compiles release-11 bytecode, and the tests run with it). The
  dependency check stays off until the module descriptor misreading
  that produced false positives is fixed in a newer stable release —
  the upgrade report will surface it, and re-enabling means adding
  checkDependencies() to the same wiring block, where the build
  script carries the note. Until then the smoke's Java 11 leg carries
  the floor verification alone. The API surface probe ran with the
  wiring: the compiler rejects JDK-16-and-later API calls at compile
  time, so the check's stable arrival is the only outstanding
  condition before the leg retires from load-bearing.
- **Coverage is measured** with Kover: the plugin applies to every
  module and the root aggregates them — the per-module application is
  load-bearing, as the failed root-only attempt proved, because the
  aggregate resolution cannot match external artifacts without the
  plugin's variant attributes. The first baseline: 77.6% lines and
  75.3% instructions overall, with branches at 55.5% as the weak spot
  — the frozen public API module sits at 89.8% lines, and the legacy
  rule module shows 68.2% lines despite having no test sources of its
  own, because the fixture-driven command line suite exercises its
  parser in-process; its 29.3% branch coverage is the lowest in the
  tree. Thresholds become a verification gate once the baseline
  settles. The leopack patterns landed with one deliberate reversal:
  the publish warning is gated on the requested task names (and the
  property spelling unified on the lowercase form the publication
  actually reads — the root had been checking a differently-cased key
  than the one feeding the credentials), the shared license-header
  delimiter covers both Kotlin file kinds, and detekt parses against
  the build's JVM target. The fork freeze was considered and rejected:
  the compatibility guards own that question mechanically, so the
  forks flow freely. The one pin the report does carry is the
  standard library — its version is managed by the compatibility
  plugin, and the report's suggestion to update its requested pin was
  observed as noise and silenced by the recipe. The header machinery
  enforced its own convention on landing: the comment recording why
  the helper build's repositories declaration is load-bearing sat
  above the plugins block, inside the replaceable header region, and
  the first formatting run consumed it together with the declaration —
  the audit diff caught it before the commit, and the block now lives
  below the delimiter boundary where nothing can eat it.
- **LeoPack is the release-phase packaging candidate** (CTAN/TDS archives
  from the shadow jar, the launcher and the rules directory), alongside
  sources and javadoc jars for the publication.
- **Dependency declarations live with their consumers.** The four
  modules with test sources declare the Kotlin test artifact next to
  their own dependencies through catalog references, the command line
  module's reflection dependency is a catalog entry pinned to the same
  version key the Kotlin plugins use, and core declares the
  serialization artifact its sources compile against instead of riding
  a transitive path through the YAML parser. The root's cross-project
  dependency block and its workaround comment are gone, the mvel module
  carries no test dependency at all (it has no test sources), and the
  build helper source lost the catalog import nothing consumed. Its
  repositories declaration stays — a correction the build itself
  enforced: the Kotlin plugin's own stdlib and reflect coordinates
  resolve through it, so the declaration is load-bearing even though
  the helper build declares no dependencies of its own.

The queue past the current sequence, recorded for the next planning
rounds:

- **The smoke floor task** — landed: a task in the helper build holds
  the policy (the two explicit properties, deliberately separate from
  the build target, with a loud divergence notice that never fails
  the run), while the build script resolves the runtime through the
  toolchain service. The step taught three things, recorded with it:
  the vendor matching is a substring test against the JVM's
  self-reported vendor, and Temurin builds of every era report
  "Eclipse Adoptium", so the property names the vendor the runtime
  actually says, not the product branding; the toolchain resolution
  demands a full installation carrying the compiler binaries, so a
  runtime-only download never satisfies it; and the launcher service
  sits behind a pair of overloads the receiver-style action resolves.
  Detection is equally honest: the build sees its own provisioned
  installs and the standard system locations, while hand-placed
  runtimes in nonstandard homes do not exist to it — which is the
  point, as the suite depends on what the build provisions, not on
  what a machine happens to carry. The smoke prelude provisions
  through the task when no local runtime matches, the separate
  download path is gone, and the helper build keeps no catalog until
  its first real dependency; when one lands, the catalog is its own,
  never the application's.
- **The Bats-shaped suite** — landed: the scenario definitions live in
  the helper build as a data table rendered through the generator
  library, and one Gradle task became the single entry point — jar
  built, runner files generated, scratch tree and floor runtime
  provisioned, the runner probed before any work, with per-leg
  reports kept as build artifacts for the eventual continuous
  integration. The prelude script retired with them. Two design
  reversals are recorded with the step: the generator stayed in the
  helper build after all, because the churn that justified a separate
  logic build was hypothetical — the scenario table is data rather
  than churning code, and one cold build per rare edit prices the
  whole question — and the upstream vocabulary gap never
  materialized: per-file setups carry the working directories, the
  double-run sequences run and assertion pairs, and skip
  orchestration lives with the runner, which knows which scenarios
  need which tools, announces every skip with its reason, and
  escalates them all in strict mode. The floor leg consumes the same
  resolution the runtime task prints, so a missing floor degrades to
  a loud skip instead of failing the build. The upstream anchoring
  patch has since shipped, and the helper build tracks it: the
  generated setup verifies its precondition, resolves its helpers
  relative to the test file, and hardened its clone guards, so the
  runner's working-directory choice became a preference rather than
  a fix — proven by invoking the runner from an unrelated directory,
  the exact shape that failed before the patch. The matrix has since
  grown back toward its historical breadth: the scenario table is
  now data-driven end to end — fixture and authored sources ride
  each row, so a scenario is a row and nothing else — and the rows
  restored the default engine's conditional dialect, session state
  in both engines, the halt directive's pinned error exit, dispatch
  by file extension with its rejection path, and a TeX-free
  file-database double run whose fixture carried a machine-specific
  committed database defused by the per-run cleanup. Every row was
  asserted from observed output first, exit codes included.
- **The tolerated findings retire** — landed: the deliberate throw in
  the directive conditional mapping became the idiomatic error call,
  the interpreter's project file parsing split into single-throw
  helpers so every function states its own failure, and the
  conversion object carries a justified suppression at its
  declaration — the count is the shape of a single typed boundary,
  and the census that tried every split found either a boundary
  scattered or a fragment left at the threshold. The tolerance entry
  left the lint configuration with them, so any new finding now
  fails the build, and the gate expectation became an exact zero.
  The scenario renderer's escape noise left alongside, replaced by
  two one-line command builders — one of them the multi-dollar raw
  string the license heredocs already use — with the regenerated
  runner files proven byte-identical.
- **The rule-formatting task families are symmetric** — landed: the
  YAML rule step left the built-in extension spelling for the
  generic format block, so both families read the same —
  `spotlessYamlRules*` beside `spotlessLuaRules*` — with the
  on-demand gating re-pointed to the symmetric names and proven
  under both the default and the validation-flag regimes against
  the actual rule artifacts.
- **The copyright years are maintained by the build** — landed: the
  license and every rule now carry the interval form, 2012 to the
  current year, with the separator standardized on the hyphen and
  the converter emitting the same interval dynamically so a
  regenerated twin agrees with the convention by construction. The
  division of labor is layered: the formatter owns the header shape
  — its year placeholder is the interface between the tools — and
  the header plugin owns the clock, refreshing the interval ends on
  demand. Two findings are recorded with the step: the formatter
  preserves each file's own year when rewriting headers into the
  interval shape, so the normalization needed the clock task to
  converge both families — and its second run, reporting zero
  changes, is the fixed-point proof. And the header task calls the
  project at execution time, which the configuration cache forbids;
  the task runs with the cache disabled until the plugin stores its
  resolved state at configuration time. A test pinning the license
  checksum as a stable file was caught by the license edit and
  updated with the new value — the pin now guards the new text. The
  configuration-cache finding has since been fixed upstream and the
  tree tracks the patched release: the header task now runs from
  the stored cache entry like every other task, and the documented
  invocation dropped its cache-bypassing flag. The checksum test,
  which had pinned the license as its stable file, moved to an
  immutable fixture in the test resources — the license is now
  a mutable-by-design file, refreshed by the clock task, and the
  pin's job is proving the hash function against a file that never
  changes.
