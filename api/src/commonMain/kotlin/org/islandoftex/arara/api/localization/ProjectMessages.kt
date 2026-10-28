// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for project messages. */
public interface ProjectMessages {
    /** Project directive extraction failed; arguments: input path and parser details. */
    public fun errorProjectFileDirectivesExtraction(file: String, details: String): String

    /** Project script failure; arguments: script path and interpreter detail. */
    public fun errorProjectInvalidLua(path: String, detail: String): String

    /** Duplicate project names; argument: conflicting names. */
    public fun errorProjectDuplicateNames(names: String): String

    /** Unknown project dependency; arguments: project and dependency names. */
    public fun errorProjectUnknownDependency(project: String, dependency: String): String

    /**
     * The error shown when the project dependencies contain a cycle.
     */
    public fun errorProjectCycle(cycle: String): String

    /** Shared project input; arguments: file path and both project names. */
    public fun errorProjectFileOwnership(path: String, owner: String, project: String): String

    /** Unrecognized project input type; argument: file path. */
    public fun errorProjectFiletype(path: String): String

    /**
     * The error shown when a file is neither a project specification nor
     * a list of them.
     */
    public val errorInvalidProjectFile: String
}
