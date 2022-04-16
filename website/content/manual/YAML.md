+++
title = "YAML"
description = "Introduction to the YAML format"
weight = 10
+++

According to the [specification](http://yaml.org/spec/1.2/spec.html), YAML (a
recursive acronym for *YAML Ain't Markup Language*) is a human-friendly, cross
language, Unicode-based data serialization language designed around the common
native data type of programming languages. arara uses this format in three
circumstances:

1. *Parametrized directives*, as the set of attribute/value pairs (namely,
   argument name and corresponding value) is represented by a map. This
   particular type of directive is formally introduced in [Concepts](/manual/concepts).

2. *Rules*, as their entire structure is represented by a set of specific keys
   and their corresponding values (a proper YAML document). A rule follows a
   very strict model, detailed in [Concepts](/manual/concepts).

3. *Configuration files*, as the general settings are represented by a set of
   specific keys and their corresponding values (a proper YAML
   document). Configuration files are covered in
   [Configuration](/manual/configuration).

This chapter only covers the relevant parts of the YAML format for a consistent
use with arara. For advanced topics, I highly recommend the complete format
specification, available online.

# Collections

According to the specification, YAML's block collections use indentation for
scope and begin each entry on its own line. Block sequences indicate each entry
with a dash and space. Mappings use a colon and space to mark each *key: value*
pair. Comments begin with an octothorpe `#`. arara relies solely on mappings and
a few scalars to sequences at some point. Let us see an example of a sequence of
scalars:

```yaml
team:
- Paulo Cereda
- Marco Daniel
- Brent Longborough
- Nicola Talbot
- Ben Frank
```

It is quite straightforward: `team` holds a sequence of four scalars. YAML also
has flow styles, using explicit indicators rather than indentation to denote
scope. The flow sequence is written as a comma-separated list within square
brackets:

```yaml
primes: [ 2, 3, 5, 7, 11 ]
```

Attribute maps are easily represented by nesting entries, respecting
indentation. For instance, consider a map `developer` containing two keys,
`name` and `country`. The YAML representation is presented as follows:

```yaml
developer:
name: Paulo
country: Brazil
```

Similarly, the flow mapping uses curly braces. Observe that this is the form
adopted by a parametrized directive (see syntax in [Concepts](/manual/concepts)):

```yaml
developer: { name: Paulo, country: Brazil }
```

An attribute map can contain sequences as well. Consider the following code
where `developers` holds a list of two developers containing their names
and countries:

```yaml
developers:
- name: Paulo
  country: Brazil
- name: Marco
  country: Germany
```

The previous code can be easily represented in flow style by using square and
curly brackets to represent sequences and attribute maps.

# Scalars

Scalar content can be written in block notation, using a literal style,
indicated by a vertical bar, where *all line breaks are
significant*. Alternatively, they can be written with the folded style, denoted
by a greater-than sign, where *each line break is folded to a space* unless it
ends an empty or a more-indented line. It is mportant to note that arara
intensively uses both styles (as seen in Section~\ref{sec:rule}, on
page~\pageref{sec:rule}). Let us see an example:

```yaml
logo: |
  This is the arara logo
  in its ASCII glory!
    __ _ _ __ __ _ _ __ __ _
   / _` | '__/ _` | '__/ _` |
  | (_| | | | (_| | | | (_| |
   \__,_|_|  \__,_|_|  \__,_|
slogan: >
  The cool TeX
  automation tool
```

As seen in the previous code, `logo` holds the ASCII logo of our tool,
respecting line breaks. Similarly, observe that the `slogan` key holds the text
with line breaks replaced by spaces (in the same fashion TeX does with
consecutive, non-empty lines).

{% messagebox(title="Block indentation indicator") %}
According to the YAML specification, the indentation level of a block scalar is
typically detected from its first non-empty line. It is an error for any of the
leading empty lines to contain more spaces than the first non-empty line, hence
the ASCII logo could not be represented, as it starts with a space.

When detection would fail, YAML requires that the indentation level for the
content be given using an explicit indentation indicator. This level is
specified as the integer number of the additional indentation spaces used for
the content, relative to its parent node. It would be the case if we want to
represent our logo without the preceding text.
{% end %}

YAML's flow scalars include the plain style and two quoted styles. The
double-quoted style provides escape sequences. The single-quoted style is useful
when escaping is not needed. All flow scalars can span multiple lines. Note that
line breaks are always folded. Since arara uses MVEL as its underlying scripting
language (see [MVEL](/manual/mvel) for reference), it might be
advisable to quote scalars when starting with forbidden symbols in YAML.

# Tags

According to the specification, in YAML, untagged nodes are given a type
depending on the application. The examples covered in this primer use the `seq`,
`map` and `str` types from the fail safe schema. Explicit typing is denoted with
a tag using the exclamation point symbol. Global tags are usually uniform
resource identifiers and may be specified in a tag shorthand notation using a
handle. Application-specific local tags may also be used. For arara, there is a
special schema used for both rules and configuration files, so in those cases,
make sure to add `!config` as global tag:

```yaml
!config
```

In particular, rules and configuration files of arara are properly covered in
[Concepts](/manual/concepts) and [Configuration](/manual/configuration). For
now, it suffices to say that the `!config` global tag is necessary to provide
the correct mapping of values inside our tool.

# Further reading

This chapter does not cover all features of the YAML format, so further reading
is advisable. I highly recommend the [official YAML
specification](http://yaml.org/spec/1.2/spec.html), currently covering the third
version of the format.
