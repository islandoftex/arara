// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara

import com.github.ajalt.clikt.parameters.options.versionOption
import com.uchuhimo.konf.Config
import java.time.LocalDate
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.utils.DisplayUtils

/**
 * arara's main entry point
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
object Arara {
    // TODO: watch config files
    val baseconfig = Config { addSpec(AraraSpec) }
            .from.env()
            .from.systemProperties()
    var config = baseconfig.withLayer("initial")

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

        val version = config[AraraSpec.version]
        CLI().versionOption(version, names = setOf("-V", "--version"),
                message = {
                    "arara $version\n" +
                            "Copyright (c) ${LocalDate.now().year}, Island of TeX\n" +
                            LanguageController.getMessage(Messages
                                    .INFO_PARSER_NOTES) + "\n\n" +
                            "New features in version $version:\n" +
                            Arara::class.java
                                    .getResource("/org/islandoftex/arara/cli/configuration/release-notes")
                                    .readText()
                })
                .main(args)
    }
}
