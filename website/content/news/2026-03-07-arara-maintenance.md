+++
title = "arara maintenance"
description = "arara maintenance"
+++

It's been a while since we published anything regarding arara development. We
are still here and listening to user feedback. arara's current maintenance mode
does not require regular updates but here's what we've been up to and a few
notes to questions we have been asked repeatedly over the last few years.

<!-- more -->

# Maintenance Mode

The last major arara release has been in 2022. For a species of parrots, this
bird has gone into a very quiet daily routine. However, let us ensure you that
the project is not dead, irregular commits do not mean no committment to
maintain arara, or anything like that. We believe in the tool, its ongoing
relevance, and the community around it. Our sincerest thanks to all of our
users.

When we released arara v7, we already knew that its core feature set has been
completed for a while. We were experimenting with new concepts like Lua-based
project definitions and adding to the rule set wherever there was demand. But
nothing revolutionary was expected in terms of feature development or bug
squashing. The only real user feedback we received was not on missing features
or functional bugs but primarily when a new Java version, Windows version, or
macOS version broke the currently released arara.

On the one hand, that is an extremely gratifying state to be in as an open
source project: knowing that your user base is content with the features of your
tool and relying on it in a way that breakages actually matter enough to get
reported instead of the tool being thrown out from your workflow. On the other
hand, it also means that all the fancy new features we envisioned for arara v8
did not really have any demand.

Knowing that, we have deprioritized moving our rules to Lua, enhancing the
Lua-based projects introduced in v7, and making arara a more powerful build tool
because in the end, it would be a technically unnecessary breakage for users who
value the current state. The one priority we still have is making a native
executable out of arara, direction still unclear (Kotlin Multiplatform, GraalVM,
…). If that requires us to make the changes towards Lua we envisioned for v8,
they may come as part of that transition, but probably not on their own.

That said, even this one remaining priority is something on which we currently
do not have the capacity to work on. For both Paulo and Ben, real life
contributed a few “social elements” to their spare time slots so we enjoy
working on other projects and aspects related to the Island of TeX where the
effort-impact ratio or even the development fun is more prominent than in making
changes to arara that are basically unnecessary for now.

However, we are still listening to feedback. If you are lacking a new rule or
want to contribute a new rule upstream, go for it, we definitely appreciate it.
If you enjoy the Lua-based projects feature and have ideas on how to improve it?
Talk to us, it may motivate us to work on it again. Or if you are interested in
even more intricate features or addressing things we have planned but not done
yet, we are still always looking for contributors and would be happy to welcome
new faces to the island's maintainer pool (no binding or large commitment
required).

# macOS and notarization

Every year, we will receive e-mails on the TeX Live mailing list about arara
being marked as “forcibly removed” on new MacTeX installations. If you are on
macOS, do not worry, you are still able to install arara. After having finished
your MacTeX installation, open TeX Live utility and install `arara` again, then
it will work just fine.

Now you may ask why this is necessary at all. We would love to pretend we didn't
know. However, we do, it's Apple's requirement to notarize binary distributions
that are distributed as installers for macOS. That includes signing a package
with an Apple Developer ID and letting Apple scrutinize an installer's content.
Here's the catch, though: none of arara's core developers use Apple hard- and
software for development, none of arara's core developers have an Apple
Developer ID (which costs money), and as explained above, due to the maintenance
mode of arara, none of arara's core developers plans to have a yearly expense
for a platform they do not use otherwise.

So we are stuck in a situation where none of us wants to be in, sacrificing user
experience on macOS due to development concerns. However, it's also one of the
scenarios where we are caught in the middle between parties that are actually
concerned: the package that actually wants to get notarized is MacTeX which
could include arara. arara itself wouldn't be that much of a problem if all it
used were Java/JVM dependencies. But arara relies on the JNA project as a
dependency which pulls natively compiled code into our distribution. In turn,
that is the reason, arara is missing from MacTeX.

The JNA developers
[rightly](https://github.com/java-native-access/jna/issues/1306#issuecomment-772696095)
[defer](https://github.com/java-native-access/jna/issues/1592#issuecomment-1917556290)
signing responsibilities to the applications bundling JNA. And MacTeX also
rightly defers this responsibility to the projects that intend to get bundled
with MacTeX. So we totally accept the blame and are hereby informing you that we
won't act on it. However, if you happen to be a user enjoying arara and willing
to sign arara with your Apple Developer ID once a year for inclusion into
MacTeX, make sure to contact us, and we will definitely take another stab at
MacTeX inclusion of arara.
