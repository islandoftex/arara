// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import com.charleskorn.kaml.Yaml
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

/**
 * A local configuration which resembles configuration files in the working
 * directory.
 *
 * @author Island of TeX
 */
@Serializable
data class LocalConfiguration(
    /**
     * The directories rules are looked up in, as the
     * configuration names them.
     */
    private val paths: List<String> = emptyList(),
    /**
     * The file types the configuration declares, mapped from
     * extension to pattern.
     */
    private val filetypes: List<SerialFileType> = emptyList(),
    /**
     * The language tag the configuration asks the console to
     * speak, if any.
     */
    private val language: String? = null,
    /**
     * The maximum number of loops a looping directive may take,
     * if the configuration caps them.
     */
    private val loops: Int? = null,
    /**
     * Whether the tool output echoes live, if the configuration
     * says so.
     */
    private val verbose: Boolean? = null,
    /**
     * Whether the run writes its log file, if the configuration
     * says so.
     */
    private val logging: Boolean? = null,
    /**
     * Whether the directive extraction reads the header alone
     * (the default) or, when false, the whole file, if the
     * configuration says so.
     */
    private val header: Boolean? = null,
    /**
     * The name of the file database, if the configuration
     * renames it.
     */
    private val dbname: String? = null,
    /**
     * The name of the log file, if the configuration renames it.
     */
    private val logname: String? = null,
    /**
     * The console presentation the configuration selects, by name; the
     * command line wins over this value, which wins over the plain
     * default.
     */
    val console: String? = null,

    /**
     * The key that silences the live feedback for those who
     * prefer stillness. The breath is on by default.
     */
    val animations: Boolean? = null,
    /**
     * The named directive lists the configuration provides, mapped from
     * preamble name to its directives.
     */
    val preambles: Map<String, String> = mapOf(),
    /**
     * The preamble applied when no other is requested.
     */
    val defaultPreamble: String? = null,
    /**
     * Whether the default preamble is applied to all files, the
     * historical behaviour, or only to the files that carry no
     * directives of their own.
     */
    val prependPreambleIfDirectivesGiven: Boolean = true,
    /**
     * Whether the paths are expanded for engine tokens; a
     * script-based configuration computes its paths with the
     * values in scope to the script, so no tokens remain to
     * expand. The flag stays out of the serialized format on
     * purpose.
     */
    @Transient
    private val expandPathTokens: Boolean = true
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
                if (expandPathTokens) {
                    input.replace(
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
                } else {
                    // the script configuration already computed its
                    // paths; there are no tokens to expand
                    input
                }
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
        // the database name honors the configured path the same way
        // the log file always has: the value names the file, and a
        // relative path resolves against the working directory when
        // the database is opened - never silently discarding the
        // user's intent
        val databaseName = dbname?.let { MPPPath(it.trim()) }
            ?: baseOptions.databaseName
        val maxLoops = loops?.let {
            if (loops > 0) {
                loops
            } else {
                throw AraraException(
                    LanguageController.messages.errorConfigurationLoopsInvalidRange
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
            logFile = logName,
            // the append mode belongs to the caller's standing
            // choice: the configuration never speaks for it, so
            // the base carries it instead of silently resetting
            appendLog = baseOptions.appendLog
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
            enableAnimations = animations
                ?: baseOptions.enableAnimations,
            // the width belongs to the caller's standing choice: the
            // configuration never speaks for it, so the base
            // carries it instead of silently resetting
            terminalOutputWidth = baseOptions.terminalOutputWidth
        )
    }

    /**
     * Holds the YAML loader of the configuration format.
     */
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
        fun load(file: MPPPath): LocalConfiguration =
            file.runCatching {
                val text = file.readText()
                if (!text.startsWith("!config"))
                    throw AraraException("Configuration should start with !config")
                Yaml.default.decodeFromString(serializer(), text)
            }.getOrElse {
                throw AraraException(
                    LanguageController.messages.errorConfigurationGenericError,
                    it
                )
            }
    }
}
