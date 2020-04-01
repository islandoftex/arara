// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.islandoftex.arara.Arara
import org.islandoftex.arara.model.FileTypeImpl
import org.islandoftex.arara.utils.CommonUtils
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

            return field.map { input ->
                var path = CommonUtils.removeKeywordNotNull(input)
                try {
                    path = TemplateRuntime.eval(path, mapOf(
                            "user" to user, "application" to application
                    )) as String
                } catch (_: RuntimeException) {
                    // do nothing, gracefully fallback to
                    // the default, unparsed path
                }
                path
            }
        }

    // file types
    var filetypes: List<FileTypeImpl> = listOf()

    // the application language
    // default to English
    var language: String = Arara.config[AraraSpec.Application.defaultLanguageCode]
        get() = CommonUtils.removeKeywordNotNull(field)

    // maximum number of loops
    var loops: Int = Arara.config[AraraSpec.Execution.maxLoops]

    // verbose flag
    @SerialName("verbose")
    var isVerbose: Boolean = Arara.config[AraraSpec.Execution.verbose]

    // logging flag
    @SerialName("logging")
    var isLogging: Boolean = Arara.config[AraraSpec.Execution.logging]

    // header flag
    @SerialName("header")
    var isHeader: Boolean = Arara.config[AraraSpec.Execution.onlyHeader]

    // database name
    var dbname: String = Arara.config[AraraSpec.Execution.databaseName]
        get() = CommonUtils.removeKeywordNotNull(field)

    // log name
    var logname: String = Arara.config[AraraSpec.Execution.logName]
        get() = CommonUtils.removeKeywordNotNull(field)

    // map of preambles
    var preambles: Map<String, String> = Arara.config[AraraSpec.Execution.preambles]

    // look and feel
    // default to none
    var laf: String = Arara.config[AraraSpec.UserInteraction.lookAndFeel]
        get() = CommonUtils.removeKeywordNotNull(field)
}
