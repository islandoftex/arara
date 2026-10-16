// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.TextColors

/** Shared terminal colors, glyphs and layout widths, with ASCII fallbacks. */
object StyledTheme {
    /** Application logo color. */
    val logo = TextColors.cyan

    /** Unicode running-task marker. */
    const val runningGlyph = "▸"

    /** ASCII running-task marker. */
    const val runningFallback = ">"

    /** Unicode successful-task marker. */
    const val successGlyph = "✓"

    /** ASCII successful-task marker. */
    const val successFallback = "+"

    /** Unicode failed-task marker. */
    const val failureGlyph = "✗"

    /** ASCII failed-task marker. */
    const val failureFallback = "x"

    /** Unicode horizontal rule. */
    const val ruleGlyph = "─"

    /** ASCII horizontal rule. */
    const val ruleFallback = "-"

    /** Unicode task-to-result leader. */
    const val leaderGlyph = "·"

    /** ASCII task-to-result leader. */
    const val leaderFallback = "."

    /** Unicode subprocess-output frame rule. */
    const val frameGlyph = "┈"

    /** ASCII subprocess-output frame rule. */
    const val frameFallback = "."

    /** Preferred label gutter, in terminal cells. */
    const val fieldGutterWidth = 16

    /** Minimum space beside a label, allowing one wide grapheme. */
    const val minimumValueWidth = 2

    /** Maximum prose width, in terminal cells. */
    const val proseMaximumWidth = 79
}
