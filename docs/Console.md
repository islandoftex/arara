+++
title = "The console"
description = "The plain and the styled presentations, the live feedback, and the terminals that try to lie"
weight = 8
+++

arara talks to you through a terminal, and terminals deserve better than
one-size-fits-all. Version 8 gives the tool two presentations: the *plain*
one, the historic silhouette that has carried every example in this
manual, and the *styled* one, a modern face for the terminals that can
carry it. This chapter covers both, the live feedback a styled run carries
while a task works, and — because terminals are honest only sometimes —
what arara does when the terminal lies.

# The plain presentation

The plain presentation is the default, and it is the one you have been
reading all along: the ASCII logo and the version line opening the run,
the file line with its size and modification stamp, the justified task
entries with their results, the error sections titled on dashed rules, and
the closing verdict with its tallies. It asks nothing of the terminal —
no colors, no glyphs, no escape sequences — so it looks identical
everywhere, from a modern terminal to a log captured on a build server.
If a run looks wrong nowhere, that is the presentation doing its job.

# The styled presentation

Ask for the styled presentation — with `-c styled` or the `console` key of
the configuration, as covered in [Configuration](Configuration.md) — and
the same run grows a face: tinted rules frame the output, a running glyph
marks each entry, the closing verdict carries a summary table naming each
file, its task count, its time and its status, and failures quote the tail
of their own output in a dim panel so the reason sits beside the result.
A verbose styled run shows the command it executes, frames the tool's own
output with dotted rules, and names the conditionally skipped directives.

## The live feedback

While a task runs, the styled presentation breathes: braille frames cycle
in the application's cyan accent beside a live counter of the elapsed
time, escalating from tenths of a second to the full clock as the task
runs longer, and the outcome lands on the very line the counter kept
warm. The feedback is on by default; the `animations` configuration key
silences it for those who prefer stillness; and it asks the terminal's
blessing — an undecorated stream never sees a frame.

{% <devlab.callout type="info" title="When the run is quick"> %}
The breath waits for a moment before starting, so a task that finishes in
a blink never flickers — the fast runs stay still, the slow ones come
alive, and the quick and the deliberate look equally calm.
{% </devlab.callout> %}

## The degraded form

Not every stream can carry colors and glyphs, and the styled presentation
does not insist: on a stream without the escape vocabulary, it degrades
to the same ASCII structure the plain presentation uses — the layout
holds, the summary holds, the verdict holds; only the ink is declined.
The run tells you which face it wore: the log names the presentation in
its provenance line, `plain`, `styled`, or `styled degraded by an
undecorated stream`, the same way it names the engine.

# Choosing the presentation

The choice follows the now-familiar ladder: the command line wins, the
configuration follows, and the plain default holds the floor:

```sh
$ arara -c styled hello.tex
```

```yaml
console: styled
```

An unoffered name is an error, not a silent fallback — whether it comes
from the flag or the configuration file, arara tells you the name it did
not recognize and the two it knows, so a typo cannot quietly downgrade a
styled workflow to plain.

# The dialogs

Rules can hold a conversation with the person running the tool — a
message, a choice of options, an input request, a dropdown — through the
dialog helpers of [Writing rules](Rules.md). The conversation survives
every execution mode and both presentations: the plain one renders the
classic terminal dialogs, and the styled one the type-tinted dialog
boxes, both sharing the same reading semantics, because a dialog is a
conversation with the person at the terminal, not with the terminal
itself. Under a stream that cannot answer — a build server, a redirected
pipe — the dialogs fail loudly rather than guessing: no answer is an
answer, and the tool does not invent one.

# Terminals that try to lie

On Windows, the console landscape has two color systems: the legacy
console API every host accepts, and VT sequences, which a program must
opt into before the console agrees to interpret them. arara does not flip
that bit behind your back: it probes the console honestly, so a legacy
`conhost` gets the degraded presentation *by design*, ConPTY hosts —
Windows Terminal, mintty — render the full styled presentation, and a
terminal that claims colors it cannot process, a bash inside a legacy
console inheriting a lying `TERM`, garbles every ANSI byte it receives,
arara's own help included. The escape hatch is the
`VirtualTerminalLevel` registry key, which makes conhost process VT
sequences for every program — your call, not arara's.

The JVM compounds the platform's quirks with one of its own: the
standard streams on Windows encode with the legacy codepage, so the glyph
vocabulary reached a mintty as question marks whatever the font. arara's
streams now speak UTF-8 behind a Windows terminal claim: a pipe keeps the
pure bytes, a console additionally borrows the console's output codepage
and returns it at exit — and a refused borrow changes nothing at all.

{% <devlab.callout type="warning" title="Fonts matter"> %}
The glyph vocabulary wants a modern font: Consolas lacks the check, the
cross and the braille frames, while Cascadia Mono and DejaVu Sans Mono
carry them all. If the styled presentation shows boxes where the glyphs
should be, the font is the culprit, not the terminal.
{% </devlab.callout> %}

That is the console's whole story: two presentations, an honest choice
between them, live feedback that asks before it breathes, and a tool that
probes the terminal rather than trusting it — so what you see is always
what the terminal can actually show.
