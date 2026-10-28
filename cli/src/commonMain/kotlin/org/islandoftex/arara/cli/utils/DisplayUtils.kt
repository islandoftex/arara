// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.math.ln
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.ui.ConsoleRenderer
import org.islandoftex.arara.api.ui.DisplayRunActions
import org.islandoftex.arara.api.ui.DisplayTaskActions
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.core.reporting.RunLedger
import org.islandoftex.arara.core.session.Session

/** The generated version object belongs to the platform source set. */
internal expect fun applicationVersion(): String

/** Stable entry points composed from focused operation domains. */
object DisplayUtils : DisplayRunActions by displayRunActions,
    DisplayTaskActions by displayTaskActions {

    private const val MILLISECOND_DIGITS = 3
    private const val BYTES_PER_UNIT = 1000.0
    private const val DECIMAL_TENTHS = 10

    /** Active terminal renderer; log output remains independent of this choice. */
    var renderer: ConsoleRenderer = PlainConsoleRenderer()
    internal var presentation = RunLedger()

    /** Selected configuration filename shown in diagnostics. */
    var configurationFileName: String = "[none]"
    /** Classic ASCII application logo. */
    val logoString: String = """
              __ _ _ __ __ _ _ __ __ _
             / _` | '__/ _` | '__/ _` |
            | (_| | | | (_| | | | (_| |
             \__,_|_|  \__,_|_|  \__,_|
    """.trimIndent()

    /** Version greeting based on the generated application version. */
    val versionLine: String = "arara ${applicationVersion()}, the cool TeX automation tool."

    internal fun elapsedSeconds(value: Double): String {
        val duration = value.seconds
        val whole = duration.inWholeSeconds
        val milliseconds = (duration - whole.seconds).inWholeMilliseconds.toString().padStart(MILLISECOND_DIGITS, '0')
        return "$whole${Session.userInterfaceOptions.locale.decimalSeparator}$milliseconds"
    }
    internal fun byteSizeToString(size: Long): String {
        val conversionFactor = BYTES_PER_UNIT
        return if (size < conversionFactor) "$size B"
        else
            (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
                val baseSize = size / conversionFactor.pow(exp.toDouble())
                val ones = baseSize.toInt()
                val tenths = ((baseSize - ones) * DECIMAL_TENTHS).toInt()
                val separator = Session.userInterfaceOptions.locale.decimalSeparator
                val prefix = "kMGTPE"[exp - 1]
                "$ones$separator$tenths ${prefix}B"
            }
    }
}
