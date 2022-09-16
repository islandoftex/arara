![arara](https://i.stack.imgur.com/hjUsN.png)

# arara

![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?style=flat-square)
![Minimum JRE: 8.0](https://img.shields.io/badge/Minimum_JRE-8-blue.svg?style=flat-square)
![Current version](https://img.shields.io/badge/dynamic/json.svg?color=blue&label=Latest%20release&query=%24.0.name&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F14349047%2Frepository%2Ftags&style=flat-square)

`arara` is a TeX automation tool based on rules and directives. It gives you a
way to enhance your TeX experience. The tool is an effort to provide a concise
way to automate the daily TeX workflow for users and also package writers. Users
might write their own rules when the provided ones do not suffice.

> We are proud to announce that `arara` is part of the
> [Island of TeX](https://gitlab.com/islandoftex). The GitHub repository will
> be used as mirror but development happens on GitLab. Please open relevant
> issues and merge requests there.

## Basic use

To use `arara`, you need to tell it what to do. Unlike most other tools, you
give `arara` these _directives_ in the document itself – usually near the top.
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
rules and directives in the user manual available on our
[CTAN page](https://ctan.org/pkg/arara) or on
[arara's website](https://islandoftex.gitlab.io/arara/manual/). In addition
to documenting all of the rules that come standard with `arara`, the manual
gives a detailed explanation of how `arara` works, as well as how to create and
use your own rules.

## Getting the latest and greatest arara

`arara` is continuously built by the GitLab CI. For each and every commit, it is
ensured that a green tick means `arara` passes the test suite and is ready to
be tested. However, that is *not* meant you can use the executable artifacts of
the builds *for productive use*.

Development of `arara` takes place in the development branch. Feel free to be
one of our testers and enjoy the latest features and bug fixes by building from
there.

If you encounter any bugs or issues when using the development branch, you are
most welcome to get in touch with us and discuss potential solutions. We are
also always watching out for developers who want to join us making `arara` even
better.

## Get in touch

We use a [Gitter](https://gitter.im/Island-of-TeX/arara) chatroom for discussing
things related to `arara`. You are more than welcome to come join the fun and
say *hi!* to us. We also have the
[issues](https://gitlab.com/islandoftex/arara/issues) section in our repository
as a valid channel to report problems, bugs and suggest improvements.

If you want to stay in the loop of our other projects, feel free to visit our
[community room on Gitter](https://gitter.im/Island-of-TeX/community) or
alternatively, the Matrix rooms for
[arara](https://matrix.to/#/!HfEWIEvFtDplCLSQvz:matrix.org?via=matrix.org) or
the [community](https://matrix.to/#/!titTeSvZiqNOvRIKCv:matrix.org?via=matrix.org).

## Localization

Would you like to make `arara` speak your own language? Splendid! We would love
to have you in the team! Just contact us by opening an issue or joining our
dedicated chatroom. The localization process is quite straightforward, we can
help you! Any language is welcome!

## Downloads

[![Download from GitLab](https://img.shields.io/badge/dynamic/json.svg?color=blue&label=Latest%20release&query=%24.0.name&url=https%3A%2F%2Fgitlab.com%2Fapi%2Fv4%2Fprojects%2F14349047%2Frepository%2Ftags&style=flat-square)](https://gitlab.com/islandoftex/arara/-/releases)

Our tool is available out of the shelf on all major TeX distributions,
including TeX Live and MiKTeX, which makes manual installation unnecessary
given the significant coverage of such distributions. Chances are
you already have `arara` in your system!

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

`arara`, the cool TeX automation tool, is brought to you by the Island of TeX.
If you want to support TeX development by a donation, the best way to do this
is donating to the [TeX Users Group](https://www.tug.org/donate.html).
