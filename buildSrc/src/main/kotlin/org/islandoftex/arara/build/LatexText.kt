// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.coralmark.ElementFragment
import org.islandoftex.coralmark.Fragment
import org.islandoftex.coralmark.TagElement
import org.islandoftex.coralmark.TextFragment

/**
 * The LaTeX text layer of the manual conversion: the escaping every
 * prose character needs, the rendering of mixed content — table cells,
 * hyperlink labels, and the fragments they are made of — and the
 * TeX-family logo pass.
 *
 * The three concerns share one rule: prose is transformed, verbatim
 * never is. The escaping runs as the conversion's text processor while
 * the tree walk renders, the mixed-content renderers serve the element
 * handlers, and the logo pass runs last, over the restored document,
 * where the verbatim regions are structural enough to be excluded
 * exactly.
 */
internal object LatexText {

    /** The fenced listings: verbatim, never rewritten. */
    private val listingRegex =
        Regex("""\\begin\{araralisting\}.*?\\end\{araralisting\}""", setOf(RegexOption.DOT_MATCHES_ALL))

    /**
     * Inline code: verbatim, never rewritten. The content rides
     * escaped — bare braces never appear in it — so the match admits
     * only escape pairs and stops at the first structural brace, which
     * keeps the span short and the star recursion shallow.
     */
    private val textttRegex =
        Regex("""\\texttt\{(?:[^{}\\]|\\textbackslash\{\}|\\.)*\}""")

    /** The target of a hyperlink: never rewritten, so the link stays whole. */
    private val hrefRegex =
        Regex("""\\href\{[^{}]*\}""")

    /** Everything the logo pass must never touch. */
    private val verbatimRegex =
        Regex(
            "${listingRegex.pattern}|${textttRegex.pattern}|${hrefRegex.pattern}",
            setOf(RegexOption.DOT_MATCHES_ALL),
        )

    /** The TeX-family words whose prose mentions render as their logos. */
    private val logoWords =
        listOf(
            "LaTeX",
            "BibTeX",
            "MiKTeX",
            "pLaTeX",
            "upLaTeX",
            "TeX",
        )

    /**
     * The LaTeX characters that need escaping in prose: each one would be
     * misread by the typesetting engine, so the text processor replaces
     * them with their safe spellings, one character at a time, so no
     * escape sequence can shadow another.
     */
    fun escapeLatex(text: String): String {
        val sb = StringBuilder(text.length * 2)
        for (c in text) {
            when (c) {
                '\\' -> sb.append("\\textbackslash{}")
                '$' -> sb.append("\\$")
                '%' -> sb.append("\\%")
                '#' -> sb.append("\\#")
                '&' -> sb.append("\\&")
                '_' -> sb.append("\\_")
                '{' -> sb.append("\\{")
                '}' -> sb.append("\\}")
                '~' -> sb.append("\\textasciitilde{}")
                '^' -> sb.append("\\textasciicircum{}")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Renders one table cell through its fragments: text runs are
     * escaped, inline code rides texttt, and emphasis and links keep
     * their shape. The same fragment chain serves the hyperlink
     * labels, so both render paths treat mixed content identically.
     */
    fun renderCell(
        cell: TagElement,
        chapterNames: Set<String>,
    ): String = cell.fragments.joinToString("") { renderFragment(it, chapterNames) }

    /**
     * Renders one fragment of a mixed-content element.
     */
    fun renderFragment(
        fragment: Fragment,
        chapterNames: Set<String>,
    ): String =
        when (fragment) {
            is TextFragment -> escapeLatex(fragment.text)
            is ElementFragment -> renderInline(fragment.element, chapterNames)
        }

    /**
     * Renders one hyperlink: external targets ride href, references to
     * sibling chapters become live cross-references through the label
     * stamped on every chapter heading, and anything else keeps the
     * label as plain text — a dead reference is worse than none.
     */
    fun renderLink(
        href: String,
        label: String,
        chapterNames: Set<String>,
    ): String =
        when {
            href.startsWith("http://") || href.startsWith("https://") ->
                "\\href{$href}{$label}"
            href.endsWith(".md") -> {
                val target = href.removePrefix("@/").removeSuffix(".md")
                if (target in chapterNames) {
                    "\\hyperref[chap:$target]{$label}"
                } else {
                    label
                }
            }
            else -> label
        }

    /**
     * Renders an inline element inside a handler-owned subtree.
     */
    private fun renderInline(
        element: TagElement,
        chapterNames: Set<String>,
    ): String {
        val inner = element.fragments.joinToString("") { renderFragment(it, chapterNames) }
        return when (element.tagName) {
            "code" -> "\\texttt{${escapeLatex(element.text)}}"
            "strong" -> "\\textbf{$inner}"
            "em" -> "\\emph{$inner}"
            "a" -> renderLink(element.attr("href").orEmpty(), inner, chapterNames)
            else -> inner
        }
    }

    /**
     * Renders the TeX-family logos: each prose mention becomes its true
     * letterform through hologo, while the verbatim regions — the
     * listings, the inline code, and the hyperlink targets — are split
     * out first, so terminal banners and code samples keep their
     * literal text.
     */
    fun renderLogos(latex: String): String {
        val sb = StringBuilder()
        var index = 0
        for (match in verbatimRegex.findAll(latex)) {
            sb.append(rewriteLogos(latex.substring(index, match.range.first)))
            sb.append(match.value)
            index = match.range.last + 1
        }
        sb.append(rewriteLogos(latex.substring(index)))
        return sb.toString()
    }

    /**
     * Rewrites the logo words of one prose segment; the word boundaries
     * keep family members such as `BibTeX` from colliding with the
     * standalone rules, and the lookbehind keeps the pass idempotent:
     * the words inside an already rendered logo — the callout bodies
     * convert once recursively, and their output rides the outer pass
     * — are never rewritten again. The backslash rides doubled: the
     * replacement string treats a lone one as its own escape. The
     * robust logo form renders the same letterforms and survives the
     * moving arguments — the headings, the table of contents, the
     * bookmarks.
     */
    private fun rewriteLogos(segment: String): String =
        logoWords.fold(segment) { text, word ->
            text.replace(Regex("(?<!hologoRobust\\{)\\b$word\\b"), "\\\\hologoRobust{$word}")
        }
}
