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

    const val proseMaximumWidth = 79
}
