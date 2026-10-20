// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import java.nio.charset.Charset
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.bufferedReader
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.utils.LoggingUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.configuration.LocalConfiguration
import org.islandoftex.arara.mvel.configuration.YamlLocalConfiguration
import org.islandoftex.arara.lua.configuration.LuaLocalConfiguration
import org.islandoftex.arara.mvel.utils.MvelState

/**
 * Resolves configuration files and loads configuration and call properties.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object ConfigurationFiles {
    /**
     * The configuration file in use.
     *
     * Look for configuration files in the user's working directory first
     * if no configuration files are found in the user's working directory,
     * try to look up in a global directory, that is, the user home. Within
     * each directory Lua names precede YAML names. Only regular files qualify.
     */
    fun configFileForProject(project: Project): MPPPath? {
        val names = listOf("lua", "yaml").flatMap { extension ->
            listOf(".araraconfig", "araraconfig", ".arararc", "arararc").map { "$it.$extension" }
        }
        val directories = listOfNotNull(
            project.workingDirectory,
            Environment.getSystemPropertyOrNull("user.home")?.let { MPPPath(it) }
        )
        return directories.firstNotNullOfOrNull { directory ->
            names.map { directory / it }.firstOrNull { it.exists && it.isRegularFile }
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
    private fun loadLocalConfiguration(file: MPPPath, currentProject: Project): LocalConfiguration =
        when (file.fileName.substringAfterLast('.')) {
            "yaml" -> YamlLocalConfiguration.load(file)
            "lua" -> LuaLocalConfiguration.load(file, currentProject.workingDirectory)
            else -> throw AraraException("Unsupported configuration format: $file")
        }

    /**
     * Loads the application configuration.
     *
     * @param file The YAML or Lua configuration file.
     * @param configureLogging Apply the destination immediately; CLI callers defer this until overrides are applied.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun load(file: MPPPath, currentProject: Project, configureLogging: Boolean = true) {
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

        MvelState.preambles.clear()
        MvelState.preambles += resource.preambles
        MvelState.defaultPreamble = resource.defaultPreamble
        MvelState.prependPreambleIfDirectivesGiven = resource.prependPreambleIfDirectivesGiven

        // just to be sure, update the current locale in order to
        // display localized messages and reset logging status
        LanguageController.loadMessagesFor(Session.userInterfaceOptions.locale)
        if (configureLogging) LoggingUtils.setupLogging(Session.loggingOptions)
    }

    /** Loads UTF-8 call properties, retaining the existing best-effort behavior on failure. */
    fun loadProperties(properties: Properties, path: Path, charset: Charset = Charsets.UTF_8) {
        runCatching { path.bufferedReader(charset).use { properties.load(it) } }
    }

    /** Converts property keys and values to strings for session arguments. */
    fun toMap(properties: Properties): Map<String, String> =
        properties.entries.associate { "${it.key}" to "${it.value}" }
}
