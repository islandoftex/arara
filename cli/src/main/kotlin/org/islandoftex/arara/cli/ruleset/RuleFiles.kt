// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.lua.rules.LuaRuleParser

/**
 * Implements rule file resolution: locating the rule file for a directive
 * and parsing it into the rule representation, including the one-time
 * deprecation warning of the YAML format.
 *
 * @author Island of TeX
 * @version 8.0
 * @since 8.0
 */
@Suppress("DEPRECATION")
object RuleFiles {
    private val logger = KotlinLogging.logger { }
    private var yamlDeprecationShown = false

    /**
     * Parses the provided file, checks the identifier and returns a rule
     * representation.
     *
     * @param file The rule file.
     * @param identifier The directive identifier.
     * @return The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parseRule(file: MPPPath, identifier: String): Rule =
        when (file.toString().substringAfterLast('.')) {
            RuleFormat.LUA.extension ->
                LuaRuleParser.parse(file, identifier)
            RuleFormat.MVEL.extension -> {
                warnAboutYamlDeprecation()
                org.islandoftex.arara.mvel.rules.Rule.parse(file, identifier)
            }
            else ->
                throw AraraException(
                    LanguageController.messages
                        .errorParseRuleUnsupportedFormat(
                            identifier,
                            file.normalize().parent.toString(),
                            file.fileName
                        )
                )
        }

    /**
     * Constructs the path given the current path and the rule name.
     *
     * @param path The current path.
     * @param name The rule name.
     * @param format The rule format determining the file extension.
     * @param workingDirectory The working directory to resolve relative
     *   rule paths against.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun construct(
        path: MPPPath,
        name: String,
        format: RuleFormat,
        workingDirectory: MPPPath
    ): MPPPath = "$name.${format.extension}".let { fileName ->
        if (path.isAbsolute) {
            path / fileName
        } else {
            // when retrieving rules the current project is never null
            // because the executor always acts on a project file;
            // first resolve the path (rule path) against the working
            // directory, then the rule name we want to resolve
            workingDirectory / path / fileName
        }
    }.normalize()

    private fun warnAboutYamlDeprecation() {
        if (!yamlDeprecationShown) {
            yamlDeprecationShown = true
            logger.warn {
                "YAML rules are deprecated and will be removed in a " +
                    "future version of arara. Please migrate your rules " +
                    "to the Lua format (see the --engine option)."
            }
        }
    }
}
