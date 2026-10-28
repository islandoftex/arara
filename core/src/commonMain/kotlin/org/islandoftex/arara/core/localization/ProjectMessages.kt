// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for project messages. */
interface ProjectMessages {
    /** Project directive extraction failed; arguments: input path and parser details. */
    fun errorProjectFileDirectivesExtraction(file: String, details: String): String

    /** Project script failure; arguments: script path and interpreter detail. */
    fun errorProjectInvalidLua(path: String, detail: String): String

    /** Duplicate project names; argument: conflicting names. */
    fun errorProjectDuplicateNames(names: String): String

    /** Unknown project dependency; arguments: project and dependency names. */
    fun errorProjectUnknownDependency(project: String, dependency: String): String

    /**
     * The error shown when the project dependencies contain a cycle.
     */
    fun errorProjectCycle(cycle: String): String

    /** Shared project input; arguments: file path and both project names. */
    fun errorProjectFileOwnership(path: String, owner: String, project: String): String

    /** Unrecognized project input type; argument: file path. */
    fun errorProjectFiletype(path: String): String

    /**
     * The error shown when a file is neither a project specification nor
     * a list of them.
     */
    val errorInvalidProjectFile: String
}
