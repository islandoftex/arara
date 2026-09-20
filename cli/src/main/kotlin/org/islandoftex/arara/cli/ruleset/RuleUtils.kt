// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.lua.rules.LuaRuleParser

/**
 * Implements rule utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Suppress("DEPRECATION")
object RuleUtils {
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
                TODO("Kotlin DSL not implemented yet")
        }

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
