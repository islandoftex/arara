#!/usr/bin/env python3
"""
assemble-website.py — lift the manual into the website worktree.

The arara documentation lives once, in the docs/ directory of this
repository, and two pipelines read it there:

  * the LaTeX build (docs/tex, through buildSrc) converts the
    chapters into the typeset PDF book, and

  * this script copies the very same chapters into the website
    worktree, applying the small set of transforms the web needs.

The chapters already carry their Zola front matter — titles,
descriptions and reading weights, in the canonical order of
docs/tex/arara-manual.tex — so the lift is nearly verbatim. The
generated files are never committed: the website branch carries the
scaffolding, this script supplies the book. Run it whenever docs/
changes and the site follows the manual.

Run the script against any checkout of the site:

    python3 scripts/assemble-website.py --site <path-to-the-site>

In CI, the pages job runs the same script against the fetched
checkout, so the local site and the deployed site are always built
from identical hands.
"""

import argparse
import re
import shutil
from pathlib import Path

# The canonical reading order, from docs/tex/arara-manual.tex: the
# same \input sequence the PDF book follows, so the web's table of
# contents tells the same story as the printed one.
CHAPTERS = [
    "Introduction", "Concepts", "CommandLine", "Rules", "Configuration",
    "Engines", "Projects", "Console", "Logging", "Database",
    "Building", "Migration", "Library", "Pack",
]

# The manual section index: generated, not committed. Zola reads the
# sort_by setting and orders the chapter pages by the weights they
# already carry in their own front matter.
MANUAL_INDEX = (
    "+++\n"
    'title = "The arara manual"\n'
    'description = "The whole tool, from concepts to rule authorship — '
    'fourteen chapters, the same book the PDF tells."\n'
    'sort_by = "weight"\n'
    "+++\n"
)

# The changelog carries no front matter — it is a plain document in
# the repository root — so the site page gains its title here, and
# its leading H1 heading is stripped during assembly to avoid
# duplicating the page title.
CHANGELOG_FRONT = (
    "+++\n"
    'title = "Changelog"\n'
    'description = "Every notable change to arara, Keep a Changelog style."\n'
    "+++\n\n"
)

# Zola 0.23 runs every page through Tera before parsing the Markdown,
# so a literal double-curly-brace run would be read as a template
# expression. arara's own directive syntax interpolates through
# exactly those delimiters, and the chapters and the changelog
# document them — so the site would refuse to build. This pattern
# matches one such run (no nested braces), in prose or in code
# examples, letting the transform below neutralize it.
DOUBLE_CURLY = re.compile(r"\{\{[^{}]*\}\}")


def tera_raw_wrap(text: str) -> str:
    """Neutralize literal double-curly-brace runs for the site.

    Each `{{ … }}` occurrence — arara's interpolation syntax — is
    wrapped in a Tera raw block. Tera honors the wrapper and leaves
    the literal text alone; the raw markers themselves never reach
    the Markdown output, so a code fence showing
    `{{ session_path() }}` renders exactly as written. Only the
    assembled copies see the treatment: the PDF pipeline and the
    repository's browsable Markdown keep the pristine delimiters.
    """
    return DOUBLE_CURLY.sub(r"{% raw %}\g<0>{% endraw %}", text)


def demote_headings(text: str) -> str:
    """Drop every heading one level for the site.

    The chapters typeset their internal sections as level-1
    headings: the PDF build maps that level onto the section
    hierarchy beneath each chapter title, which is exactly right for
    the book. On the site the page's own title renders as the only
    H1, so level-1 sections would scream in the outline — every
    heading drops one rung at the seam and nests properly beneath
    the page title.

    The walk is fence-aware: the tracking flips at every ``` line,
    so heading-looking lines inside code blocks — comments in YAML
    or properties examples, shebangs in shell snippets — pass
    through untouched. Headings already at the deepest level stay
    put rather than falling off the HTML scale.
    """
    out = []
    fence = False
    for line in text.split("\n"):
        if line.lstrip(" ").startswith("```"):
            fence = not fence
            out.append(line)
            continue
        if not fence and line.startswith("#") and not line.startswith("######"):
            out.append("#" + line)
        else:
            out.append(line)
    return "\n".join(out)


