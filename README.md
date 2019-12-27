![arara](https://i.stack.imgur.com/hjUsN.png)

# arara

![Version](https://img.shields.io/badge/Current_version-5.0.0-blue.svg?style=flat-square)
![Language: Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?style=flat-square)
![Minimum JRE: 8.0](https://img.shields.io/badge/Linimum_JRE-8-blue.svg?style=flat-square)
![License: New BSD](https://img.shields.io/badge/License-New_BSD-blue.svg?style=flat-square)
![CTAN packaging](https://img.shields.io/badge/CTAN_packaging-5.0.0-blue.svg?style=flat-square)

`arara` is a TeX automation tool based on rules and directives. It gives you a
way to enhance your TeX experience. The tool is an effort to provide a concise
way to automate the daily TeX workflow for users and also package writers. Users
might write their own rules when the provided ones do not suffice.

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
rules and directives in the user manual available in our
[releases](https://gitlab.com/islandoftex/arara/-/releases) section. In addition
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
 
## Support

[![Gitter chatroom](https://img.shields.io/badge/Gitter-join_chat-blue.svg?style=flat-square)](https://gitter.im/cereda/arara)
[![GitLab issues](https://img.shields.io/badge/GitLab-issues-blue.svg?style=flat-square)](https://gitlab.com/islandoftex/arara/issues)

We use a [Gitter](https://gitter.im/cereda/arara) chatroom for discussing things
related to `arara`. You are more than welcome to come join the fun and say *hi!*
to us. We also have the [issues](https://gitlab.com/islandoftex/arara/issues)
section in our repository as a valid channel to report problems, bugs and
suggest improvements. 

## Localization

Would you like to make `arara` speak your own language? Splendid! We would love
to have you in the team! Just send us an e-mail, join our dedicated chatroom or
open an issue about it. The localization process is quite straightforward, we
can help you! Any language is welcome!

A big thanks to our translators Marco Daniel, Clemens Niederberger, Ulrike
Fischer, Gert Fischer, Enrico Gregorio and Marijn Schraagen for the awesome
localization work!

## Downloads

[![Download from GitLab](https://img.shields.io/badge/GitLab-5.0.0-blue.svg?style=flat-square)](https://gitlab.com/islandoftex/arara/-/releases)
[![Download from Bintray](https://img.shields.io/badge/Bintray-5.0.0-blue.svg?style=flat-square)](https://bintray.com/cereda/arara)

From the 4.0 series on, the team decided to not release cross-platform
installers any more. Our tool is available out of the shelf on all major TeX
distributions, including TeX Live and MiKTeX, which makes manual installation
unnecessary given the significant coverage of such distributions. Chances are
you already have `arara` in your system!

You can obtain the official package available in the
[releases](https://gitlab.com/islandoftex/arara/-/releases) section of our
project repository, as well as the [Bintray](https://bintray.com/cereda/arara)
software distribution service. Please refer to the documentation on how to
manually deploy our tool.

## License

This application is licensed under the 
[New BSD License](http://www.opensource.org/licenses/bsd-license.php). Please
note that the New BSD License has been verified as a GPL-compatible free
software license by the [Free Software Foundation](http://www.fsf.org/), and
has been vetted as an open source license by the
[Open Source Initiative](http://www.opensource.org/).

## The team

`arara`, the cool TeX automation tool, is brought to you by Paulo Cereda, Marco
Daniel, Brent Longborough and Nicola Talbot. If you want to support TeX
development by a donation, the best way to do this is donating to the
[TeX Users Group](https://www.tug.org/donate.html).
