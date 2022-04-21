+++
title = "Deploying the tool"
description = "Deploying the tool"
weight = 10
+++

As previously mentioned, arara runs on top of a Java virtual machine, available
on all major operating systems -- in some cases, you might need to install the
proper virtual machine. This chapter provides detailed instructions on how to
properly deploy the tool in your computer from either the official package
available in our project repository or a personal build generated from source
(as seen in [Building](/manual/building)).

# Directory structure

From the early development stages, our tool employs a very straightforward
directory structure. In short, we provide the `ARARA_HOME` alias to the
directory path in which the `arara.jar` Java archive file is located. This
particular file is the heart and soul of our tool and dictates the default rule
search path, which is a special directory named `rules` available from the same
level. This directory contains all rules specified in the YAML format. The
structure overview is presented as follows.

<img src="/arara/images/manual/structure1.svg" alt="Structure 1" width="648"/>

Provided that this specific directory structure is honoured, the tool is ready
for use off the shelf. In fact, the official arara CTAN package is available in
the [artifacts
section](https://gitlab.com/islandoftex/arara/-/jobs/artifacts/master/download?job=publish:tdszip)
of our project repository. Once the package is properly downloaded, we simply
need to extract it into a proper `ARARA_HOME` location.

# Defining a location

First and foremost, we need to obtain `master-ctan.zip` from our project
repository at GitLab, which is our CTAN package artifact. As the name indicates,
this is a compressed file format, so we need to extract it into a proper
location. Run the following commands in the terminal:

```sh
$ unzip master-ctan.zip
$ unzip arara.tds.zip
$ mv scripts/arara .
```

As a result of the previous commands, we obtained a directory named `arara` with
the exact structure presented in the previous section in our working directory
(amongst other files and directories that can be safely discarded). Now we need
to decide where arara should reside in our system. For example, I usually deploy
my tools inside the `/opt/paulo` path, so I need to run the following command in
the terminal (please note that my personal directory already has the proper
permissions, so I do not need superuser privileges):

```sh
$ mv arara /opt/paulo/
```

The tool has found a comfortable home inside my system! Observe that the full
path of the `ARARA_HOME` reference points out to `/opt/paulo/arara` since this
is my deployment location of choice. The resulting structure overview, from the
root directory, is presented as follows:

<img src="/arara/images/manual/structure2.svg" alt="Structure 1" width="896"/>

If the tool was built from source (as indicated in
[Building](/manual/building)), make sure to construct the provided directory
structure previously presented. We can test the deployment by running the
following command in the terminal (please note the full path):

```sh
$ java -jar /opt/paulo/arara/arara.jar
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

Please observe that, provided that the underlying operating system has an
appropriate Java virtual machine installed, arara can be used as a portable,
standalone application. Portable applications can be stored on any data storage
device, including external devices such as USB drives and floppy disks.

# Tool wrapping

arara is now properly deployed in our system, but we still need to provide the
full path of `arara.jar` to the Java virtual machine in order to make our tool
work. This section provides three approaches regarding the creation of a
*wrapper*, a shell feature that embeds a system command or utility, that accepts
and passes a set of parameters to that command.

- **Shell alias:** An *alias* is a command available in various shells which
    enables a replacement of a word by another string. It is mainly used for
    abbreviating a system command, or for adding default arguments to a
    regularly used command. In order to create a shell alias for our tool, open
    `.bashrc` (a script that is executed whenever a new terminal session starts
    in interactive mode) in your favourite editor and add the following line,
    preferably at the end:

  ```sh
  alias arara='java -jar /opt/paulo/arara/arara.jar'
  ```

  Save the file and restart your terminal. It is important to observe that the
  given full path must be properly quoted if it contains spaces. There is no
  need to provide explicit parameters, as an alias simply acts as an inline
  string replacement.

- **Shell function:** A *shell function* is, as the name suggests, a subroutine,
    a code block that implements a set of operations as a means to performs a
    specified task. In order to create a shell function for our tool, open
    `.bashrc` (a script that is executed whenever a new terminal session starts
    in interactive mode) in your favourite editor and add the following line,
    preferably at the end:

  ```sh
  arara() {
      java -jar /opt/paulo/arara/arara.jar "$@"
  }
  ```

  Save the file and restart your terminal. It is important to observe that the
  given full path must be properly quoted if it contains spaces. Note that the
  `$@` symbol used in the function body represents a special shell variable that
  stores all the actual parameters in a list of strings.

  {% messagebox(title="Alias or function?") %}
  In general, an alias should effectively not do more than change the default
  options of a command, as it constitutes a mere string replacement. A function
  should be used when you need to do something more complex than an alias. In
  our particular case, as the underlying logic is pretty straightforward, both
  approaches are valid.
  {% end %}

- **Script file:** A *script* is a computer program designed to be run by an
    interpreter. In our context, the script merely sets up the environment and
    runs a system command. In order to provide a script for our tool, open your
    favourite editor and create the following file called `arara` (no
    extension):

  ```sh
  #!/bin/bash
  jarpath=/opt/paulo/arara/arara.jar
  java -jar "$jarpath" "$@"
  ```

  It is important to observe that the given full path must be properly quoted if
  it contains spaces. Note that the `$@` symbol used in the script body
  represents a special shell variable that stores all the actual parameters in a
  list of strings. This script file will act as the entry point for our
  tool. Now, we need to make it executable (i.e, set the corresponding execute
  permission) by running the following command in the terminal:

  ```sh
  $ chmod +x arara
  ```

  Now we need to move this newly executable script file to one of the
  directories set forth in the `PATH` environment variable, where executable
  commands are located. For illustrative purposes only, let us move the script
  file to the `/usr/local/bin/` directory, a location originally designed for
  programs that a normal user may run. Run the following command in the terminal
  (note the need for superuser privileges):

  ```sh
  $ sudo mv arara /usr/local/bin/
  ```

  Alternatively, the script can be placed inside a special directory named
  `bin/` from the home directory of the current user, which is usually added by
  default to the system path. Observe that, in this particular case, superuser
  privileges are not required, as the operation is kept at the current user
  level. Run the following command in the terminal instead (please note that the
  `~` symbol is a shell feature called [tilde
  expansion](http://www.gnu.org/software/bash/manual/html_node/Tilde-Expansion.html)
  and refers to the home directory of the current user):

  ```sh
  $ mv arara ~/bin/
  ```

  There is no need to restart your terminal, as the reference becomes available
  as soon as it is moved to the new location. Note that a shell script can
  provide a convenient variation of a system command where special environment
  settings, command options, or post-processing apply automatically, but in a
  way that allows the new script to still act as a fully normal Unix command.

Regardless of the adopted approach, there should be an `arara` wrapper available
as an actual Unix command in your shell session. In order to test the wrapper,
run the following command in the terminal:

```sh
$ arara
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

It is important to observe that the wrapper initiative presented in this section
might cause a potential name clash with existing TeX Live or MiKTeX binaries and
symbolic links. In this particular scenario, make sure to inspect the command
location as a means to ensure a correct execution. To this end, run the
following command in the terminal:

```sh
$ which arara
/usr/local/bin/arara
```

The `which` command shows the full path of the executable name provided as
parameter. This particular utility does this by searching for an executable or
script in the directories listed in the `PATH` environment variable. Be mindful
that aliases and shell functions are listed as well.
