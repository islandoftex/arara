// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import com.charleskorn.kaml.Yaml
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.configuration.LoggingOptions
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.mvel.files.FileType
import org.mvel2.templates.TemplateRuntime

/**
 * A local configuration which resembles configuration files in the working
 * directory.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
@ExperimentalTime
data class LocalConfiguration(
    private var paths: List<String> = listOf(),
    private var filetypes: List<FileType> = listOf(),
    private var language: String = UserInterfaceOptions().locale.toLanguageTag(),
    private var loops: Int = ExecutionOptions().maxLoops,
    private var verbose: Boolean = ExecutionOptions().verbose,
    private var logging: Boolean = LoggingOptions().enableLogging,
    private var header: Boolean = ExecutionOptions().parseOnlyHeader,
    private var dbname: String = ExecutionOptions().databaseName.toString(),
    private var logname: String = LoggingOptions().logFile.toString(),
    private var laf: String = UserInterfaceOptions().swingLookAndFeel
) {
    /**
     * Convert the relevant properties of the configuration to execution
     * options. Intended to be used together with [toLoggingOptions] and
     * [toUserInterfaceOptions] to destructure and discard this object.
     *
     * @return The corresponding execution options.
     */
    @Throws(AraraException::class)
    fun toExecutionOptions(
        currentProject: Project,
        baseOptions: org.islandoftex.arara.api.configuration.ExecutionOptions = ExecutionOptions()
    ): org.islandoftex.arara.api.configuration.ExecutionOptions {
        val preprocessedPaths = paths.map { it.trim() }.map { input ->
            try {
                TemplateRuntime.eval(input, mapOf(
                        "user" to mapOf(
                                "home" to (Environment.getSystemPropertyOrNull("user.home") ?: ""),
                                "name" to (Environment.getSystemPropertyOrNull("user.name") ?: "")
                        ),
                        "application" to mapOf(
                                "workingDirectory" to currentProject.workingDirectory.toAbsolutePath().toString()
                        )
                )) as String
            } catch (_: RuntimeException) {
                // do nothing, gracefully fallback to
                // the default, unparsed path
                input
            }
        }
        val databaseName = Paths.get(cleanFileName(dbname))
        val maxLoops = if (loops > 0) {
            loops
        } else {
            throw AraraException(
                    LanguageController.messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
            )
        }
        return ExecutionOptions
                .from(baseOptions)
                .copy(
                        maxLoops = maxLoops,
                        verbose = verbose,
                        databaseName = databaseName,
                        fileTypes = filetypes
                                .plus(baseOptions.fileTypes),
                        rulePaths = preprocessedPaths.map { Paths.get(it) }
                                .plus(baseOptions.rulePaths)
                                .toSet(),
                        parseOnlyHeader = header
                )
    }

    /**
     * Convert the relevant properties of the configuration to logging options.
     * Intended to be used together with [toExecutionOptions] and
     * [toUserInterfaceOptions] to destructure and discard this object.
     *
     * @return The corresponding logging options.
     */
    fun toLoggingOptions(): org.islandoftex.arara.api.configuration.LoggingOptions {
        val logName = Paths.get(cleanFileName(logname))
        return LoggingOptions(
                enableLogging = logging,
                logFile = logName
        )
    }

    /**
     * Convert the relevant properties of the configuration to UI options.
     * Intended to be used together with [toExecutionOptions] and
     * [toLoggingOptions] to destructure and discard this object.
     *
     * @return The corresponding user interface options.
     */
    fun toUserInterfaceOptions(): org.islandoftex.arara.api.configuration.UserInterfaceOptions {
        return UserInterfaceOptions(
                locale = Locale.forLanguageTag(language),
                swingLookAndFeel = laf
        )
    }

    /**
     * Cleans the file name to avoid invalid entries.
     *
     * @param name The file name.
     * @return A cleaned file name.
     */
    private fun cleanFileName(name: String): String {
        val result = File(name).name.trim()
        return if (result.isEmpty()) "arara" else result.trim()
    }

    companion object {
        /**
         * Validates the configuration file.
         *
         * @param file The configuration file.
         * @return The configuration file as a resource.
         * @throws AraraException Something wrong happened, to be caught in the
         * higher levels.
         */
        @Throws(AraraException::class)
        fun load(file: Path): LocalConfiguration =
                file.runCatching {
                    val text = file.toFile().readText()
                    if (!text.startsWith("!config"))
                        throw Exception("Configuration should start with !config")
                    Yaml.default.parse(LocalConfiguration.serializer(),
                            text)
                }.getOrElse {
                    throw AraraException(
                            LanguageController.messages.ERROR_CONFIGURATION_GENERIC_ERROR,
                            it
                    )
                }
    }
}
