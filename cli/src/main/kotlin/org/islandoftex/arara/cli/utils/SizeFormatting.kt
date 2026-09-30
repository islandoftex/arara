// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.math.ln
import kotlin.math.pow
import org.islandoftex.arara.core.session.Session

/**
 * Gets a human readable representation of a size.
 *
 * @param size The byte size to be converted.
 * @return A string representation of the size.
 */
@Suppress("MagicNumber")
internal fun byteSizeToString(size: Long): String {
    val conversionFactor = 1000.0
    return if (size < conversionFactor) "$size B"
    else
        (ln(size.toDouble()) / ln(conversionFactor)).toInt().let { exp ->
            val baseSize = size / conversionFactor.pow(exp.toDouble())
            val ones = baseSize.toInt()
            val tenths = ((baseSize - ones) * 10).toInt()
            val separator = Session.userInterfaceOptions
                .locale.decimalSeparator.toString()
            "$ones$separator$tenths " +
                "${"kMGTPE"[exp - 1]}B"
        }
}
