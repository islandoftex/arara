// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for rule validation messages. */
interface RuleValidationMessages {
    /** Unsupported rule format; arguments: file path and supplied format. */
    fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String

    /**
     * The error shown when a rule cannot be parsed.
     */
    val errorParseRuleGenericError: String

    /**
     * The header prepended to errors raised by a rule, naming the rule
     * and its location.
     */
    fun errorRuleIdentifierAndPath(identifier: String, location: String): String

    /**
     * The error shown when a rule argument uses a reserved identifier.
     */
    fun errorValidateBodyArgumentIdIsReserved(identifier: String): String

    /**
     * The error shown when a rule declares duplicate argument
     * identifiers.
     */
    val errorValidateBodyDuplicateArgumentIdentifiers: String

    /**
     * The error shown when a rule argument has neither a flag nor a
     * default.
     */
    val errorValidateBodyMissingKeys: String

    /**
     * The error shown when a rule argument has no identifier.
     */
    val errorValidateBodyNullArgumentId: String

    /**
     * The error shown when a rule command entry is null.
     */
    val errorValidateBodyNullCommand: String

    /**
     * The error shown when a rule has no name.
     */
    val errorValidateHeaderNullName: String

    /**
     * The error shown when a rule identifier does not match the expected
     * one.
     */
    fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String
}
