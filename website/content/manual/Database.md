+++
title = "The file database"
description = "Remembering file changes between runs"
weight = 7
+++

arara can remember whether a file has changed since the last verification. The
`changed` and `unchanged` helpers consult a file database which persists between
runs, unlike values stored in a session. This chapter explains when those checks
update the database and where the records are kept.

# Checking for changes

A string argument refers to an extension of the current file. For example, while
processing `main.tex`, this directive checks `main.aux`:

```tex
% arara: pdflatex if changed('aux')
```

The explicit file form accepts a file reference. Use `to_file('other.tex')` with
the Lua engine, or `toFile('other.tex')` with the YAML/MVEL engine:

```tex
% arara: pdflatex if changed(to_file('other.tex'))
```

Run the latter example with `--engine lua`. The checks have the same meaning in
both engines. `unchanged` is the logical opposite of `changed`, and performs the
same verification rather than merely looking up an earlier result.

| File exists? | Recorded before? | Contents changed? | Database action | `changed` |
| --- | --- | --- | --- | --- |
| Yes | No | — | Insert checksum | `true` |
| Yes | Yes | Yes | Update checksum | `true` |
| Yes | Yes | No | None | `false` |
| No | Yes | — | Remove record | `true` |
| No | No | — | None | `false` |

Checks use the file contents, through a CRC32 checksum, rather than its last
modification time. Paths in the database are absolute and normalized.

{% messagebox(title="A check updates the record") %}
An evaluated check records the new checksum immediately. Checking the same file
again without modifying it therefore returns `false` from `changed`, even in the
same run. A later command failure does not undo the record. Short-circuit
operators can also prevent a check from being evaluated at all; the order of
checks in an expression matters.
{% end %}

In a dry run, directive conditionals are not evaluated, so checks placed there do
not read or update the database. Rule scripts and command functions still run;
calling a file-change helper from that code can still update its record.

# Database location

The default name is `arara.yaml`, relative to the project's working directory.
Change it with Lua `database` or YAML `dbname`, as described in
[Configuration file](@/manual/Configuration.md):

```yaml
!config
dbname: cache/arara.yaml
```

The corresponding Lua configuration is:

```lua
return { database = "cache/arara.yaml" }
```

{% messagebox(title="Database paths in version 8") %}
Earlier versions discarded the directory part of the configured database path.
The complete path is now honored, so an existing cache may need to be moved to
that location. The YAML format is unchanged; no conversion is required.
{% end %}

The complete configured path is used. Relative paths resolve against the
project's working directory; absolute paths are used as given. The parent
directory must exist and permit writing. Two projects using different working
directories have separate databases by default. Configuring the same absolute
path makes them consult the same records.

# Stored records and errors

The database keeps its YAML format, including the `!database` header. Existing
YAML databases remain readable. Saved entries are ordered by path so that an
unchanged set of records has deterministic output. There is usually no reason
to edit these generated records by hand.

A missing database starts empty. Other load and save errors identify the path;
malformed files are not silently replaced. Restore a valid copy or remove the
generated database to rebuild it. Existing files count as changed on their next
check; missing files do not.