def outdent_nested_callouts(text: str) -> str:
    """Move callouts that sit inside list items to the top level.

    The chapters nest some callouts inside list items, which the PDF
    renders happily — a box inside an item. The site cannot: the
    callout component emits a block-level div and, in Zola 0.23's
    render-then-parse model, an unindented div inside a list context
    tears the surrounding list apart, leaving malformed HTML that
    collapses the layout. So every nested callout travels to the top
    level at the seam.

    The state machine: when an indented callout opening tag is found,
    its indent is measured and then subtracted from every line
    through the closing tag. Lines lose exactly that prefix, so the
    relative structure inside the box — lists, code fences,
    emphasis — survives the move untouched, and lines that do not
    carry the prefix (or are blank) pass through as they are. The
    PDF pipeline keeps the source nesting as typeset.
    """
    out = []
    indent = 0
    for line in text.split("\n"):
        stripped = line.lstrip(" ")
        if not indent and stripped.startswith("{% <devlab.callout") and stripped != line:
            indent = len(line) - len(stripped)
        if indent and (not line.strip() or line.startswith(" " * indent)):
            out.append(line[indent:])
        else:
            out.append(line)
        if indent and stripped.startswith("{% </devlab.callout>"):
            indent = 0
    return "\n".join(out)


# The four diagrams the manual carries, with curated labels and
# descriptions for the component wrapper; any fence beyond the table
# falls back to generic, honest prose.
DIAGRAM_META = {
    ("Building", 0): (
        "arara-home-layout",
        "The arara home layout",
        "The ARARA_HOME directory: the tool's jar beside the rules "
        "directory, the default rule pack with every official rule "
        "in both formats."),
    ("Concepts", 0): (
        "directive-evaluation-flow",
        "The directive evaluation flow",
        "From a directive found in the file to the evaluation of its "
        "command expression."),
    ("Concepts", 1): (
        "command-execution-flow",
        "The command execution flow",
        "The system command runs, and the exit status decides whether "
        "the run proceeds."),
    ("Projects", 0): (
        "multi-project-run",
        "The multi-project run",
        "Specification files merge into one dependency graph, which "
        "resolves the order in which each project runs."),
}


# A leading mermaid init directive for every hatched diagram: the
# renderer lays out at the natural width instead of stretching to
# the page, and the site's stylesheet scales down gracefully when a
# diagram is wider than its column — never up.
DIAGRAM_INIT = "%%{init: { 'flowchart': { 'useMaxWidth': false } } }%%"

# The manual's diagrams are wide ribbons — flowchart LR — chosen for
# the printed page. The site's column is narrow and tall, so the
# seam stands the ribbons up: the flowchart direction flips to TD
# in the assembled copies, and the text renders at its natural size
# instead of shrinking to fit. The PDF keeps its ribbons as set.
FLOWCHART_DIRECTION = re.compile(r"^(\s*(?:flowchart|graph)\s+)LR(\s*)$")


