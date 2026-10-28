// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.rules.RuleFormat
import org.islandoftex.arara.core.localization.LanguageController

/** Shared rule selection without parsing or running any rule script. */
object RuleLookup {
    /** Resolve the first existing candidate, preserving directory, format and filename priority. */
    fun resolve(
        identifier: String,
        executionOptions: org.islandoftex.arara.api.configuration.ExecutionOptions,
        workingDirectory: MPPPath,
        engine: RuleEngine
    ): MPPPath {
        val paths = executionOptions.rulePaths
        return candidates(identifier, paths, workingDirectory, engine).firstOrNull { it.exists }
            ?: throw AraraException(
                LanguageController.messages.errorInterpreterRuleNotFound(
                    identifier,
                    "$identifier.lua / $identifier.yaml",
                    paths.joinToString("; ", "(", ")") { it.normalize().toString() }
                )
            )
    }

    /** Enumerate normalized candidates in the exact order used by execution and discovery. */
    fun candidates(
        identifier: String,
        paths: Set<MPPPath>,
        workingDirectory: MPPPath,
        engine: RuleEngine
    ): List<MPPPath> = paths.flatMap { path ->
        val formats = if (engine == RuleEngine.LUA) listOf(RuleFormat.LUA, RuleFormat.MVEL)
            else listOf(RuleFormat.MVEL, RuleFormat.LUA)
        formats.flatMap { format ->
            listOf(
                construct(path, identifier, format, workingDirectory),
                construct(path, "arara-rule-$identifier", format, workingDirectory)
            )
        }
    }.distinct()

    /**
     * Constructs the path given the current path and the rule name.
     *
     * @param path The current path.
     * @param name The rule name.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun construct(
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
}
