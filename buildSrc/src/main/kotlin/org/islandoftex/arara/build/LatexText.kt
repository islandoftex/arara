// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.islandoftex.coralmark.ElementFragment
import org.islandoftex.coralmark.Fragment
import org.islandoftex.coralmark.TagElement
import org.islandoftex.coralmark.TextFragment

/**
 * The LaTeX text layer of the manual conversion: the escaping every
 * prose character needs, and the rendering of mixed content — table
 * cells, hyperlink labels, and the fragments they are made of.
 */
internal object LatexText {

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
     * their shape.
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
}
