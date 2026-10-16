+++
title = "arara as a library"
description = "Embedding the tool in your own Kotlin and Java applications"
weight = 13
+++

Everything the previous chapters showed through the command line —
resolving files, building projects, interpreting rules, running tasks —
is a library surface underneath. The arara project publishes its
modules for exactly that purpose, so your application can embed the
tool, drive its executor, and present the run through your own renderer.
This chapter is for the developers among us.

# The modules

The project builds five modules, and four of them publish:

- **`api`** — the public, stable interface: the models and contracts
  every embedding speaks. The module builds with explicit API mode, so
  its surface is deliberate, documented, and versioned — what it
  exposes is what you may build on.

- **`core`** — the implementations: the linear executor and its hooks,
  the file resolution and searching, the file database, the session, the
  localization machinery, the headless dialog default.

- **`mvel`** — the deprecated YAML rule engine and its MVEL conditionals,
  required while the transition lasts.

- **`lua`** — the Lua rule engine, the format the future speaks.

- **`cli`** — the application itself, published only as the runnable jar
  you know from [Building and deploying](Building.md); it is not a
  library, and it wires the others together.

The published modules ship the conventional artifact set — the compiled
jar, its sources jar, and the documentation — under the project's
coordinates, which the repository's build configuration carries.

# The API surface

The `api` module holds the types every embedding touches:

- **`MPPPath`** — the path model, and the one genuinely sharp edge of
  the surface. A path carries *two* spellings: the provided path,
  exactly as the caller wrote it — backing the absolute checks and the
  resolution — and the reference path, absolutized and normalized —
  backing the string representation, the I/O and, importantly, the
  equality. `MPPPath("a") == MPPPath("/abs/cwd/a")` holds true. Build
  your comparisons on the model's own equality, never on string
  spellings.

- **`MPPLocale`** — the locale representation, identified by a BCP 47
  tag, the only localization type the API surface carries.

- **`FileType`** — the extension-and-pattern pair taught in
  [Important concepts](Concepts.md).

- **`Command`** — the high-level system command model the rules build.

- **`Session`** — the key-and-value contract of the run's shared memory,
  with `get`, `put`, `contains`, `remove` and `clear`.

- The **options models** — `ExecutionOptions`, `UserInterfaceOptions`,
  `LoggingOptions` — carrying the run's configuration, the same keys the
  [Configuration](Configuration.md) chapter documents.

- **`AraraException`** and its family — the tool's own exceptions, so
  embedders can distinguish the tool's verdicts from their own errors.

# A minimal embedding

An embedding drives the same machinery the command line drives:
configure the options, resolve a file, build the project, wire the
hooks, and ask the linear executor to run — in Kotlin:

```kotlin
// the run's configuration, the same keys the configuration file carries
val options = ExecutionOptions()

// resolve the file against a working directory, as the command line does
val workingDirectory = MPPPath("/home/islandoftex/project")
val file = FileSearching.resolveFile("main.tex", workingDirectory, options)

// a project is a name, a working directory and its files — the model
// the Projects chapter teaches
val project = Project(
    "My Project", workingDirectory, setOf(file)
)

// the hooks observe the run: before and after the whole execution,
// each project, and each file — with the file's own report in hand
LinearExecutor.hooks = ExecutorHooks(
    executeBeforeExecution = { println("The run starts.") },
    executeAfterFile = { report ->
        println("A file finished with exit code ${report.exitCode}.")
    }
)

// the run: the report carries the exit code the process would take
val report = LinearExecutor.execute(listOf(project))
```

The executor is linear by design — files run in order, projects in
their resolved order — and the `ExecutionReport` answers with the run's
verdict. Everything the console showed you is available to your
application through the same hooks: the file processing lines, the
directive lists, the command results.

{% <devlab.callout type="warning" title="The shared state is shared"> %}
The session, the execution options and the executor's hooks are
per-JVM global state — the same design the command line relies on. An
embedding that runs several projects through one executor shares that
state deliberately, exactly as the command line does when a
specification merges several projects into one run.
{% </devlab.callout> %}

# The seams your application owns

The library deliberately leaves three decisions to the embedding
application:

- **The console presentation.** The command line resolves a renderer
  from the presentation names; a library embedding can do the same, or
  implement the renderer contract itself — every line the run prints
  flows through the renderer seam, so your application can present the
  run through its own UI, or through no UI at all.

- **The dialogs.** Core ships a headless dialog default that assumes no
  interactive surface, so an unwired embedding fails loudly when a rule
  holds a conversation — no answer is an answer, and the default does
  not invent one. Wire your own dialog renderer, and the conversation
  reaches your users wherever they are.

- **The engine.** The rule engines are selected as the command line
  selects them, and both are on the classpath of whichever module you
  depend on — depend on `lua` alone, and the deprecated engine never
  loads.

The logging, too, follows the seam: the `LoggingOptions` the embedding
configures decide whether a log file is written, where it lands, and
whether a fresh run truncates it or appends — the standing choice the
command line does not expose, available to the embedding application.

That is the library in one view: four published modules, an explicit
and stable API, a linear executor with observable hooks, and three seams
— presentation, dialogs, engine — that belong to your application. For
the surface of a specific type, the generated API documentation ships
with every published release; for the semantics, this manual is the
reference, because the command line and the embedding are the same
machine wearing two faces.
