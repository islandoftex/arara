// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.mvel.rules.Rule

/**
 * Implements rule utilitary methods.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
object RuleUtils {
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
    fun parseRule(file: Path, identifier: String): org.islandoftex.arara.api.rules.Rule {
        return if (file.toString().substringAfterLast('.') == "yaml")
            Rule.parse(file, identifier)
        else
            TODO("Kotlin DSL not implemented yet")
    }
}
