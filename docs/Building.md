+++
title = "Building and deploying"
description = "Getting arara, giving it a home, and wrapping it as a command"
weight = 11
+++

arara is a Kotlin application licensed under the [New BSD
License](https://www.opensource.org/licenses/bsd-license.php), and the
source code is available in the project repository at
[GitLab](https://gitlab.com/islandoftex/arara). This chapter covers two
journeys: building the tool from source, and deploying it — from either
the official package or a personal build — so that `arara` becomes an
actual command in your shell.

# Requirements

The build asks for three things:

- **`git`**, for obtaining the sources — or the source code download from
  the repository page, no questions asked:

  ```sh
  $ git --version
  git version 2.50.0
  ```

- **A Java Development Kit.** The tool *runs* on Java 11 to 27, from any
  vendor — but the *build* is a Gradle build, and Gradle itself needs 17
  or newer, so pick a JDK from 17 up and both truths hold at once:

  ```sh
  $ javac -version
  javac 21.0.12
  ```

- **No Gradle installation.** The repository carries the Gradle wrapper,
  `./gradlew`, which provisions the build tool itself — the first build
  downloads Gradle behind the scenes. Check it like this:

  ```sh
  $ ./gradlew --version
  ```

An active Internet connection is required for the clone and the first
build — Gradle dynamically downloads libraries and plugins into a local
cache — and subsequent builds can occur offline, provided the cache
exists.

{% <devlab.callout type="tip" title="One tool to rule them all"> %}
For the brave, there is the [Software Development Kit
Manager](https://sdkman.io/), a tool for managing parallel versions of
several development kits on most Unix systems — several JDK vendors and
versions included. Personally, I prefer the packaged versions provided by
my favourite Linux distribution, but this tool sets up a development
environment with little to no effort.
{% </devlab.callout> %}

# Building the tool

Clone the repository — the shallow variant is faster when the history is
not the point:

```sh
$ git clone https://gitlab.com/islandoftex/arara.git
$ cd arara
```

Inside, the sources live in the module directories — `api`, `core`,
`mvel`, `lua` and `cli` — and the build runs through the wrapper:

```sh
$ ./gradlew :cli:build
```

The `build` target compiles, tests and packages the tool. Afterwards, the
`cli/build/libs/` directory holds the result: a Java archive whose name
carries `with-deps`, ready to run because it bundles every dependency.
Subsequent builds are significantly faster than the first, as they rely
on the local cache:

```sh
$ ./gradlew :cli:shadowJar
BUILD SUCCESSFUL in 43s
```

The version of the built tool derives from the project's changelog —
between releases, it reads `8.0.0-SNAPSHOT`, and at a release it reads
the release's own number. Test the fresh build with a version query:

```sh
$ java -jar cli/build/libs/arara-cli-with-deps-*.jar --version
```

And when the generated files should leave the tree alone:

```sh
$ ./gradlew clean
```

# Deploying the tool

arara is a portable application: provided the underlying operating system
has a proper Java virtual machine, it can live anywhere — a system
directory, your home, a USB drive, even a floppy disk. The one
convention that matters is the directory structure, because the tool
derives its default rule path from where its own heart beats:

```text
arara/                  ← ARARA_HOME, the tool's home
├── arara.jar           ← the heart and soul
└── rules/              ← the default rule pack, both dialects
    ├── arara-rule-pdflatex.yaml
    ├── arara-rule-pdflatex.lua
    └── ...
```

The `ARARA_HOME` alias refers to the directory containing `arara.jar`,
and the `rules/` directory beside it is the default rule search path —
every official rule, shipped in both the YAML and the Lua formats. As
long as this structure is honoured, the tool works off the shelf.

## The official package

The official arara package is available in the artifacts section of the
project repository — a CTAN package, generated at the tags, carrying the
tool, the rules and the documentation. Download and extract it into your
chosen location:

```sh
$ unzip arara.zip
$ unzip arara.tds.zip
$ mv scripts/arara .
```

The result is the `arara` directory with exactly the structure above,
ready to move wherever the tool should reside:

```sh
$ mv arara /opt/islandoftex/
```

If the tool was built from source instead, construct the structure by
hand: the `with-deps` archive renamed to `arara.jar`, and the `rules/`
directory from the repository beside it.

# Tool wrapping

The tool is deployed, but a Java archive wants a full invocation to run.
Three classic approaches turn it into an actual command — pick the one
that suits your shell:

- **Shell alias** — an inline string replacement, added to your
  `.bashrc`:

  ```sh
  alias arara='java -jar /opt/islandoftex/arara/arara.jar'
  ```

- **Shell function** — a small subroutine, also in `.bashrc`, with the
  `$@` variable passing every argument along:

  ```sh
  arara() {
      java -jar /opt/islandoftex/arara/arara.jar "$@"
  }
  ```

- **Script file** — a standalone entry point, no shell configuration
  touched:

  ```sh
  #!/bin/bash
  jarpath=/opt/islandoftex/arara/arara.jar
  java -jar "$jarpath" "$@"
  ```

  Save it as `arara`, make it executable, and move it to a directory the
  `PATH` carries — `/usr/local/bin/` with superuser privileges, or the
  `bin/` under your home directory without them:

  ```sh
  $ chmod +x arara
  $ sudo mv arara /usr/local/bin/
  ```

{% <devlab.callout type="info" title="Alias or function?"> %}
An alias is a string replacement, good for little more than default
options; a function is a subroutine, good for anything more. For our
tool, both are valid — the logic is straightforward either way.
{% </devlab.callout> %}

Whichever approach you choose, an `arara` command should now answer in
your shell — and a TeX distribution might already carry an arara of its
own, TeX Live and MiKTeX both know the name. Before blaming either
arara, check which one answers:

```sh
$ which arara
/usr/local/bin/arara
```

The `which` command searches the `PATH` for the executable — mind that
aliases and shell functions are listed as well, and that the wrapper you
wrote wins over the distribution's binary only where the `PATH` says so.
Happy building, happy deploying!
