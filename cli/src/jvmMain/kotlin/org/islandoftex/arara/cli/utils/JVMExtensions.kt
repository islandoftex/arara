// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.session.Environment

private val logger = KotlinLogging.logger { }

/**
 * Displays the file information in the terminal.
 */
fun ProjectFile.printFileInformation() {
    RunLog.welcome(AraraAPI.version)
    logger.debug {
        val javaVersion = Environment.getSystemProperty("java.version", "[unknown version]")
        val javaVendor = Environment.getSystemProperty("java.vendor", "[unknown vendor]")
        val javaHome = Environment.getSystemProperty("java.home", "[unknown location]")
        val osName = Environment.getSystemProperty("os.name", "[unknown OS name]")
        val osArch = Environment.getSystemProperty("os.arch", "[unknown OS arch]")
        val osVersion = Environment.getSystemProperty("os.version", "[unknown OS version]")
        val userHome = Environment.getSystemProperty("user.home", "[unknown user's home directory]")
        """

            ::: arara @ ${RunLog.applicationPath}
            ::: Java $javaVersion, $javaVendor
            ::: $javaHome
            ::: $osName, $osArch, $osVersion
            ::: user.home @ $userHome
            ::: CF @ ${DisplayUtils.configurationFileName}
            ${RunLog.separator()}
        """.trimIndent()
    }
    RunLog.fileInformation(this)
    DisplayUtils.printFileInformation(this)
}
