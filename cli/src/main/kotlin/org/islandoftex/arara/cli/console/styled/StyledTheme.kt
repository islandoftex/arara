// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console.styled

import com.github.ajalt.mordant.rendering.TextColors

/**
 * The visual vocabulary of the styled console: the glyphs with their
 * ASCII fallbacks, the tint of the application logo and the
 * readable measure of the prose, defined in one place so the
 * presentation tunes without touching the renderer. The semantic
 * colors come from the terminal's own theme - success, danger and
 * the muted chrome - so a theme change or a capability drop
 * restyles the run for free. The glyphs ride the color capability:
 * a terminal that cannot carry them falls back to the plain
 * characters every font has - the mid-dot separator and the
 * ellipsis are the exception in the other direction, carried by
 * every font, so they never degrade.
 *
 * @author Island of TeX
 */
object StyledTheme {
    /**
     * The tint of the application logo, the mark of the island.
     */
    val logo = TextColors.cyan

    /**
     * The glyph that marks a running entry.
     */
    const val runningGlyph = "▸"

    /**
     * The ASCII fallback of the running glyph.
     */
    const val runningFallback = ">"

    /**
     * The glyph that marks a success.
     */
    const val successGlyph = "✓"

    /**
     * The ASCII fallback of the success glyph.
     */
    const val successFallback = "+"

    /**
     * The glyph that marks a failure.
     */
    const val failureGlyph = "✗"

    /**
     * The ASCII fallback of the failure glyph.
     */
    const val failureFallback = "x"

    /**
     * The glyph that marks a warning dialog.
     */
    const val warningGlyph = "⚠"

    /**
     * The glyph that marks an informational dialog.
     */
    const val infoGlyph = "ℹ"

    /**
     * The dotted rule character that frames the raw tool output of a
     * verbose run.
     */
    const val frameGlyph = "┈"

    /**
     * The ASCII fallback of the dotted rule character.
     */
    const val frameFallback = "."

    /**
     * The glyph that marks a conditionally skipped directive.
     */
    const val skipGlyph = "◌"

    /**
     * The ASCII fallback of the skip glyph.
     */
    const val skipFallback = "o"

    /**
     * The glyph that marks a simulated dry run.
     */
    const val dryGlyph = "○"

    /**
     * The ASCII fallback of the dry run glyph.
     */
    const val dryFallback = "o"

    /**
     * The rule character of the section frames.
     */
    const val ruleGlyph = "─"

    /**
     * The ASCII fallback of the rule character.
     */
    const val ruleFallback = "-"

    /**
     * The dotted leader between an entry and its result.
     */
    const val leaderGlyph = "·"

    /**
     * The ASCII fallback of the dotted leader.
     */
    const val leaderFallback = "."

    /**
     * The mid-dot that separates an entry's name from its task, a
     * file from its metadata and a landed result from its
     * duration. Every font carries it - legacy consoles included -
     * so it rides undecorated streams like decorated ones.
     */
    const val separatorGlyph = "·"

    /**
     * The ellipsis that marks the truncation of the failure
     * panel's quoted output. Every font carries it - legacy
     * consoles included - so it rides undecorated streams like
     * decorated ones.
     */
    const val ellipsisGlyph = "…"

    /**
     * The vertical bar of the failed task's output panel, the
     * glyph and its inner spacing together.
     */
    const val panelBar = "│ "

    /**
     * The ASCII fallback of the panel bar.
     */
    const val panelBarFallback = "| "

    /**
     * The width of the label column of the verbose task fields.
     */
    const val fieldGutterWidth = 16

    /**
     * The maximal width prose wraps at, even on wide terminals: the
     * readable measure of a text column, kept under the classic
     * terminal width.
     */
    const val proseMaximumWidth = 79
}
