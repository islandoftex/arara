// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.mvel.configuration.LocalConfiguration
import org.islandoftex.arara.mvel.utils.MvelState
import kotlin.time.ExperimentalTime

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
    fun configFileForProject(project: Project): MPPPath? {
        val names =
            listOf(
                ".araraconfig.yaml",
                "araraconfig.yaml",
                ".arararc.yaml",
                "arararc.yaml",
            )
        return project.workingDirectory.let { workingDir ->
            names.map { workingDir / it }.firstOrNull { it.exists }
        } ?: Environment
            .getSystemPropertyOrNull("user.home")
            ?.let { userHome ->
                names
                    .map { MPPPath(userHome) / it }
                    .firstOrNull { it.exists }
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
    private fun loadLocalConfiguration(file: MPPPath): LocalConfiguration =
        if (file.fileName.endsWith(".yaml")) {
            LocalConfiguration.load(file)
        } else {
            TODO("Kotlin DSL not implemented yet")
        }

    /**
     * Loads the application configuration.
     *
     * @param file The YAML file to read the configuration from.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @OptIn(ExperimentalTime::class)
    @Throws(AraraException::class)
    fun load(
        file: MPPPath,
        currentProject: Project,
    ) {
        // then validate it and update the configuration accordingly
        val resource = loadLocalConfiguration(file)
        LinearExecutor.executionOptions =
            resource.toExecutionOptions(
                currentProject,
                LinearExecutor.executionOptions,
            )
        Session.loggingOptions =
            resource.toLoggingOptions(
                Session.loggingOptions,
            )
        Session.userInterfaceOptions =
            resource.toUserInterfaceOptions(
                Session.userInterfaceOptions,
            )

        MvelState.preambles += resource.preambles
        MvelState.defaultPreamble = resource.defaultPreamble
        MvelState.prependPreambleIfDirectivesGiven =
            resource.prependPreambleIfDirectivesGiven

        // just to be sure, update the current locale in order to
        // display localized messages and reset logging status
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        LoggingUtils.setupLogging(Session.loggingOptions)
    }
}
