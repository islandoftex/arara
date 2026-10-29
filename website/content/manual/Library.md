+++
title = "arara as a library"
description = "Modules and responsibilities of an embedding application"
weight = 16
+++

The command line application is built on modules which can also be used from
Kotlin or Java. This chapter introduces those modules and the responsibilities
of a host application. Detailed types and method signatures belong to the API
reference supplied with the libraries.

# Modules and artifacts

The source project uses Kotlin Multiplatform, with JVM-only targets at present.
All application modules require Java 21 or later.

| Module | Purpose |
| --- | --- |
| `api` | Path, project, rule, execution, localization, reporting and host contracts |
| `core` | Executor, configuration, sessions, file database, rule lookup, diagnostic analysis and locale implementations |
| `lua` | Lua rules, configuration and project specifications |
| `mvel` | YAML rule/configuration loading and MVEL expression evaluation |
| `cli` | Command-line configuration, rule routing and terminal presentation |

The Maven group is `org.islandoftex.arara`. JVM coordinates use artifact names
such as `arara-core-jvm`; the corresponding Multiplatform publication is
`arara-core`. Core exposes its API dependency transitively. Each module's
publication includes sources and HTML API documentation under the `javadoc`
classifier. Use a published version and the Maven repository supplying it.

For development against a local checkout, `./gradlew checkPublications` stages
a Maven repository under `build/publication-check/repository` and checks it
with an independent Kotlin consumer. A consuming JVM build can depend on
`org.islandoftex.arara:arara-core-jvm:VERSION`, replacing `VERSION` with the
version staged by that checkout. Add the staged repository and Maven Central
to that build's repositories.

Generate the combined API reference with:

```sh
./gradlew dokkaGeneratePublicationHtml
```

The entry point is `build/dokka/html/index.html`. For the complete command-line
workflow, use the runnable application described in
[Building arara](@/manual/Building.md).

# Paths and file-change checks

Use `MPPPath` for file operations. Its string representation and JVM path
accessors are absolute and normalized. `suppliedSpelling` exposes the supplied
text for display or textual matching. Native equality and resolution semantics
are retained; symbolic links are not collapsed when comparing paths. The
separate `ForeignPath` API provides explicit POSIX or Windows path operations
without filesystem access.

For example, with an existing `main.tex`, a program can use the same file-change
checks as a rule:

```kotlin
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.files.FileHandling

fun main() {
    val workingDirectory = MPPPath(".")
    val options = ExecutionOptions()
    val source = workingDirectory.resolve("main.tex")
    val database = workingDirectory.resolve(options.databaseName)
    println(FileHandling.hasChanged(source, database))
}
```

This check records the checksum immediately, as described in
[The file database](@/manual/Database.md). The host's logging backend is its own
choice; core supplies the SLF4J API runtime dependency.

# Execution and host capabilities

`LinearExecutor.execute(projects)` orders projects by their dependencies and
files by priority, returning an `ExecutionReport` with the exit code and timing.
`ExecutorHooks` provides callbacks around the run, projects and files, and a
`processDirectives` hook before directive execution.

A host must also supply executable directives. The default factories in
`Directives.hooks` do not construct them: set `DirectiveFetchingHooks` when
using core's text extractor, or provide a `ProjectFile` implementation whose
`fetchDirectives` returns your own `Directive` objects. The CLI supplies its
own factories and interpreter routing. Setting options and calling the core
executor alone does not install that command-line wiring.

The dialog contract is `DialogRenderer`. The default `HeadlessDialogRenderer`
reports an error if a rule asks for interaction. Install a renderer through
`Dialogs.renderer` when the host provides dialogs; the choice and cancellation
conventions are described in [Methods](@/manual/Methods.md#dialog-boxes).

The session, executor options, hooks and dialog renderer are shared state within
the process. Save and restore values changed by an embedding, and serialize runs
using this state rather than treating the executor as an independent instance
per request. Successful class loaders stay available for lazy class and resource
loading; call `ClassLoading.closeLoaders()` when the host no longer needs them.

The CLI closes the console renderers it creates and restores the host's renderer.
A host using a `StyledConsoleRenderer` directly must call `close()` when finished
to stop live progress and release its timer. Call `suspendProgress()` before
printing through another component or opening a custom dialog.

Hosts using the CLI's native terminal detection should put JNA on the module
path and use `--add-modules=com.sun.jna --enable-native-access=com.sun.jna`.
The packaged arara launchers supply these options.
