// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.Arara
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.mvel.configuration.LocalConfiguration

/**
 * Implements configuration utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object ConfigurationUtils {
    /**
     * The configuration file in use.
     *
     * Look for configuration files in the user's working directory first
     * if no configuration files are found in the user's working directory,
     * try to look up in a global directory, that is, the user home.
     */
    val configFile: Path?
        get() {
            val names = listOf(".araraconfig.yaml",
                    "araraconfig.yaml", ".arararc.yaml", "arararc.yaml")
            Arara.currentProject.workingDirectory
                    .let { workingDir ->
                        val first = names
                                .map { workingDir.resolve(it) }
                                .firstOrNull { Files.exists(it) }
                        if (first != null)
                            return first
                    }
            return Environment.getSystemPropertyOrNull("user.home")
                    ?.let { userHome ->
                        names.map { Paths.get(userHome).resolve(it) }
                                .firstOrNull { Files.exists(it) }
                    }
        }

    /**
     * Validates the configuration file.
     *
     * @param file The configuration file.
     * @return The configuration file as a resource.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun loadLocalConfiguration(file: Path): LocalConfiguration {
        return if (file.fileName.toString().endsWith(".yaml"))
            LocalConfiguration.load(file)
        else
            TODO("Kotlin DSL not implemented yet")
    }

    /**
     * Loads the application configuration.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(file: Path) {
        // then validate it and update the configuration accordingly
        val resource = loadLocalConfiguration(file)
        LinearExecutor.executionOptions = resource.toExecutionOptions(
                Arara.currentProject,
                LinearExecutor.executionOptions
        )
        Session.loggingOptions = resource.toLoggingOptions()
        Session.userInterfaceOptions = resource.toUserInterfaceOptions()

        // just to be sure, update the current locale in order to
        // display localized messages and reset logging status
        LanguageController.setLocale(Session.userInterfaceOptions.locale)
        LoggingUtils.setupLogging(Session.loggingOptions)
    }
}
