// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.io.File
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.mvel.rules.RuleImpl

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
    fun parseRule(file: File, identifier: String): Rule {
        return if (file.extension == "yaml")
            RuleImpl.parse(file, identifier)
        else
            TODO("Kotlin DSL not implemented yet")
    }
}
