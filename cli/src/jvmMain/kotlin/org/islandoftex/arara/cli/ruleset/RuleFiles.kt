// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.RuleLookup
import org.islandoftex.arara.mvel.rules.Rule

/**
 * Locates and parses rule files in directory order, preferring the requested engine within each directory.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object RuleFiles {
    /**
     * Parses the provided file, checks the identifier and returns a rule
     * representation.
     *
     * @param file The rule file.
     * @param identifier The directive identifier.
     * @param scriptOutputToStderr Route Lua script output to diagnostics for structured callers.
     * @return The rule object.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun parseRule(
        file: MPPPath, identifier: String, scriptOutputToStderr: Boolean = false
    ): org.islandoftex.arara.api.rules.Rule {
        return if (file.toString().substringAfterLast('.') == "yaml")
            Rule.parse(file, identifier)
        else if (file.toString().endsWith(".lua"))
            org.islandoftex.arara.lua.rules.LuaRuleParser.parse(file, identifier, scriptOutputToStderr)
        else
            throw AraraException(
                LanguageController.messages.errorParseRuleUnsupportedFormat(identifier, file.toString())
            )
    }

    /**
     * Gets the rule according to the provided directive.
     *
     * @param directive The provided directive.
     * @return The absolute, normalized path of the rule, given the provided
     * directive.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun find(
        directive: Directive,
        executionOptions: org.islandoftex.arara.api.configuration.ExecutionOptions,
        workingDirectory: MPPPath,
        engine: RuleEngine
    ): MPPPath = RuleLookup.resolve(directive.identifier, executionOptions, workingDirectory, engine)
}
