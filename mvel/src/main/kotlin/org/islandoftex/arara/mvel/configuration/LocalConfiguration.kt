// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import com.charleskorn.kaml.Yaml
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.files.FileHandling
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
data class LocalConfiguration(
    private val paths: List<String> = listOf(),
    private val filetypes: List<FileType> = listOf(),
    private val language: String? = null,
    private val loops: Int? = null,
    private val verbose: Boolean? = null,
    private val logging: Boolean? = null,
    private val header: Boolean? = null,
    private val dbname: String? = null,
    private val logname: String? = null,
    private val laf: String? = null,
    val preambles: Map<String, String> = mapOf()
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
        baseOptions: ExecutionOptions = org.islandoftex.arara.core.configuration.ExecutionOptions()
    ): ExecutionOptions {
        val templateContext = mapOf(
                "user" to mapOf(
                        "home" to (Environment.getSystemPropertyOrNull("user.home") ?: ""),
                        "name" to (Environment.getSystemPropertyOrNull("user.name") ?: "")
                ),
                "application" to mapOf(
                        "workingDirectory" to currentProject.workingDirectory.toAbsolutePath().toString()
                )
        )
        val preprocessedPaths = paths.asSequence()
                .map { it.trim() }
                .map { input ->
                    try {
                        TemplateRuntime.eval(input, templateContext) as String
                    } catch (_: RuntimeException) {
                        // do nothing, gracefully fallback to
                        // the default, unparsed path
                        input
                    }
                }
                .map { Paths.get(it) }
                .map { path ->
                    if (path.isAbsolute)
                        path
                    else
                        currentProject.workingDirectory.resolve(path)
                }
                .map { FileHandling.normalize(it) }
                .toSet()
        val databaseName = dbname?.let { Paths.get(cleanFileName(it)) }
                ?: baseOptions.databaseName
        val maxLoops = loops?.let {
            if (loops > 0) {
                loops
            } else {
                throw AraraException(
                        LanguageController.messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                )
            }
        } ?: baseOptions.maxLoops

        return org.islandoftex.arara.core.configuration.ExecutionOptions
                .from(baseOptions)
                .copy(
                        maxLoops = maxLoops,
                        verbose = verbose ?: baseOptions.verbose,
                        databaseName = databaseName,
                        fileTypes = filetypes
                                .plus(baseOptions.fileTypes),
                        rulePaths = preprocessedPaths
                                .plus(baseOptions.rulePaths),
                        parseOnlyHeader = header ?: baseOptions.parseOnlyHeader
                )
    }

    /**
     * Convert the relevant properties of the configuration to logging options.
     * Intended to be used together with [toExecutionOptions] and
     * [toUserInterfaceOptions] to destructure and discard this object.
     *
     * @return The corresponding logging options.
     */
    fun toLoggingOptions(
        baseOptions: LoggingOptions = org.islandoftex.arara.core.configuration.LoggingOptions()
    ): LoggingOptions {
        val logName = logname?.let { Paths.get(cleanFileName(it)) }
                ?: baseOptions.logFile
        return org.islandoftex.arara.core.configuration.LoggingOptions(
                enableLogging = logging ?: baseOptions.enableLogging,
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
    fun toUserInterfaceOptions(
        baseOptions: UserInterfaceOptions = org.islandoftex.arara.core.configuration.UserInterfaceOptions()
    ): UserInterfaceOptions {
        return org.islandoftex.arara.core.configuration.UserInterfaceOptions(
                locale = language?.let { Locale.forLanguageTag(it) }
                        ?: baseOptions.locale,
                swingLookAndFeel = laf ?: baseOptions.swingLookAndFeel
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
                        throw AraraException("Configuration should start with !config")
                    Yaml.default.decodeFromString(LocalConfiguration.serializer(),
                            text)
                }.getOrElse {
                    throw AraraException(
                            LanguageController.messages.ERROR_CONFIGURATION_GENERIC_ERROR,
                            it
                    )
                }
    }
}
