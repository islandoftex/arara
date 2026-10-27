// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.watch

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.core.files.Database
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.files.workspace.InputIgnores
import org.islandoftex.arara.core.files.workspace.InputPattern
import org.islandoftex.arara.core.files.workspace.selectionPattern

/** Content snapshot independent of the persistent database used by rule conditionals. */
class WatchSnapshot internal constructor(
    private val database: Database,
    private val identities: Map<MPPPath, String>,
    /** Existing directories eligible for native notification registration. */
    val directories: Set<MPPPath>,
    /** Broken links and inaccessible inputs, reported without terminating the session. */
    val problems: Set<String>
) {
    /** Additions, removals, link replacements and content changes since [previous]. */
    fun changesSince(previous: WatchSnapshot): Set<MPPPath> =
        (identities.keys + previous.identities.keys).filterTo(linkedSetOf()) {
            identities[it] != previous.identities[it] || database[it] != previous.database[it]
        }
}

/** Collects source inputs, following links with ancestor-based cycle detection. */
class WatchInputs(
    private val projects: List<Project>,
    private val excluded: Set<MPPPath> = emptySet()
) {
    /** Snapshot every project's inputs; shared sources remain attributed to every referencing project. */
    fun capture(): Map<String, WatchSnapshot> = projects.associate { it.name to capture(it) }

    private fun capture(project: Project): WatchSnapshot = InputCollector(project,
        excluded.map { it.normalize() } + projects.filter { it.buildDirectory != null }
            .map { it.executionDirectory.normalize() }).capture()
}

private class InputCollector(private val project: Project, private val forbidden: List<MPPPath>) {
    private val root = project.workingDirectory.normalize()
    private data class Scope(val root: MPPPath, val ignores: InputIgnores = InputIgnores(root))
    private val scope = Scope(root)
    private val include = project.watch.include.map { InputPattern(selectionPattern(it)) }
    private val exclude = project.watch.exclude.map { InputPattern(selectionPattern(it)) }
    private val targets = project.files.map { it.sourcePath.normalize() }.toSet()
    private val physicalExclusions = forbidden.map { path ->
        val nio = path.toJVMPath()
        if (Files.exists(nio)) nio.toRealPath() else nio
    }
    private val hashes = Database()
    private val identities = linkedMapOf<MPPPath, String>()
    private val directories = linkedSetOf<MPPPath>()
    private val problems = linkedSetOf<String>()

    fun capture(): WatchSnapshot {
        visit(root, false, mutableSetOf(), scope)
        (targets + project.watch.paths.map { root.resolve(it).normalize() } +
            project.buildDirectory?.mappings.orEmpty().map { root.resolve(it.source).normalize() }).forEach {
            val base = if (it.startsWith(root)) scope else Scope(if (it.isDirectory) it else it.parent)
            visit(it, true, mutableSetOf(), base)
        }
        return WatchSnapshot(hashes, identities, directories, problems)
    }

    private fun relative(path: MPPPath, scope: Scope): String =
        scope.root.toJVMPath().relativize(path.toJVMPath()).toString().replace('\\', '/')

    private fun forbidden(path: MPPPath): Boolean = forbidden.any { path == it || path.startsWith(it) } ||
        path.toJVMPath().any { it.toString() in setOf(".git", ".arara-workspace") }

    private fun visit(path: MPPPath, explicit: Boolean, ancestors: MutableSet<Path>, scope: Scope) {
        if (forbidden(path) || (path !in targets && exclude.any { it.matches(relative(path, scope)) })) return
        val forced = explicit || path in targets || include.any { it.matches(relative(path, scope)) }
        try {
            inspect(path, forced, ancestors, scope)
        } catch (_: IOException) {
            problems += path.toString()
        } catch (_: AraraException) {
            problems += path.toString()
        }
    }

    private fun inspect(path: MPPPath, forced: Boolean, ancestors: MutableSet<Path>, scope: Scope) {
        val symbolic = Files.isSymbolicLink(path.toJVMPath())
        if (symbolic && path.exists && forbiddenPhysical(path.toJVMPath().toRealPath())) return
        if (symbolic) identities[path] = "link:" + Files.readSymbolicLink(path.toJVMPath())
        when {
            !path.exists -> if (symbolic) problems.add(path.toString())
            path.isDirectory -> visitDirectory(path, forced, ancestors, scope)
            path.isRegularFile && selected(path, forced, scope) -> {
                identities.putIfAbsent(path, "file")
                hashes[path] = FileHandling.calculateHash(path)
            }
            symbolic && !forced -> identities.remove(path)
        }
    }

    private fun selected(path: MPPPath, forced: Boolean, scope: Scope): Boolean =
        forced || (!generated(path.fileName) && !scope.ignores.excluded(path, false))

    private fun visitDirectory(path: MPPPath, forced: Boolean, ancestors: MutableSet<Path>, scope: Scope) {
        val ignored = !forced && include.isEmpty() && path != scope.root && scope.ignores.excluded(path, true)
        val canonical = path.toJVMPath().toRealPath()
        if (ignored || forbiddenPhysical(canonical) || path.resolve(".arara-workspace").exists) {
            identities.remove(path)
            return
        }
        if (ancestors.add(canonical)) {
            directories += path
            try {
                path.listEntries().sortedBy { it.toString() }.forEach { visit(it, false, ancestors, scope) }
            } finally {
                ancestors.remove(canonical)
            }
        }
    }

    private fun forbiddenPhysical(path: Path): Boolean =
        physicalExclusions.any { path.startsWith(it) } ||
            path.any { it.toString() in setOf(".git", ".arara-workspace") }

    private fun generated(name: String): Boolean = name.startsWith(".arara-input-") ||
        name.endsWith("~") || name.endsWith(".swp") || name.endsWith(".synctex.gz") || name.endsWith(".run.xml") ||
        name.substringAfterLast('.', "").lowercase() in GENERATED

    private companion object {
        val GENERATED = setOf("aux", "log", "toc", "lof", "lot", "out", "bbl", "blg", "bcf", "idx", "ind",
            "ilg", "glg", "glo", "gls", "acn", "acr", "alg", "nav", "snm", "vrb", "fls", "fdb_latexmk",
            "pdf", "dvi", "xdv", "synctex")
    }
}
