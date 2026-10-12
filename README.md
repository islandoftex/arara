[![pipeline status](https://gitlab.com/islandoftex/arara/badges/main/pipeline.svg?style=flat-square)](https://gitlab.com/islandoftex/arara/-/pipelines)
[![Latest release](https://img.shields.io/badge/dynamic/json.svg?color=blue&label=Latest%20release&query=%24.0.name&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F14349047%2Frepository%2Ftags&style=flat-square)](https://gitlab.com/islandoftex/arara/-/releases)
![Requires Java 11+](https://img.shields.io/badge/Requires-Java_11%2B-blue.svg?style=flat-square)

# arara

arara is a TeX automation tool based on rules and directives. It gives you a
way to enhance your TeX experience. The tool is an effort to provide a concise
way to automate the daily TeX workflow for users and also package writers. Users
might write their own rules when the provided ones do not suffice.

## Basic use

To use arara, you need to tell it what to do. Unlike most other tools, you
give arara these _directives_ in the document itself – usually near the top.
So to run `pdflatex` once on your document, you should say something like:

```tex
% arara: pdflatex
\documentclass{article}
\begin{document}
Hello, world!
\end{document}
```

Now when you run `arara myfile`, that directive (`% arara: ...`) will be seen
and carried out as described by the `pdflatex` rule.  You can read more about
rules and directives in the user manual — or start with its short quick-start
companion — available on our [CTAN page](https://ctan.org/pkg/arara) or on
[arara's website](https://islandoftex.gitlab.io/arara/). In addition
to documenting all of the rules that come standard with arara, the manual
gives a detailed explanation of how arara works, as well as how to create and
use your own rules.

## Getting the latest and greatest arara

Development happens on `main`. Every pipeline run proves the tool — a green tick
means the test suite passed and the build is ready. The pipeline publishes the
runnable jar with its checksums as artifacts for testers, and the package
registry carries `-SNAPSHOT` builds of the library modules for anyone embedding
the API.

If you encounter any bugs or issues, you are most welcome to get in touch with
us and discuss potential solutions. We are also always watching out for
developers who want to join us making arara even better.

## Get in touch

We use a [Matrix](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org)
chat room for discussing things related to arara. You are more than welcome to come
join the fun and say *hi!* to us. We also have the
[issues](https://gitlab.com/islandoftex/arara/issues) section in our repository
as a valid channel to report problems, bugs and suggest improvements.

If you want to stay in the loop of our other projects, feel free to visit our
[community room on Matrix](https://matrix.to/#/!titTeSvZiqNOvRIKCv:matrix.org?via=matrix.org).

## Localization

Would you like to make arara speak your own language? Splendid! We would love
to have you in the team! Just contact us by opening an issue or joining our
dedicated chatroom. The localization process is quite straightforward, we can
help you! Any language is welcome!

## Downloads

Our tool is available out of the shelf on all major TeX distributions,
including TeX Live and MiKTeX, which makes manual installation unnecessary
given the significant coverage of such distributions. Chances are
you already have arara in your system!

Note that arara rides on a Java runtime — version 11 or newer — which the TeX
distributions do not ship, so make sure your system has one.

You can obtain the official package available in the
[releases](https://gitlab.com/islandoftex/arara/-/releases) section of our
project repository. Please refer to the documentation on how to
manually deploy our tool.

## License

This application is licensed under the
[New BSD License](https://opensource.org/licenses/BSD-3-Clause). Please
note that the New BSD License has been verified as a GPL-compatible free
software license by the [Free Software Foundation](http://www.fsf.org/), and
has been vetted as an open source license by the
[Open Source Initiative](http://www.opensource.org/).

## The team

arara, the cool TeX automation tool, is brought to you by the Island of TeX.
If you want to support TeX development by a donation, the best way to do this
is donating to the [TeX Users Group](https://www.tug.org/donate.html).
