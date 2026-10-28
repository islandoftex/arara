// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import org.islandoftex.arara.api.files.WatchMode
import org.islandoftex.arara.cli.watch.WatchSettings

/** Parsed watch options; registration remains owned by the command. */
internal class WatchFlags : OptionGroup() {
    val watch by option("--watch")
        .help("Continuously compile Lua projects when their inputs change")
        .flag()

    val watchMode by option("--watch-mode")
        .help("Filesystem monitoring: auto, native or polling")
        .choice("auto" to WatchMode.AUTO,
            "native" to WatchMode.NATIVE,
            "polling" to WatchMode.POLLING)
        .default(WatchMode.AUTO)

    val watchDebounce by option("--watch-debounce")
        .help("Wait for input changes to settle (milliseconds; default 300)")
        .int().restrictTo(min = 1).default(WatchSettings.DEFAULT_DEBOUNCE)

    val watchInterval by option("--watch-interval")
        .help("Maximum delay between content reconciliations (milliseconds; default 1000)")
        .int().restrictTo(min = 1).default(WatchSettings.DEFAULT_INTERVAL)
}
