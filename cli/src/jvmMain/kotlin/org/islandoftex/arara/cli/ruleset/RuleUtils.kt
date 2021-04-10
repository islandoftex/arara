// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.dsl.language.DSLInstance
import org.islandoftex.arara.dsl.scripting.AraraScriptCompilationConfiguration
import org.islandoftex.arara.dsl.scripting.AraraScriptEvaluationConfiguration
import org.slf4j.LoggerFactory

/**
 * Implements rule utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object RuleUtils {
    private val logger = LoggerFactory.getLogger(RuleUtils::class.java)

    /**
     * Cache rules which have been read and parsed.
     */
    private val ruleCache = mutableMapOf<String, Rule>()

    /**
     * Ensure that rules have been cached from the correct path.
     */
    private val ruleFileCache = mutableMapOf<String, MPPPath>()

    /**
     * Resolve a rule from the cache.
     */
    fun getRule(identifier: String) = ruleCache[identifier]

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
    fun parseYAMLRule(file: MPPPath, identifier: String): Rule {
        return ruleCache[identifier]?.takeIf { ruleFileCache[identifier] == file }
                ?: if (file.toString().substringAfterLast('.') == "yaml") {
                    org.islandoftex.arara.mvel.rules.Rule.parse(file, identifier)
                            .also {
                                logger.debug("Caching YAML rule $identifier")
                                ruleCache[identifier] = it
                            }
                } else {
                    throw AraraException("Retrieving a YAML DSL rule is only " +
                            "supported from YAML files.")
                }
    }

    /**
     * Parse a Kotlin DSL [file] and cache all rules contained within.
     *
     * @return The parsed rules from the [file].
     */
    fun parseKotlinDSLRuleFile(file: MPPPath): List<Rule> {
        logger.debug("Caching Kotlin DSL rules from $file")
        return BasicJvmScriptingHost().eval(
                file.readText().toScriptSource(),
                AraraScriptCompilationConfiguration(),
                AraraScriptEvaluationConfiguration()
        ).valueOrThrow().run {
            DSLInstance.rules.onEach {
                ruleCache[it.identifier] = it
            }
        }
    }
}
