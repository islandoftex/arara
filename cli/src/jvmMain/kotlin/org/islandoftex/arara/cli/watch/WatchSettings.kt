// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import org.islandoftex.arara.core.watch.WatchMode

internal data class WatchSettings(
    val mode: WatchMode = WatchMode.AUTO,
    val debounceMillis: Long = DEFAULT_DEBOUNCE.toLong(),
    val intervalMillis: Long = DEFAULT_INTERVAL.toLong()
) {
    companion object {
        const val DEFAULT_DEBOUNCE = 300
        const val DEFAULT_INTERVAL = 1000
    }
}
