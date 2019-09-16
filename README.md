![arara](https://i.stack.imgur.com/hjUsN.png)

# arara

![Version: 4.0](https://img.shields.io/badge/current_version-4.0-blue.svg?style=flat-square)
![Revision: 2](https://img.shields.io/badge/revision-2-blue.svg?style=flat-square)
![Language: Java](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)
![Minimum JRE: 5.0](https://img.shields.io/badge/minimum_JRE-5.0-blue.svg?style=flat-square)
[![License: New BSD](https://img.shields.io/badge/license-New_BSD-blue.svg?style=flat-square)](https://opensource.org/licenses/bsd-license)
![CTAN packaging: 4.0.6](https://img.shields.io/badge/CTAN_packaging-4.0.6-blue.svg?style=flat-square)

`arara` is a TeX automation tool based on rules and directives. It gives you a way to enhance your TeX experience. The tool is an effort to provide a concise way to automate the daily TeX workflow for users and also package writers. Users might write their own rules when the provided ones do not suffice.

## Basic use

To use `arara`, you need to tell it what to do. Unlike most other tools, you give `arara` these _directives_ in the document itself – usually near the top.  So to run `pdflatex` once on your document, you should say something like:

```tex
% arara: pdflatex
\documentclass{article}
\begin{document}
Hello, world!
\end{document}
```

Now when you run `arara myfile`, that directive (`% arara: ...`) will be seen and carried out as described by the `pdflatex` rule.  You can read more about rules and directives in the user manual available in our [releases](https://github.com/cereda/arara/releases) section. In addition to documenting all of the rules that come standard with `arara`, the manual gives a detailed explanation of how `arara` works, as well as how to create and use your own rules.

## Versions

![Current version: 4.0](https://img.shields.io/badge/current_version-4.0-blue.svg?style=flat-square)
![Development version: 5.0](https://img.shields.io/badge/development_version-5.0-red.svg?style=flat-square)

The stable major version of `arara` is the 4.0 series (note that revision numbers may vary). Please refer to the development branch for more information on the upcoming 5.0 series release. The master branch always refers to the stable version (including potential revisions). The CTAN packaging for the current version is 4.0.6 (for internal use).

For historical purposes, the source code for older versions of `arara` is available in the [releases](https://github.com/cereda/arara/releases) section of our repository. However, be mindful that these versions are unsupported.

## Build status

[![Version: 4.0](https://img.shields.io/badge/version-4.0-blue.svg?style=flat-square)](https://travis-ci.org/cereda/arara/)
![Travis CI for 4.0](https://img.shields.io/travis/cereda/arara.svg?style=flat-square)
[![Version: 5.0](https://img.shields.io/badge/version-5.0-red.svg?style=flat-square)](https://travis-ci.org/cereda/arara/branches)
![Travis CI for 5.0](https://img.shields.io/travis/cereda/arara/development.svg?style=flat-square)

`arara` uses [Travis CI](https://travis-ci.org) as a hosted continuous integration service. For each and every commit, we can see in real time the build status of our application checked against a range of Java VM vendors. It is worth noting that the current series is designed and built to be Java 5.0 compliant, so if you have an old JVM, it is almost sure that you will be able to run `arara` in it without any problems.

## Support

[![Gitter chatroom](https://img.shields.io/badge/gitter-join_chat-blue.svg?style=flat-square)](https://gitter.im/cereda/arara)
[![GitHub issues](https://img.shields.io/badge/github-issues-blue.svg?style=flat-square)](https://github.com/cereda/arara/issues)

We use a [Gitter](https://gitter.im/cereda/arara) chatroom for discussing things related to `arara`. You are more than welcome to come join the fun and say *hi!* to us. We also have the [issues](https://github.com/cereda/arara/issues) section in our repository as a valid channel to report problems, bugs and suggest improvements. 

## Localization

Would you like to make `arara` speak your own language? Splendid! We would love to have you in the team! Just send us an e-mail, join our dedicated chatroom or open an issue about it. The localization process is quite straightforward, we can help you! Any language is welcome!

A big thanks to our translators Marco Daniel, Clemens Niederberger, Ulrike Fischer, Gert Fischer, Enrico Gregorio and Marijn Schraagen for the awesome localization work!

## Downloads

[![Download from GitHub](https://img.shields.io/badge/github-4.0-blue.svg?style=flat-square)](https://github.com/cereda/arara/releases)
[![Download from Bintray](https://img.shields.io/badge/bintray-4.0-blue.svg?style=flat-square)](https://bintray.com/cereda/arara)

From the 4.0 series on, the team decided to not release cross-platform installers any more. Our tool is available out of the shelf on all major TeX distributions, including TeX Live and MiKTeX, which makes manual installation unnecessary given the significant coverage of such distributions. Chances are you already have `arara` in your system!

You can obtain the official package available in the [releases](https://github.com/cereda/arara/releases) section of our project repository, as well as the [Bintray](https://bintray.com/cereda/arara) software distribution service. Please refer to the documentation on how to manually deploy our tool.

## License

This application is licensed under the [New BSD License](http://www.opensource.org/licenses/bsd-license.php). Please note that the New BSD License has been verified as a GPL-compatible free software license by the [Free Software Foundation](http://www.fsf.org/), and has been vetted as an open source license by the [Open Source Initiative](http://www.opensource.org/).

## The team

`arara`, the cool TeX automation tool, is brought to you by Paulo Cereda, Marco Daniel, Brent Longborough and Nicola Talbot. If you want to support TeX development by a donation, the best way to do this is donating to the [TeX Users Group](https://www.tug.org/donate.html).
