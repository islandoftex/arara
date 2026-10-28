// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.islandoftex.arara.cli.console.ConsoleMode
import org.islandoftex.arara.core.configuration.UserInterfaceOptions

/** Parsed presentation options; registration remains owned by the command. */
internal class PresentationFlags : OptionGroup() {
    val language by option("-L", "--language")
        .help("Set the application language")

    val console by option("-c", "--console")
        .help("Set the console presentation: plain or styled")
        .choice("plain" to ConsoleMode.PLAIN, "styled" to ConsoleMode.STYLED)

    val terminalWidth by option("--terminal-width")
        .help("Set the maximum console width in columns (at least 20)")
        .int().restrictTo(min = UserInterfaceOptions.MINIMUM_TERMINAL_WIDTH)
}
