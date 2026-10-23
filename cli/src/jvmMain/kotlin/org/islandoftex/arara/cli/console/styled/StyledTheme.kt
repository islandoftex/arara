// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.TextColors

object StyledTheme {
    val logo = TextColors.cyan

    const val runningGlyph = "▸"

    const val runningFallback = ">"

    const val successGlyph = "✓"

    const val successFallback = "+"

    const val failureGlyph = "✗"

    const val failureFallback = "x"

    const val ruleGlyph = "─"

    const val ruleFallback = "-"

    const val leaderGlyph = "·"

    const val leaderFallback = "."

    const val frameGlyph = "┈"

    const val frameFallback = "."

    const val fieldGutterWidth = 16

    // Keep room for a wide grapheme beside a label.
    const val minimumValueWidth = 2

    const val proseMaximumWidth = 79
}
