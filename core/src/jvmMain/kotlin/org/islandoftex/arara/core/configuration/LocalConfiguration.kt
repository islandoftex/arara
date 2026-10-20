// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.utils.formatString

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
    private val paths: List<String> = emptyList(),
    private val filetypes: List<SerialFileType> = emptyList(),
    private val language: String? = null,
    private val console: String? = null,
    private val loops: Int? = null,
    private val verbose: Boolean? = null,
    private val logging: Boolean? = null,
    private val header: Boolean? = null,
    private val dbname: String? = null,
    private val logname: String? = null,
    val preambles: Map<String, String> = mapOf(),
    val defaultPreamble: String? = null,
    val prependPreambleIfDirectivesGiven: Boolean = true,
    @Transient private val expandPathTokens: Boolean = true
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
        val preprocessedPaths = paths.asSequence()
            .map { it.trim() }
            .map { input ->
                if (!expandPathTokens) input else input.replace(
                    "@{user.home}",
                    Environment.getSystemPropertyOrNull("user.home") ?: ""
                )
                    .replace(
                        "@{user.name}",
                        Environment.getSystemPropertyOrNull("user.name") ?: ""
                    )
                    .replace(
                        "@{application.workingDirectory}",
                        currentProject.workingDirectory.normalize().toString()
                    )
            }
            .map { MPPPath(it) }
            .map { path ->
                if (path.isAbsolute)
                    path
                else
                    currentProject.workingDirectory / path
            }
            .map { it.normalize() }
            .toSet()
        val databaseName = dbname?.let { name ->
            if (name.isBlank()) throw AraraException(
                LanguageController.messages.ERROR_SCRIPT_NONBLANK.formatString("dbname/database")
            )
            runCatching { currentProject.workingDirectory.resolve(name.trim()).normalize() }.getOrElse {
                throw AraraException(LanguageController.messages.ERROR_CONFIGURATION_GENERIC_ERROR,
                    it as? Exception ?: RuntimeException(it))
            }
        } ?: baseOptions.databaseName
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
                fileTypes = filetypes.map { it.toFileType() }
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
        val logName = logname?.let { MPPPath(it) }
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
            locale = language?.let { MPPLocale(it) }
                ?: baseOptions.locale,
            terminalOutputWidth = baseOptions.terminalOutputWidth,
            consoleMode = console ?: baseOptions.consoleMode
        )
    }

}
