// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.utils.formatString

private val logger = KotlinLogging.logger { }

/**
 * Displays the file information in the terminal.
 */
fun ProjectFile.printFileInformation() {
    RunLog.welcome(AraraAPI.version)
    logger.debug {
        """

            ::: arara @ ${RunLog.applicationPath}
            ::: Java %s, %s
            ::: %s
            ::: %s, %s, %s
            ::: user.home @ %s
            ::: CF @ ${DisplayUtils.configurationFileName}
            ${RunLog.separator()}
        """.trimIndent().formatString(
            Environment.getSystemProperty(
                "java.version",
                "[unknown version]"
            ),
            Environment.getSystemProperty(
                "java.vendor",
                "[unknown vendor]"
            ),
            Environment.getSystemProperty(
                "java.home",
                "[unknown location]"
            ),
            Environment.getSystemProperty(
                "os.name",
                "[unknown OS name]"
            ),
            Environment.getSystemProperty(
                "os.arch",
                "[unknown OS arch]"
            ),
            Environment.getSystemProperty(
                "os.version",
                "[unknown OS version]"
            ),
            Environment.getSystemProperty(
                "user.home",
                "[unknown user's home directory]"
            )
        )
    }
    RunLog.fileInformation(this)
    DisplayUtils.printFileInformation(this)
}