def wrap_mermaid_diagrams(text: str, chapter: str) -> str:
    """Hatch mermaid fences as the theme's diagram components.

    The chapters write diagrams as ```mermaid fences — the PDF
    pipeline reads them through its own figure machinery — but the
    site's theme expects its devlab.diagram component, which carries
    an accessible label and description, and which loads the theme's
    bundled renderer only on the pages that use it (no configuration
    needed: the marker itself is the switch). At the seam, each
    fence's body travels verbatim inside the component wrapper,
    opening with the init directive above so the diagram renders at
    its natural width; curated labels and descriptions ride along
    from the table, and any fence beyond the table gets a generic,
    honest fallback.

    The component tags are emitted at column zero regardless of the
    fence's indentation — a diagram can never be born nested, the
    structural guarantee the callout outdent provides retroactively.
    The state machine: a ```mermaid line opens a component and the
    following bare ``` line closes it, so the fence structure stays
    balanced for any transform that ran before this one. Inside the
    component, the flowchart direction flips from LR to TD — the
    site's column wants its chains standing up, not stretched wide.
    """
    out = []
    inside = False
    index = 0
    for line in text.split("\n"):
        if not inside and line.strip() == "```mermaid":
            diagram_id, label, description = DIAGRAM_META.get(
                (chapter, index),
                (f"{chapter.lower()}-diagram-{index + 1}",
                 "Diagram", "A Mermaid diagram from the arara manual."))
            out.append(
                '{% <devlab.diagram id="' + diagram_id
                + '" label="' + label
                + '" description="' + description + '"> %}')
            out.append(DIAGRAM_INIT)
            inside = True
            index += 1
        elif inside and line.strip() == "```":
            out.append("{% </devlab.diagram> %}")
            inside = False
        elif inside and FLOWCHART_DIRECTION.match(line):
            out.append(FLOWCHART_DIRECTION.sub(r"\1TD\2", line))
        else:
            out.append(line)
    return "\n".join(out)


def assemble(repo: Path, site: Path) -> None:
    """Lift the manual, the quickstart and the changelog into the site."""
    docs = repo / "docs"
    changelog = repo / "CHANGELOG.md"
    content = site / "content"
    if not docs.is_dir():
        raise SystemExit(f"no docs directory at {docs}")
    if not (content / "_index.md").is_file():
        raise SystemExit(f"no website content directory at {content}")

    # The manual directory is generated as a whole: a previous run's
    # output must never survive a new one, so the directory is
    # rebuilt from scratch on every assembly.
    manual = content / "manual"
    if manual.exists():
        shutil.rmtree(manual)
    manual.mkdir(parents=True)
    (manual / "_index.md").write_text(MANUAL_INDEX, encoding="utf-8")

    # The chapters, near-verbatim: the four site transforms in
    # sequence — literal braces neutralized, headings demoted,
    # nested callouts freed, mermaid fences hatched — and nothing
    # else.
    for name in CHAPTERS:
        source = docs / f"{name}.md"
        text = source.read_text(encoding="utf-8")
        text = tera_raw_wrap(text)
        text = demote_headings(text)
        text = outdent_nested_callouts(text)
        text = wrap_mermaid_diagrams(text, name)
        (manual / f"{name}.md").write_text(text, encoding="utf-8")

    # The quickstart is a standalone page rather than a chapter: the
    # same three transforms apply, but it lands in the site's root
    # and keeps the weight it already carries.
    quickstart = (docs / "Quickstart.md").read_text(encoding="utf-8")
    quickstart = tera_raw_wrap(quickstart)
    quickstart = demote_headings(quickstart)
    quickstart = outdent_nested_callouts(quickstart)
    quickstart = wrap_mermaid_diagrams(quickstart, "Quickstart")
    (content / "quickstart.md").write_text(quickstart, encoding="utf-8")

    # The changelog is a plain repository document: the site page
    # gains front matter here, loses its first heading (which would
    # duplicate the page title), and has any literal interpolation
    # delimiters neutralized. Its heading hierarchy is already
    # right for the web — release sections under the page title —
    # so it is not demoted.
    text = changelog.read_text(encoding="utf-8")
    head, _, rest = text.partition("\n")
    if head.startswith("# "):
        text = rest.lstrip("\n")
    (content / "changelog.md").write_text(tera_raw_wrap(CHANGELOG_FRONT + text), encoding="utf-8")

    print(f"assembled {len(CHAPTERS)} chapters, the quickstart and the changelog into {content}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo", type=Path, default=Path(__file__).resolve().parents[1],
        help="the arara checkout (default: this repository)")
    parser.add_argument(
        "--site", type=Path, required=True,
        help="the site checkout: the directory containing config.toml")
    args = parser.parse_args()
    assemble(args.repo, args.site)


if __name__ == "__main__":
    main()
