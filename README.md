![arara](https://i.stack.imgur.com/hjUsN.png)

# arara

![Version: 4.0](https://img.shields.io/badge/current_version-4.0-blue.svg?style=flat-square)
![Revision: 1](https://img.shields.io/badge/revision-1-blue.svg?style=flat-square)
![Language: Java](https://img.shields.io/badge/language-Java-blue.svg?style=flat-square)
![Minimum JRE: 5.0](https://img.shields.io/badge/minimum_JRE-5.0-blue.svg?style=flat-square)
[![License: New BSD](https://img.shields.io/badge/license-New_BSD-blue.svg?style=flat-square)](https://opensource.org/licenses/bsd-license)

`arara` is a TeX automation tool based on rules and directives. It gives you subsidies to enhance your TeX experience. The tool is an effort to provide a concise way to automate the daily TeX workflow for users and also package writers. Users might write their own rules when the provided ones do not suffice.

## Basic use

To use `arara`, you need to tell it what to do. Unlike most other tools, you give `arara` these _directives_ in the document itself – usually near the top.  So to run `pdflatex` once on your document, you should say something like:

```tex
% arara: pdflatex
\documentclass{article}
\begin{document}
Hello, world!
\end{document}
```

Now when you run `arara myfile`, that directive (`% arara: ...`) will be seen and carried out as described by the `pdflatex` rule.  You can read more about rules and directives in the [user manual](http://mirrors.ctan.org/support/arara/doc/arara-usermanual.pdf). In addition to documenting all of the rules that come standard with `arara`, its manual gives a detailed explanation of how `arara` works, how to create and use your own rules, and how to integrate the tool into the common TeX IDEs.

## Versions


![Current version: 4.0](https://img.shields.io/badge/current_version-4.0-blue.svg?style=flat-square)
![Development version: 5.0](https://img.shields.io/badge/development_version-5.0-red.svg?style=flat-square)

The stable major version of `arara` is the 4.0 series (note that revision numbers may vary). Please refer to the development branch for more information on the upcoming 5.0 series release. The master branch always refers to the stable version (including potential revisions).

For historical purposes, the source code for older versions of `arara` is available in the [releases](https://github.com/cereda/arara/releases) section of our repository. However, be mindful that such versions are unsupported.

## Build status

![Version: 4.0](https://img.shields.io/badge/version-4.0-blue.svg?style=flat-square)
![Travis CI for 4.0](https://img.shields.io/travis/cereda/arara.svg?style=flat-square)
![Version: 5.0](https://img.shields.io/badge/version-5.0-red.svg?style=flat-square)
![Travis CI for 5.0](https://img.shields.io/travis/cereda/arara/develoopment.svg?style=flat-square)

`arara` uses [Travis CI](https://travis-ci.org) as a hosted continuous integration service. For each and every commit, we can see in real time the build status of our application checked against a range of Java VM vendors. It is worth noting that the current series is designed and built to be Java 5.0 compliant, so if you have an old JVM, it is almost sure that you will be able to run `arara` in it without any problems.

## Support

[![Gitter chatroom](https://img.shields.io/badge/gitter-join_chat-blue.svg?style=flat-square)](https://opensource.org/licenses/bsd-license)
[![GitHub issues](https://img.shields.io/github/issues-raw/cereda/arara.svg?style=flat-square)](https://gitter.im/cereda/arara)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/cereda/arara.svg?style=flat-square)

We use a [Gitter](https://gitter.im/cereda/arara) chatroom for discussing things related to `arara`. You are more than welcome to come join the fun and say *hi!* to us. We also have the [issues](https://github.com/cereda/arara/issues) section in our repository as a valid channel to report problems, bugs and suggest improvements. 

## Localization

Would you like to make `arara` speak your own language? Splendid! We would love to have you in the team! Just send us an e-mail, join our dedicated chatroom or open an issue about it. The localization process is quite straightforward, we can help you! `:)` Any language is welcome!

## Downloads

Binary releases are powered by Bintray and not available in the repository anymore. You can [click here](https://bintray.com/cereda/arara/installers/_latestVersion) or use the button below to get access to the current 3.0 release. Note that you need Java to run both the installer and `arara` itself.

*Download:* [ ![Download](https://api.bintray.com/packages/cereda/arara/installers/images/download.svg?version=3.0) ](https://bintray.com/cereda/arara/installers/3.0/link)

If you want to try out the development version, you need to build it from source. It is actually a straightforward process: just clone this repository, go to the `application/` directory and run `mvn assembly:assembly` (you need [Apache Maven](http://maven.apache.org/) for this); you'll get the resulting `.jar` file in a `target/` directory. Have fun!

## License

This application is licensed under the [New BSD License](http://www.opensource.org/licenses/bsd-license.php).Please note that the New BSD License has been verified as a GPL-compatible free software license by the [Free Software Foundation](http://www.fsf.org/), and has been vetted as an open source license by the [Open Source Initiative](http://www.opensource.org/).

### The core team

- ![Brazil](http://i.imgur.com/If4PQTk.png) Paulo Roberto Massa Cereda
- ![Germany](http://i.imgur.com/GovD283.png) Marco Daniel
- ![United Kingdom](http://i.imgur.com/Lvp73Wo.png) Brent Longborough
- ![United Kingdom](http://i.imgur.com/Lvp73Wo.png) Nicola Louise Cecilia Talbot

![Quack](http://i.imgur.com/hKsnp9f.png)

*No ducks were hurt in the making of this tool*
