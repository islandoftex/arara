// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for rule validation messages. */
public interface RuleValidationMessages {
    /** Unsupported rule format; arguments: file path and supplied format. */
    public fun errorParseRuleUnsupportedFormat(identifier: String, format: String): String

    /**
     * The error shown when a rule cannot be parsed.
     */
    public val errorParseRuleGenericError: String

    /**
     * The header prepended to errors raised by a rule, naming the rule
     * and its location.
     */
    public fun errorRuleIdentifierAndPath(identifier: String, location: String): String

    /**
     * The error shown when a rule argument uses a reserved identifier.
     */
    public fun errorValidateBodyArgumentIdIsReserved(identifier: String): String

    /**
     * The error shown when a rule declares duplicate argument
     * identifiers.
     */
    public val errorValidateBodyDuplicateArgumentIdentifiers: String

    /**
     * The error shown when a rule argument has neither a flag nor a
     * default.
     */
    public val errorValidateBodyMissingKeys: String

    /**
     * The error shown when a rule argument has no identifier.
     */
    public val errorValidateBodyNullArgumentId: String

    /**
     * The error shown when a rule command entry is null.
     */
    public val errorValidateBodyNullCommand: String

    /**
     * The error shown when a rule has no name.
     */
    public val errorValidateHeaderNullName: String

    /**
     * The error shown when a rule identifier does not match the expected
     * one.
     */
    public fun errorValidateHeaderWrongIdentifier(expected: String, actual: String): String
}
