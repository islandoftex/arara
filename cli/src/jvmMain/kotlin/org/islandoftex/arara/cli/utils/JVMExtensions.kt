// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import korlibs.time.DateFormat
import korlibs.time.format
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.utils.formatString

private val logger = KotlinLogging.logger { }

/**
 * Displays the file information in the terminal.
 */
fun ProjectFile.printFileInformation() {
    logger.info {
        "\n" + LanguageController.messages.LOG_INFO_WELCOME_MESSAGE
            .formatString(AraraAPI.version) + "\n" +
            DisplayUtils.displaySeparator()
    }
    logger.debug {
        """

            ::: arara @ ${DisplayUtils.applicationPath}
            ::: Java %s, %s
            ::: %s
            ::: %s, %s, %s
            ::: user.home @ %s
            ::: CF @ ${DisplayUtils.configurationFileName}
            ${DisplayUtils.displaySeparator()}
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
    LanguageController.messages.INFO_DISPLAY_FILE_INFORMATION
        .formatString(
            path.fileName,
            DisplayUtils.byteSizeToString(path.fileSize),
            DateFormat("yyyy-MM-dd HH:mm:ss")
                .format(path.lastModified)
        ).let {
            logger.info { it }
            DisplayUtils.printWrapped(it)
            println()
        }
}
