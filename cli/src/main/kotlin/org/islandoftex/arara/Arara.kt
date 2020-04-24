// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara

import com.github.ajalt.clikt.parameters.options.versionOption
import java.nio.file.Paths
import java.time.LocalDate
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.files.UNKNOWN_TYPE
import org.islandoftex.arara.core.localization.LanguageController

/**
 * arara's main entry point
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
object Arara {
    /**
     * arara's current exit code
     */
    @JvmStatic
    var exitCode = 0

    /**
     * arara's project it is currently working on.
     *
     * TODO: sane initialization
     */
    @JvmStatic
    var currentProject: Project =
            org.islandoftex.arara.core.files.Project("", Paths.get(""), setOf())

    /**
     * arara's main file it is currently working on.
     *
     * TODO: sane initialization
     */
    @JvmStatic
    var currentFile: ProjectFile = org.islandoftex.arara.core.files
            .ProjectFile(Paths.get("/tmp/"), FileType.UNKNOWN_TYPE)

    /**
     * arara's user interface configuration.
     */
    @JvmStatic
    var userInterfaceOptions: UserInterfaceOptions =
            org.islandoftex.arara.core.configuration.UserInterfaceOptions()

    /**
     * arara's logging configuration.
     */
    var loggingOptions: LoggingOptions =
            org.islandoftex.arara.core.configuration.LoggingOptions()

    /**
     * Main method. This is the application entry point.
     * @param args A string array containing all command line arguments.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // print the arara logo in the terminal; I just
        // hope people use this tool in a good terminal with
        // fixed-width fonts, otherwise the logo will be messed
        DisplayUtils.printLogo()

        CLI().versionOption(AraraAPI.version, names = setOf("-V", "--version"),
                message = {
                    "arara ${AraraAPI.version}\n" +
                            "Copyright (c) ${LocalDate.now().year}, Island of TeX\n" +
                            LanguageController.messages.INFO_PARSER_NOTES + "\n\n" +
                            "New features in version ${AraraAPI.version}:\n" +
                            Arara::class.java
                                    .getResource("/org/islandoftex/arara/cli/configuration/release-notes")
                                    .readText()
                })
                .main(args)
    }
}
