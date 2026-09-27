// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import org.islandoftex.arara.mvel.utils.MvelState

/**
 * Implements configuration utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object ConfigurationUtils {
    /**
     * The base names a configuration file may take, without extension, in
     * lookup order.
     */
    private val baseNames = listOf(
        ".araraconfig", "araraconfig", ".arararc", "arararc"
    )

    /**
     * The configuration file in use.
     *
     * arara first looks for YAML configuration files, then for Lua
     * configuration files with the same base names. Within each format,
     * files in the project's working directory take precedence; if no
     * configuration file is found there, arara looks up a global one in
     * the user home directory.
     */
    fun configFileForProject(project: Project): MPPPath? =
        listOf("yaml", "lua").firstNotNullOfOrNull { extension ->
            configFileWithExtension(project, extension)
        }

    /**
     * Find the first existing configuration file with the provided
     * [extension], preferring the project's working directory over the
     * user home directory.
     */
    private fun configFileWithExtension(
        project: Project,
        extension: String
    ): MPPPath? {
        val names = baseNames.map { "$it.$extension" }
        return project.workingDirectory.let { workingDir ->
            names.map { workingDir / it }.firstOrNull { it.exists }
        } ?: Environment.getSystemPropertyOrNull("user.home")
            ?.let { userHome ->
                names.map { MPPPath(userHome) / it }
                    .firstOrNull { it.exists }
            }
    }

    /**
     * Validates the configuration file.
     *
     * @param file The configuration file.
     * @param currentProject The project the configuration is loaded for;
     *   its working directory is in scope to a Lua script as `cwd`.
     * @return The configuration file as a resource.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    private fun loadLocalConfiguration(
        file: MPPPath,
        currentProject: Project
    ): LocalConfiguration =
        when (file.toString().substringAfterLast('.')) {
            "yaml" -> LocalConfiguration.load(file)
            "lua" -> LuaLocalConfiguration.load(
                file,
                currentProject.workingDirectory
            )
            else -> throw AraraException(
                "The configuration file '$file' uses a format arara does " +
                    "not support."
            )
        }

    /**
     * Loads the application configuration.
     *
     * @param file The configuration file (YAML or Lua) to read the
     * configuration from.
     * @param currentProject The project the configuration is loaded for;
     *   its working directory anchors relative rule paths.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(file: MPPPath, currentProject: Project) {
        // then validate it and update the configuration accordingly
        val resource = loadLocalConfiguration(file, currentProject)
        LinearExecutor.executionOptions = resource.toExecutionOptions(
            currentProject,
            LinearExecutor.executionOptions
        )
        Session.loggingOptions = resource.toLoggingOptions(
            Session.loggingOptions
        )
        Session.userInterfaceOptions = resource.toUserInterfaceOptions(
            Session.userInterfaceOptions
        )

        MvelState.preambles += resource.preambles
        MvelState.defaultPreamble = resource.defaultPreamble
        MvelState.prependPreambleIfDirectivesGiven = resource.prependPreambleIfDirectivesGiven

        // just to be sure, update the current locale in order to
        // display localized messages and reset logging status
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        LoggingUtils.setupLogging(Session.loggingOptions)
    }
}
