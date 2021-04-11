+++
title = "Introduction"
description = "Introduction"
weight = 1
+++

Hello there, welcome to arara, the cool TeX automation tool! This chapter is
actually a quick introduction to what you can (and cannot) expect from
arara. For now, concepts will be informally presented and will be detailed later
on, in the next chapters.

# What is this tool?

Good question! arara is a TeX automation tool based on rules and directives. It
is, in some aspects, similar to other well-known tools like `latexmk` and
`rubber`. The key difference (and probably the selling point) might be the fact
that arara aims at explicit instructions in the source code (in the form of
comments) in order to determine what to do instead of relying on other
resources, such as log file analysis. It is a different approach for an
automation tool, and we have both advantages and disadvantages of such
design. Let us use the following file `hello.tex` as an example:

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
$ latexmk -pdf mydoc.tex
$ rubber --pdf mydoc.tex
```

The mentioned tools perform an analysis on the file and decide what has to be
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

Processing "hello.tex" (size: 70 B, last modified: 12/28/2020
07:03:16), please wait.

  ERROR

It looks like no directives were found in the provided file. Make
sure to include at least one directive and try again.

Total: 0.04 seconds
```

Quite surprising. However, this behaviour is not wrong at all, it is completely
by design: arara needs to know what you want. And for that purpose, you need to
tell the tool what to do.

{% messagebox(title="A very important concept") %}
That is the major difference of arara when compared to other tools: *it is not
an automatic process and the tool does not employ any guesswork on its own*. You
are in control of your documents; arara will not do anything unless you *teach
it how to do a task and explicitly tell it to execute the task*.
{% end %}

Now, how does one tell arara to do a task? That is actually the easy part,
provided that you have everything up and running. We accomplish the task by
adding a special comment line, hereafter known as *directive*, somewhere in our
*hello.tex* file (preferably in the first lines):

```tex
% arara: pdflatex
\documentclass{article}

\begin{document}
Hello world!
\end{document}
```

For now, do not worry too much about the terms, we will come back to them later
on, in Chapter~\ref{chap:importantconcepts}, on
page~\pageref{chap:importantconcepts}. It suffices to say that arara expects
*you* to provide a list of tasks, and this is done by inserting special comments
in the source file. Let us see how arara behaves with this updated code:

```sh
$ arara hello.tex
  __ _ _ __ __ _ _ __ __ _
 / _` | '__/ _` | '__/ _` |
| (_| | | | (_| | | | (_| |
 \__,_|_|  \__,_|_|  \__,_|

Processing "hello.tex" (size: 88 B, last modified: 12/28/2020
07:05:05), please wait.

(PDFLaTeX) PDFLaTeX engine .............................. SUCCESS

Total: 0.56 seconds
```

Hurrah, we finally got our document properly compiled with a TeX engine by the inner workings of our beloved tool, resulting in an expected `hello.pdf` file created using the very same system call that typical automation tools like `latexmk` and `rubber` use. Observe that arara works practically on other side of the spectrum: you need to tell it how and when to do a task.

# Core concepts

When adding a directive in our source code, we are explicitly telling the tool
what we want it to do, but I am afraid that is not sufficient at all. So far,
arara knows *what* to do, but now it needs to know *how* the task should be
done. If we want arara to run `pdflatex` on `hello.tex`, we need to have
instructions telling our tool how to run that specific application. This
particular sequence of instructions is referred as a `rule` in our
context.

{% messagebox(title="Note on rules") %}
Although the core team provides a lot of rules shipped with arara out of the
box, with the possibility of extending the set by adding more rules, some users
might find this decision rather annoying, since other tools have most of their
rules hard-coded, making the automation process even more transparent. However,
since arara does not rely on a specific automation or compilation scheme, it
becomes more extensible. The use of directives in the source code make the
automation steps more fluent, which allows the specification of complex
workflows much easier.
{% end %}

Despite the inherited verbosity of automation steps not being suitable for small
documents, arara really shines when you have a document which needs full control
of the automation process (for instance, a thesis or a manual). Complex
workflows are easily tackled by our tool.

Rules and directives are the core concepts of arara: the first dictates how a
task is done, and the latter is the proper instance of the associated rule on
the current document, i.e, when and where the commands must be executed.

{% messagebox(title="The name") %}
TODO: {\centering\includegraphics[width=0.9\textwidth]{figures/arara.png}\par}

