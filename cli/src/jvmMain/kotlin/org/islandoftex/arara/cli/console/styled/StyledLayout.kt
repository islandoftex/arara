// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.Line
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Text
import java.util.Locale
import kotlin.time.Duration
import org.islandoftex.arara.core.session.Session

/** Measure terminal cells rather than UTF-16 units, including wide and combining characters. */
internal class StyledLayout(private val terminal: Terminal) {
    val width: Int get() = minOf(terminal.size.width.coerceAtLeast(1),
        Session.userInterfaceOptions.terminalOutputWidthLimit?.coerceAtLeast(1) ?: Int.MAX_VALUE)

    fun cells(text: String): Int = Text(text).measure(terminal, Int.MAX_VALUE).max

    fun fit(text: String, budget: Int): String {
        if (budget <= 0) return ""
        val line = text.replace('\n', ' ').replace('\r', ' ')
        return if (cells(line) <= budget) line else abbreviate(line, budget)
    }

    private fun abbreviate(line: String, budget: Int): String {
        val suffix = ".".repeat(minOf(ELLIPSIS_WIDTH, budget))
        var available = budget - suffix.length
        val spans = parse(line).single().flatMap { span ->
            GRAPHEME.findAll(span.text).mapNotNull { match ->
                val size = cells(match.value)
                if (available >= size) {
                    available -= size
                    Span.word(match.value, span.style)
                } else {
                    available = -1
                    null
                }
            }.toList()
        }
        return terminal.render(spans.joinToString("") { it.style(it.text) }, whitespace = Whitespace.PRE) + suffix
    }

    fun print(text: String) {
        terminal.println(render(text), whitespace = Whitespace.PRE)
    }

    fun render(text: String, budget: Int = width): String {
        // Mordant 3.1's BREAK_WORD repeats an overlong word after splitting it.
        // Wrap the parsed spans ourselves, retaining styles and complete graphemes.
        val lines = parse(text).flatMap { wrap(it, budget.coerceAtLeast(1)) }
        return lines.joinToString("\n", transform = ::renderLine)
    }

    private fun renderLine(line: Line): String {
        val result = StringBuilder()
        val text = StringBuilder()
        var style: TextStyle? = null
        line.spans.forEach { span ->
            if (style != span.style) {
                style?.let { result.append(it(text.toString())) }
                text.clear()
                style = span.style
            }
            text.append(span.text)
        }
        style?.let { result.append(it(text.toString())) }
        return result.toString()
    }

    private fun parse(text: String): List<Line> =
        Text(text, whitespace = Whitespace.PRE).render(terminal, Int.MAX_VALUE).lines

    private fun wrap(line: Line, budget: Int): List<Line> {
        val result = mutableListOf<Line>()
        var spans = mutableListOf<Span>()
        var used = 0
        fun nextLine() {
            result.add(Line(spans))
            spans = mutableListOf()
            used = 0
        }
        val words = line.spans.flatMap { span ->
            WORD.findAll(span.text).map { it.value to span.style }.toList()
        }
        words.forEach { (value, style) ->
            val size = cells(value)
            if (used > 0 && used + size > width && size <= width) nextLine()
            GRAPHEME.findAll(value).forEach { grapheme ->
                val part = grapheme.value
                val partWidth = cells(part)
                if (used > 0 && used + partWidth > budget) nextLine()
                spans.add(Span.word(part, style))
                used += partWidth
            }
        }
        result.add(Line(spans))
        return result
    }

    fun seconds(elapsed: Duration): String = String.format(Locale.ROOT, "%.1f", elapsed.inWholeMilliseconds / MILLIS)
        .replace('.', Session.userInterfaceOptions.locale.decimalSeparator) + " s"

    private companion object {
        const val ELLIPSIS_WIDTH = 3
        const val MILLIS = 1000.0
        val GRAPHEME = Regex("\\X")
        val WORD = Regex("\\s+|\\S+")
    }
}
