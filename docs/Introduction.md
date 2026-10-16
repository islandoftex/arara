+++
title = "Introduction"
description = "What arara is, how it thinks, and where this manual is going"
weight = 1
+++

Hello there, welcome to arara, the cool TeX automation tool! This chapter is
actually a quick introduction to what you can (and cannot) expect from
arara. For now, concepts will be informally presented and will be detailed
later on, in the next chapters.

For an existing workflow, [migration post](@/news/2027-02-07-Release-v8.md#migrating-from-arara-7) collects
the changes to rules, configuration and project specifications. Developers
embedding the tool can start with [arara as a library](@/manual/Library.md).

For a first workflow, start with the [quickstart guide](@/Quickstart.md).
This reference manual describes the features and rule interfaces in detail.

# What is this tool?

Good question! arara is a TeX automation tool based on rules and directives. It
is, in some aspects, similar to other well-known tools like `latexmk` and
`rubber`. The key difference (and probably the selling point) might be the fact
that arara aims at explicit instructions in the source code (in the form of
comments) in order to determine what to do instead of relying on other
resources, such as log file analysis. It is a different approach for an
automation tool, and such a design brings both advantages and disadvantages.
Let us use the following file `hello.tex` as an example:

```tex
\documentclass{article}

\begin{document}
Hello world!
\end{document}
```

How would one successfully compile `hello.tex` with `latexmk` and `rubber`, for
instance? It is quite straightforward: it is just a matter of providing the file
to the tool and letting it do the hard work:

```sh
$ latexmk -pdf hello.tex
$ rubber --pdf hello.tex
```

These tools perform an analysis on the file and decide what has to be
done. However, if one tries to invoke `arara` on `hello.tex`, I am afraid
*nothing* will be generated; the truth is, arara does not know what to do with
your file, and the tool will even raise an error message complaining about this
issue:

```sh
$ arara hello.tex
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

arara 8.0.0, the cool TeX automation tool.

Processing "hello.tex" (size: 70 B, last modified: 2026-10-09 08:57:17), please
wait.

ERROR -------------------------------------------------------------------------
It looks like no directives were found in the provided file. Make sure to
include at least one directive and try again.

TOTAL FAILURE · 1 file, 0 tasks

Elapsed time: 0.1035 seconds
```

Quite surprising. However, this behaviour is not wrong at all, it is completely
by design: arara needs to know what you want. And for that purpose, you need to
tell the tool what to do.

{% <devlab.callout type="info" title="A very important concept"> %}
That is the major difference of arara when compared to other tools: *it is not
an automatic process and the tool does not employ any guesswork on its own*. You
are in control of your documents; arara will not do anything unless you *teach
it how to do a task and explicitly tell it to execute the task*.
{% </devlab.callout> %}
Now, how does one tell arara to do a task? That is actually the easy part,
provided that you have everything up and running. We accomplish the task by
adding a special comment line, hereafter known as *directive*, somewhere in our
`hello.tex` file (preferably in the first lines):

```tex
% arara: pdflatex
\documentclass{article}

\begin{document}
Hello world!
\end{document}
```

For now, do not worry too much about the terms, we will come back to them later
on, in [Important concepts](Concepts.md). It suffices to say that arara expects
*you* to provide a list of tasks, and this is done by inserting special comments
in the source file. Let us see how arara behaves with this updated code:

```sh
$ arara hello.tex
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

arara 8.0.0, the cool TeX automation tool.

Processing "hello.tex" (size: 88 B, last modified: 2026-10-09 08:57:22), please
wait.

(PDFLaTeX) PDFLaTeX engine ............................................ SUCCESS

TOTAL SUCCESS · 1 file, 1 task

Elapsed time: 0.5566 seconds
```

Hurrah, we finally got our document properly compiled with a TeX engine by the
inner workings of our beloved tool, resulting in an expected `hello.pdf` file
created using the very same system call that typical automation tools like
`latexmk` and `rubber` use. Observe that arara works practically on the other
side of the spectrum: you need to tell it how and when to do a task.

# Core concepts

When adding a directive in our source code, we are explicitly telling the tool
what we want it to do, but I am afraid that is not sufficient at all. So far,
arara knows *what* to do, but now it needs to know *how* the task should be
done. If we want arara to run `pdflatex` on `hello.tex`, we need to have
instructions telling our tool how to run that specific application. This
particular sequence of instructions is referred to as a `rule` in our context.

{% <devlab.callout type="info" title="Note on rules"> %}
Although the core team provides a lot of rules shipped with arara out of the
box, with the possibility of extending the set by adding more rules, some users
might find this decision rather annoying, since other tools have most of their
rules hard-coded, making the automation process even more transparent. However,
since arara does not rely on a specific automation or compilation scheme, it
becomes more extensible. The use of directives in the source code makes the
automation steps more fluent, which makes specifying complex workflows much
{% </devlab.callout> %}

Despite the inherited verbosity of automation steps not being suitable for small
documents, arara really shines when you have a document which needs full
control of the automation process (for instance, a thesis or a manual). Complex
workflows are easily tackled by our tool.

Rules and directives are the core concepts of arara: the first dictates how a
task is done, and the latter is the proper instance of the associated rule on
the current document, i.e., when and where the commands must be executed.
A rule is not limited to running commands silently: it can hold a
conversation with the person at the terminal — asking a question, offering
a choice, showing a warning — and branch on the answer it receives.

{% <devlab.callout type="tip" title="The name"> %}
*Do you like araras? We do, especially our tool, which shares its name with
this colorful bird — and since this manual carries no photographs, you can
meet our noisy, naughty namesake on the [project's home
page](https://islandoftex.gitlab.io/arara/), where the bird lives as the brand
it always was.*

The tool name was chosen as an homage to a Brazilian bird of the same name,
which is a macaw. The word *arara* comes from the Tupian word *a'rara*, which
means *big bird* (much to my chagrin, Sesame Street's iconic character Big Bird
is not a macaw; according to some sources, he claims to be a golden
condor). Araras are colorful, noisy, naughty and very funny. Everybody loves
araras. The name seemed catchy for a tool and, in the blink of an eye, arara was
quickly spread to the whole TeX world.
{% </devlab.callout> %}

Now that we informally introduced rules and directives, let us take a look on
how arara actually works given those two elements. The whole idea is pretty
straightforward, and I promise to revisit these concepts later on in this
manual for a comprehensive explanation (more precisely, in
[Important concepts](Concepts.md)).

First and foremost, we need to add at least one instruction in the source code
to tell arara what to do. This instruction is named a *directive* and it will
be parsed during the preparation phase. Observe that arara will tell you if no
directive was found in a file, as seen in our first interaction with the tool.

An arara directive is usually defined in a line of its own, started with a
comment (denoted by a percent sign in TeX and friends), followed by the word
`arara:` and the task name:

```tex
% arara: pdflatex
\documentclass{article}
...
```

Our example has one directive, referencing `pdflatex`. It is important to
observe that the `pdflatex` identifier *does not represent the command to be
executed*, but *the name of the rule associated with that directive*.

Once arara finds a directive, it will look for the associated *rule*. In our
example, it will look for a rule named `pdflatex` which will evidently run the
`pdflatex` command line application. A rule is a small Lua script named after
its identifier followed by the `lua` extension, and it follows a strict
structure that we will study together later on.

{% <devlab.callout type="info" title="A word on the two dialects"> %}
Long-time users will remember rules written in YAML: that format is now
deprecated, and version 8 tells you so whenever it is used, but it still works
while the transition lasts. The Lua format is where the future lives, and this
manual teaches it first. The whole story, including the migration of old rules,
awaits in [Engines](Engines.md) and [Migration](Migration.md).
{% </devlab.callout> %}

Now, we have a queue of pairs *(directive, rule)* to process. For each pair,
arara will map the directive to its corresponding rule, evaluate it and run the
proper command. The execution chain requires that command *i* was successfully
executed to then proceed to command *i + 1*, and so forth. This is also
by design: arara will halt the execution if any of the commands in the queue
raised an error. How does one know if a command was successfully executed?
arara checks the corresponding *exit status* available after a command
execution. In general, a successful execution yields 0 as its exit status.

In order to decide whether a command execution is successful, arara relies on
exit status checking. Typically, a command is successful if, and only if, its
resulting exit status is 0 and no other value. However, we can define any value,
or even forget about it and make it always return a valid status regardless of
execution (for instance, in a rule that always is successful — see, for
instance, the `clean` rule).

That is pretty much how arara works: directives in the source code are mapped
to rules. These pairs are added to a queue. The queue is then executed and the
status is reported. More details about the expansion process are presented in
[Important concepts](Concepts.md). In short, we teach arara to do a task by
providing a rule, and tell it to execute it through directives in the source
code.

# The manual at a glance

Since version 8 did quite a lot of growing up, this manual grew alongside it.
Here is where we are going, chapter by chapter:

* [Important concepts](Concepts.md) — directives, rules, file types, sessions,
  and the vocabulary the whole manual speaks.
* [The command line](CommandLine.md) — every flag arara understands, including
  the ones that choose how the tool looks and which engine it runs.
* [Writing rules](Rules.md) — the Lua rule format, from a first simple rule to
  the typed helper boundary.
* [Configuration](Configuration.md) — both configuration formats, paths,
  file types, preambles, and the keys that rename the log and the database.
* [Engines](Engines.md) — the engine model, conditionals in both dialects,
  interpolation, and the YAML deprecation told honestly.
* [Projects](Projects.md) — the invocation as a set of projects: specification
  files, dependencies, priorities, working directories, and the anonymous
  project that carries your loose files.
* [The console](Console.md) — the plain and the styled presentations, and the
  live feedback a styled run carries while a task works.
* [Logging](Logging.md) — the log file's whole life, from the engine line to
  the session's every mutation.
* [The file database](Database.md) — how arara remembers which files changed,
  and how to bend that memory to your will.
* [Building and deploying](Building.md) — installing arara, getting it into
  TeX distributions, and building it from sources.
* [Migration](Migration.md) — moving from version 7, converting old rules,
  and the deprecation timeline.
* [arara as a library](Library.md) — for the developers among us: embedding
  the tool in your own Kotlin and Java applications.

# Operating system remarks

The application is written using the Kotlin language, so arara runs on top of a
Java virtual machine, available on all the major operating systems — in some
cases, you might need to install the proper virtual machine. We tried very
hard to keep both code and libraries compatible with older virtual machines or
from other vendors. Currently, arara is known to run on Java 11 to 27, from any
vendor.

{% <devlab.callout type="warning" title="Outdated Java virtual machines"> %}
Dear reader, beware of outdated software, mainly Java virtual machines!
Although arara offers support for older virtual machines, try your best to
keep your software updated as frequently as possible. The legacy support exists
only for historical reasons, and also due to the sheer fact that we know some
people that still run arara on very old hardware. If you are not in this
particular scenario, get the latest virtual machine.
{% </devlab.callout> %}

In [Building and deploying](Building.md), we provide instructions on how to
build arara from sources using Gradle. Even if you use multiple operating
systems, arara should behave the same, including the rules. Helper functions
are available for system-specific rules based on the underlying operating
system.

# Support

If you run into any issue with arara, please let us know. We all have very
active profiles in the [TeX community at
StackExchange](https://tex.stackexchange.com/), so just use the `arara` tag in
your question and we will help you the best we can (also, take a look at their
[starter guide](https://tex.meta.stackexchange.com/q/1436)).

We also have a
[Matrix](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org)
chat room, in which we occasionally hang out. Also, if you think the report is
worthy of an issue, open one in our
[GitLab repository](https://gitlab.com/islandoftex/arara/issues).

We really hope you like our humble contribution to the TeX community. Let arara
enhance your TeX experience; it will help you when you need it the most.
Enjoy the manual.
