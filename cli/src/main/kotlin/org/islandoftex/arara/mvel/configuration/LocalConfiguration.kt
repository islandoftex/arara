// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.configuration

import java.nio.file.Paths
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.api.session.LoggingOptions
import org.islandoftex.arara.api.session.UserInterfaceOptions
import org.islandoftex.arara.cli.configuration.AraraSpec
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
class LocalConfiguration {
    // rule paths
    var paths: List<String> = listOf()
        get() {
            val user = mapOf(
                    "home" to (CommonUtils.getSystemPropertyOrNull("user.home")
                            ?: ""),
                    "name" to (CommonUtils.getSystemPropertyOrNull("user.name")
                            ?: ""))
            val application = mapOf(
                    "workingDirectory" to Arara.config[AraraSpec.Execution.workingDirectory].toAbsolutePath().toString()
            )

            return field.map { it.trim() }.map { input ->
                try {
                    TemplateRuntime.eval(input, mapOf(
                            "user" to user, "application" to application
                    )) as String
                } catch (_: RuntimeException) {
                    // do nothing, gracefully fallback to
                    // the default, unparsed path
                    input
                }
            }
        }

    // file types
    var filetypes: List<FileTypeImpl> = listOf()

    // the application language
    // default to English
    private var language: String = Arara.config[AraraSpec.Application.defaultLanguageCode]

    // maximum number of loops
    private var loops: Int = Arara.config[AraraSpec.Execution.maxLoops]

    // verbose flag
    @SerialName("verbose")
    private var isVerbose: Boolean = Arara.config[AraraSpec.Execution.verbose]

    // logging flag
    @SerialName("logging")
    private var isLogging: Boolean = Arara.config[AraraSpec.Execution.logging]

    // header flag
    @SerialName("header")
    private var isHeader: Boolean = Arara.config[AraraSpec.Execution.onlyHeader]

    // database name
    private var dbname: String = Arara.config[AraraSpec.Execution.databaseName].toString()

    // log name
    private var logname: String = Arara.config[AraraSpec.Execution.logName]

    // map of preambles
    var preambles: Map<String, String> = Arara.config[AraraSpec.Execution.preambles]

    // look and feel
    // default to none
    private var laf: String = Arara.config[AraraSpec.UserInteraction.lookAndFeel]

    /**
     * Convert the relevant properties of the configuration to execution
     * options. Intended to be used together with [toLoggingOptions] and
     * [toUserInterfaceOptions] to destructure and discard this object.
     *
     * @return The corresponding execution options.
     */
    fun toExecutionOptions(): ExecutionOptions {
        return org.islandoftex.arara.core.session.ExecutionOptions(
                maxLoops = loops,
                databaseName = Paths.get(dbname),
                parseOnlyHeader = isHeader,
                verbose = isVerbose
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
        return org.islandoftex.arara.core.session.LoggingOptions(
                enableLogging = isLogging,
                logFile = Paths.get(logname)
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
