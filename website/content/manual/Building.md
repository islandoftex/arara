+++
title = "Building arara"
description = "Building arara"
weight = 9
+++

arara is a Kotlin application licensed under the [New BSD
License](http://www.opensource.org/licenses/bsd-license.php), a verified
GPL-compatible free software license, and the source code is available in the
project repository at [GitLab](https://gitlab.com/islandoftex/arara). This
chapter provides detailed instructions on how to build our tool from source.

# Requirements

In order to build our tool from source, we need to ensure that our development
environment has the minimum requirements for a proper compilation. Make sure the
following items are available:

* On account of our project being hosted at [GitLab](https://gitlab.com/), an
  online source code repository, we highly recommend the installation of `git`,
  a version control system for tracking changes in computer files and
  coordinating work on those files among multiple people. Alternatively, you can
  directly obtain the source code by requesting a [source code
  download](https://gitlab.com/islandoftex/arara/-/archive/master/arara-master.zip)
  in the repository. In order to check if `git` is available in your operating
  system, run the following command in the terminal (version numbers might
  vary):

  ```sh
  $ git --version
  git version 2.29.2
  ```

    Please refer to the [git project website](https://git-scm.com/) in order to
    obtain specific installation instructions for your operating system. In
    general, most recent Unix systems have `git` installed out of the
    shelf.

* Our tool is written in the Kotlin programming language, so we need a proper
  Java Development Kit, a collection of programming tools for the Java
  platform. Our source code is known to be compliant with several vendors,
  including Oracle, OpenJDK, and Azul Systems. In order to check if your
  operating system has the proper tools, run the following command in the
  terminal (version numbers might vary):

  ```sh
  $ javac -version
  javac 1.8.0_171
  ```

    The previous command, as the name suggests, refers to the `javac` tool,
    which is the Java compiler itself. The most common Java Development Kit out
    there is from
    [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html). However,
    several Linux distributions (as well as some developers, yours truly
    included) favour the OpenJDK vendor, so your mileage may vary. Please refer
    to the corresponding website of the vendor of your choice in order to obtain
    specific installation instructions for your operating system.

* As a means to provide a straightforward and simplified compilation workflow,
  arara relies on Gradle, a software project management and comprehension
  tool. Gradle is a build tool just like ours with a much more comprehensive
  build framework to provide support for the JVM ecosystem. In order to check if
  `gradle`, the Gradle binary, is available in your operating system, run the
  following command in the terminal (version numbers might vary):

  ```sh
  $ gradle --version
  ------------------------------------------------------------
  Gradle 6.0.1
  ------------------------------------------------------------

  Build time:   2019-11-21 11:47:01 UTC
  Revision:     <unknown>

  Kotlin:       1.3.50
  Groovy:       2.5.8
  Ant:          Apache Ant(TM) version 1.10.7 compiled
                on September 1 2019
  JVM:          1.8.0_232 (Oracle Corporation 25.232-b09)
  OS:           Linux 5.5.0-1-MANJARO amd64
  ```

    Please refer to the Gradle [project website](https://gradle.org) in order to
    obtain specific installation instructions for your operating system. In
    general, most recent Linux distributions have the Gradle binary, as well the
    proper associated dependencies, available in their corresponding
    repositories.

* For a proper repository cloning, as well as the first Gradle build, an active
  Internet connection is required. In particular, Gradle dynamically downloads
  Java libraries and plug-ins from one or more online repositories and stores
  them in a local cache. Be mindful that subsequent builds can occur offline,
  provided that the local Gradle cache exists.

arara can be easily built from source, provided that the aforementioned
requirements are available. The next section presents the compilation details,
from repository cloning to a proper Java archive generation.

{% messagebox(title="One tool to rule them all") %}
For the brave, there is the [Software Development Kit
Manager](https://sdkman.io/), an interesting tool for managing parallel versions
of multiple software development kits on most Unix based systems. In particular,
this tool provides off the shelf support for several Java Development Kit
vendors and versions, as well as most recent versions of Gradle.

Personally, I prefer the packaged versions provided by my favourite Linux
distribution (Fedora), but this tool is a very interesting alternative to set up
a development environment with little to no effort.
{% end %}

# Compiling the tool

## Fetching the sources

First and foremost, we need to clone the project repository into our development
environment, so we can build our tool from source. The cloning will create a
directory named `arara/` within the current working directory, so remember to
first ensure that you are in the appropriate directory. For example:

```sh
$ mkdir git-projects
$ cd git-projects
```

Run the following command in the terminal to clone the arara project:

```sh
$ git clone https://gitlab.com/islandoftex/arara.git
```

Wait a couple of seconds (or minutes, depending on your Internet connection)
while the previous command clones the project repository hosted at GitLab. Be
mindful that this operation pulls down every version of every file for the
history of the project. Fortunately, the version control system has the notion
of a *shallow clone*, which is a more succinctly meaningful way of describing a
local repository with history truncated to a particular depth during the clone
operation. If you want to get only the latest revision of everything in our
repository, run the following command in the terminal:

```sh
$ git clone https://gitlab.com/islandoftex/arara.git --depth 1
```

This operation is way faster than the previous one, for obvious reasons.

## Building the executable

Unix terminals typically start at `USER_HOME` as working directory, so, if you
did not `cd` to another directory (as in the earlier example), the newly cloned
`arara/` directory is almost certain to be accessible from that level. Now, we
need to navigate to the directory named `arara/`. Run the following command in
the terminal:

```sh
$ cd arara
```

The previous command should take us inside the `arara/` directory of our
project, where the source code and the corresponding build file are located. Let
us make sure we are in the correct location by running the following command in
the terminal:

```sh
$ ls build.gradle.kts
build.gradle.kts
```

Great, we are in the correct location! From the previous output, let us inspect
the directory contents. The `cli/` directory, as the name suggests, contains the
source code of the main command-line application organized in an established
package structure, whereas `build.gradle.kts` is the corresponding Gradle build
file written to efficiently compile the project. In order to build our tool, run
the following command in the terminal:

```sh
$ ./gradlew :cli:build
```

Gradle is based around the central concept of a build life cycle. The compile
phase, as the name suggests, compiles the source code of the project using the
underlying Java compiler. After compiling, the code can be packaged, tested and
run. The `build` target actually compiles, tests and packages our
tool. Afterwards, you will have a `cli/build/libs/` directory with multiple JAR
files, one containing `with-deps`. That file is ready to run as it bundles all
dependencies. Subsequent builds will be significantly faster than the first
build because they do not fetch dependencies and rely on a build cache. Finally,
after some time, Gradle will output the following message as result (please note
that the entire compilation and packaging only took 4 seconds on my development
machine due to an existing local cache):

```
BUILD SUCCESSFUL in 4s
15 actionable tasks: 15 up-to-date
```

## Executing the fresh build

Now, let us move the resulting Java archive file from that particular directory
to our current directory. Run the following command in the terminal (please note
that the Java archive file was also renamed during the move operation):

```sh
$ mv cli/build/libs/arara-cli-with-deps-*.jar arara.jar
```

Now, our current directory contains the final `arara.jar` Java archive file
properly built from source. This file can be safely distributed and deployed, as
seen later on, in [Deploying](/manual/deploying). You can also test the
resulting file by running the following command in the terminal:

```sh
$ java -jar arara.jar
Usage: arara [OPTIONS] file...

    __ _ _ __ __ _ _ __ __ _
   / _` | '__/ _` | '__/ _` |
  | (_| | | | (_| | | | (_| |
   \__,_|_|  \__,_|_|  \__,_|

  The cool TeX automation tool.

  arara executes the TeX workflow you tell it to execute. Simply specify your
  needs within your TeX file and let arara do the work. These directives
  feature conditional execution and parameter expansion.

Options:
  -l, --log                        Generate a log output
  -v, --verbose / -s, --silent     Print the command output
  -n, --dry-run                    Go through all the motions of running a
                                   command, but with no actual calls
  -S, --safe-run                   Run in safe mode and disable potentially
                                   harmful features. Make sure your projects
                                   uses only allowed features.
  -w, --whole-file                 Extract directives in the file, not only in
                                   the header
  -p, --preamble TEXT              Set the file preamble based on the
                                   configuration file
  -t, --timeout INT                Set the execution timeout (in milliseconds)
  -L, --language TEXT              Set the application language
  -m, --max-loops INT              Set the maximum number of loops (> 0)
  -d, --working-directory PATH     Set the working directory for all tools
  -P, --call-property VALUE        Pass parameters to the application to be
                                   used within the session.
  --generate-completion [bash|zsh|fish]
                                   Generate a completion script for arara. Add
                                   'source <(arara --generate-completion
                                   <shell>)' to your shell's init file.
  -V, --version                    Show the version and exit
  -h, --help                       Show this message and exit

Arguments:
  file  The file(s) to evaluate and process
```

The following optional Gradle phase is used to handle the project cleaning,
including the complete removal of the `build/` directory. As a result, the
project is then restored to the initial state without any generated Java
bytecode. Run the following command in the terminal:

```sh
$ ./gradlew clean
```

This section covered the compilation details for building arara from source. The
aforementioned steps are straightforward and can be automated in order to
generate snapshots and daily builds. If you run into any issue, please let us
know. Happy compilation!
