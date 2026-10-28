// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.watch

import java.nio.file.Files
import kotlin.test.Test
import org.islandoftex.arara.api.files.WatchMode
import org.islandoftex.arara.api.files.toMPPPath

class WatchMonitorTest {

    @Test
    fun allModesRegisterReconcileAndReleaseDirectories() {
        val root = Files.createTempDirectory("arara-monitor").toMPPPath()
        try {
            WatchMode.entries.forEach { mode ->
                WatchMonitor(mode).use { monitor ->
                    monitor.update(setOf(root))
                    root.resolve("input.tex").writeText(mode.name)
                    monitor.await(1)
                    monitor.update(emptySet())
                    monitor.await(1)
                }
            }
        } finally {
            root.removeDirectory()
        }
    }
}