*Do you like araras? We do, specially our tool which shares the same name of this colorful bird.*

The tool name was chosen as an homage to a Brazilian bird of the same name,
which is a macaw. The word *arara* comes from the Tupian word *a'rara*, which
means *big bird* (much to my chagrin, Sesame Street's iconic character Big Bird
is not a macaw; according to some sources, he claims to be a golden
condor). Araras are colorful, noisy, naughty and very funny. Everybody loves
araras. The name seemed catchy for a tool and, in the blink of an eye, arara was
quickly spread to the whole TeX world.
{% end %}

Now that we informally introduced rules and directives, let us take a look on
how arara actually works given those two elements. The whole idea is pretty
straightforward, and I promise to revisit these concepts later on in this manual
for a comprehensive explanation (more precisely, in
Chapter~\ref{chap:importantconcepts}).

First and foremost, we need to add at least one instruction in the source code
to tell arara what to do. This instruction is named a *directive* and it will be
parsed during the preparation phase. Observe that \arara\ will tell you if no
directive was found in a file, as seen in our first interaction with the tool.

An arara directive is usually defined in a line of its own, started with a comment (denoted by a percent sign in TeX and friends), followed by the word `arara:` and task name:

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
`pdflatex` command line application. Rules are YAML files named according to
their identifiers followed by the `yaml` extension and follow a strict
structure. This concept is covered in Section~\ref{sec:rule}, on
page~\pageref{sec:rule}.

Now, we have a queue of pairs $(\textit{directive}, \textit{rule})$ to
process. For each pair, arara will map the directive to its corresponding rule,
evaluate it and run the proper command. The execution chain requires that
command $i$ was successfully executed to then proceed to command $i+1$, and so
forth. This is also by design: arara will halt the execution if any of the
commands in the queue had raised an error. How does one know if a command was
successfully executed? arara checks the corresponding *exit status* available
after a command execution. In general, a successful execution yields 0 as its
exit status.

In order to decide whether a command execution is successful, arara relies on
exit status checking. Typically, a command is successful if, and only if, its
resulting exit status is 0 and no other value. However, we can define any value,
or even forget about it and make it always return a valid status regardless of
execution (for instance, in a rule that always is successful -- see, for
instance, the `clean` rule).

That is pretty much how arara works: directives in the source code are mapped to
rules. These pairs are added to a queue. The queue is then executed and the
status is reported. More details about the expansion process are presented in
Chapter~\ref{chap:importantconcepts}, on
page~\pageref{chap:importantconcepts}. In short, we teach arara to do a task by
providing a rule, and tell it to execute it through directives in the source
code.

# Operating system remarks

The application is written using the Kotlin language, so arara runs on top of a
Java virtual machine, available on all the major operating systems~--~in some
cases, you might need to install the proper virtual machine. We tried very hard
to keep both code and libraries compatible with older virtual machines or from
other vendors. Currently, arara is known to run on Java 8 to 15, from any
vendor.

{% messagebox(title="Outdated Java virtual machines") %}
Dear reader, beware of outdated software, mainly Java virtual machines! Although
arara offers support for older virtual machines, try your best to keep your
software updated as frequently as possible. The legacy support exists only for
historical reasons, and also due to the sheer fact that we know some people that
still runs arara on very old hardware. If you are not in this particular
scenario, get the latest virtual machine.
{% end %}

In Chapter~\ref{chap:buildingfromsource}, on
page~\pageref{chap:buildingfromsource}, we provide instructions on how to build
arara from sources using Gradle. Even if you use multiple operating systems,
arara should behave the same, including the rules. There are helper functions
available in order to provide support for system-specific rules based on the
underlying operating system.

# Support

If you run into any issue with arara, please let us know. We all have very
active profiles in the [TeX community at
StackExchange](https://tex.stackexchange.com/), so just use the `arara` tag in
your question and we will help you the best we can (also, take a look at their
[starter guide](https://tex.meta.stackexchange.com/q/1436)).

We also have a
[Matrix](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org) and
[Gitter](https://gitter.im/Island-of-TeX/arara) chat rooms, in which we
occasionally hang out. Also, if you think the report is worthy of an issue, open
one in our [GitLab repository](https://gitlab.com/islandoftex/arara/issues).

We really hope you like our humble contribution to the TeX community. Let arara
enhance your TeX experience, it will help you when you will need it the
most. Enjoy the manual.
