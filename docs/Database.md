+++
title = "The file database"
description = "How arara remembers which files changed, and how to bend that memory"
weight = 10
+++

Compilers decide, files change, and builds repeat. Between one run and
the next, arara keeps a memory of what it saw — a small, private, very
literal file called the *file database* — so the `changed()` conditional
can answer honestly when a directive asks *has this file changed since
the last time I looked?* This chapter covers the memory itself, the
conditional that consults it, and the semantics a workflow can rely on.

# The conditional

The `changed()` helper is the memory's only client, and it lives in the
conditional context — a directive's `if` or `while` carrying it, as
taught in [Important concepts](Concepts.md):

```tex
% arara: pdflatex if changed('tex')
```

The argument is the extension shorthand: the helper looks for the file
with the same base name as the current file and the given extension —
`changed('tex')` watches `main.tex` while compiling `main.tex`. The
explicit form takes a file instead: `changed(to_file('other.tex'))`.
Its pessimistic twin `unchanged()` answers the opposite question, and
together they build the record-and-skip workflow: compile when the source
moved, stay still when it did not.

# The record-and-skip semantics

The semantics are exactly as literal as they sound:

- The first time a file is seen, there is no memory of it, so it counts
  as changed: the conditional holds, the task runs, and the file's stamp
  is recorded.

- On the next run, the recorded stamp answers. A file that did not move
  since is unchanged: `changed()` fails, `unchanged()` holds, and the
  directive is skipped — the console names the conditional that spared
  the task, and the log records the skip with its reason.

- Edit the file, and the next run sees the difference: the stamp no
  longer matches, the file changed again, the task runs, and the new
  stamp is recorded.

A typical workflow runs arara twice in a row to feel the semantics: the
first run compiles, the second skips — and the database grows one line
per remembered file. Under `--dry-run`, the memory is consulted but never
written: a dry run is a rehearsal, and rehearsals leave no record.

# The memory itself

The database is a plain file named `arara.db` by default — renamed
through the `dbname` key of [Configuration](Configuration.md), used as
given — and it lives anchored at the working directory of the run, or the
working directory of each project when projects carry their own, as
covered in [Projects](Projects.md). Two projects sharing a run keep
their memories in their own directories.

The file's inside is arara's own business: a private, machine-owned
record format, one line per file, sorted, with an empty database an
honest empty file. The tool reads and writes it atomically at the run's
edges, refuses a foreign or malformed file rather than trusting it, and
answers with its own verdict when the format is not its own. There is no
 YAML, no header, nothing a workflow should parse by hand — the
`changed()` conditional is the entire public surface, and the file is an
implementation a future version may rewrite.

{% <devlab.callout type="tip" title="When the memory goes wrong"> %}
A corrupted or stale database is safely disposable: remove the file and
the next run starts with an empty memory — every file counts as changed
once, and the workflow rebuilds its record from scratch. The same
medicine cures a database written by a different arara version.
{% </devlab.callout> %}

That is the whole database: one conditional, one file, one honest
memory per working directory — small enough to trust completely, because
there is nothing else in it to trust.
