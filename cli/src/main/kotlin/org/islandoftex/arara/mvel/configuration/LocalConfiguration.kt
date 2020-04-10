// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import java.nio.file.Paths
import kotlinx.serialization.Serializable
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.api.session.LoggingOptions
import org.islandoftex.arara.api.session.UserInterfaceOptions
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.configuration.ConfigurationUtils
import org.islandoftex.arara.cli.localization.LanguageController
import org.islandoftex.arara.cli.localization.Messages
import org.islandoftex.arara.cli.model.FileTypeImpl
import org.islandoftex.arara.cli.utils.CommonUtils
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
    private var paths: List<String> = listOf(),
    private var filetypes: List<FileTypeImpl> = listOf(),
    private var language: String = Arara.config[AraraSpec.userInterfaceOptions].languageCode,
    private var loops: Int = Arara.config[AraraSpec.executionOptions].maxLoops,
    private var verbose: Boolean = Arara.config[AraraSpec.executionOptions].verbose,
    private var logging: Boolean = Arara.config[AraraSpec.loggingOptions].enableLogging,
    private var header: Boolean = Arara.config[AraraSpec.executionOptions].parseOnlyHeader,
    private var dbname: String = Arara.config[AraraSpec.executionOptions].databaseName.toString(),
    private var logname: String = Arara.config[AraraSpec.loggingOptions].logFile.toString(),
    private var laf: String = Arara.config[AraraSpec.userInterfaceOptions].swingLookAndFeel,
    var preambles: Map<String, String> = Arara.config[AraraSpec.Execution.preambles]
) {
    /**
     * Convert the relevant properties of the configuration to execution
     * options. Intended to be used together with [toLoggingOptions] and
     * [toUserInterfaceOptions] to destructure and discard this object.
     *
     * @return The corresponding execution options.
     */
    @Throws(AraraException::class)
    fun toExecutionOptions(): ExecutionOptions {
        val preprocessedPaths = paths.map { it.trim() }.map { input ->
            try {
                TemplateRuntime.eval(input, mapOf(
                        "user" to mapOf(
                                "home" to (CommonUtils.getSystemPropertyOrNull("user.home")
                                        ?: ""),
                                "name" to (CommonUtils.getSystemPropertyOrNull("user.name")
                                        ?: "")
                        ),
                        "application" to mapOf(
                                "workingDirectory" to Arara.config[AraraSpec.Execution.workingDirectory].toAbsolutePath().toString()
                        )
                )) as String
            } catch (_: RuntimeException) {
                // do nothing, gracefully fallback to
                // the default, unparsed path
                input
            }
        }
        val databaseName = Paths.get(ConfigurationUtils.cleanFileName(dbname))
        val maxLoops = if (loops > 0) {
            loops
        } else {
            throw AraraException(
                    LanguageController.getMessage(
                            Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                    )
            )
        }
        return org.islandoftex.arara.core.session.ExecutionOptions(
                maxLoops = maxLoops,
                verbose = verbose,
                databaseName = databaseName,
                fileTypes = filetypes
                        .plus(Arara.config[AraraSpec.Execution.fileTypes]),
                rulePaths = preprocessedPaths.map { Paths.get(it) }
                        .plus(Arara.config[AraraSpec.Execution.rulePaths])
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
    fun toLoggingOptions(): LoggingOptions {
        val logName = Paths.get(ConfigurationUtils.cleanFileName(logname))
        return org.islandoftex.arara.core.session.LoggingOptions(
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
    fun toUserInterfaceOptions(): UserInterfaceOptions {
        return org.islandoftex.arara.core.session.UserInterfaceOptions(
                languageCode = language,
                swingLookAndFeel = laf
        )
    }
}
