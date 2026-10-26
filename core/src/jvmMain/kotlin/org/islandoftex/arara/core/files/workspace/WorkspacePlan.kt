// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.BuildSelection
import org.islandoftex.arara.api.files.BuildSymlinks
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

@Serializable
internal enum class InputKind { FILE, DIRECTORY, LINK }

internal data class StagedInput(val source: MPPPath, val kind: InputKind, val target: MPPPath? = null)

internal class WorkspacePlan(private val project: Project, private val excluded: List<MPPPath>) {
    private val options = checkNotNull(project.buildDirectory)
    private val root = project.workingDirectory.normalize()
    private val workspace = project.executionDirectory.normalize()
    private val mappings = options.mappings.map {
        root.resolve(it.source).normalize() to destination(workspace, it.destination)
    }
    private val entries = sortedMapOf<String, StagedInput>()
    private val ignores = InputIgnores(root)

    fun mapped(source: MPPPath): MPPPath {
        val mapping = mappings.filter { source.normalize().startsWith(it.first) }
            .maxByOrNull { it.first.toString().length }
            ?: (root to workspace).takeIf { source.normalize().startsWith(root) }
            ?: invalidInput(source)
        val suffix = relative(mapping.first, source.normalize())
        return if (suffix.isEmpty()) mapping.second else mapping.second.resolve(suffix).normalize()
    }

    fun collect(): Map<String, StagedInput> {
        val selections = options.inputs
        if (selections == null) {
            root.listEntries().sortedBy { it.toString() }.forEach { visit(it, mapped(it), true, mutableSetOf()) }
        } else {
            selections.forEach { select(it) }
        }
        mappings.forEach { (source, output) -> visit(source, output, false, mutableSetOf()) }
        project.files.forEach {
            val source = it.sourcePath.normalize()
            if (forbidden(source)) invalidInput(source)
            visit(source, mapped(source), false, mutableSetOf())
        }
        entries.values.filter { it.kind == InputKind.LINK }.forEach { input ->
            val target = checkNotNull(input.target)
            if (target != workspace && relative(workspace, target) !in entries) {
                throw AraraException(messages.errorBuildInputInvalid(input.source.toString()))
            }
        }
        validateLinks(workspace, entries)
        return entries
    }

    private fun select(selection: BuildSelection) {
        val pattern = when (selection) {
            is BuildSelection.Path -> {
                val path = root.resolve(selection.path).normalize()
                if (!path.startsWith(root)) invalidInput(path)
                visit(path, mapped(path), false, mutableSetOf())
                return
            }
            is BuildSelection.Glob -> selectionPattern(selection.pattern)
        }
        val matcher = InputPattern(pattern)
        var matched = false
        fun scan(directory: MPPPath) {
            for (child in directory.listEntries().sortedBy { it.toString() }) {
                if (forbidden(child)) continue
                if (matcher.matches(relative(root, child))) {
                    visit(child, mapped(child), false, mutableSetOf())
                    matched = true
                } else if (!link(child) && child.isDirectory) scan(child)
            }
        }
        scan(root)
        if (!matched) throw AraraException(messages.errorBuildInputMissing(pattern))
    }

    private fun forbidden(source: MPPPath): Boolean =
        source.toJVMPath().any { it == Path.of(".git") || it == Path.of(STATE) } ||
            excluded.any { source.startsWith(it.normalize()) } ||
            (!link(source) && source.isDirectory && present(source.resolve(".arara-workspace")))

    private fun put(path: MPPPath, input: StagedInput) {
        if (path == workspace) return
        val name = relative(workspace, path)
        destination(workspace, name)
        val previous = entries[name]
        if (previous != null && previous != input) {
            throw AraraException(messages.errorBuildInputCollision(path.toString()))
        }
        entries[name] = input
    }

    private fun visit(source: MPPPath, output: MPPPath, filtered: Boolean, ancestors: MutableSet<Path>) {
        if (forbidden(source) || filteredOut(source, filtered)) return
        if (!present(source)) missingInput(source)
        if (link(source)) resolveLink(source)
        if (link(source) && options.symlinks == BuildSymlinks.PRESERVE) {
            preserveLink(source, output, filtered)
        } else if (source.isDirectory) {
            visitDirectory(source, output, filtered, ancestors)
        } else if (source.isRegularFile) {
            if (!filtered || !ignores.excluded(source, false)) put(output, StagedInput(source, InputKind.FILE))
        } else missingInput(source)
    }

    private fun filteredOut(source: MPPPath, filtered: Boolean): Boolean {
        if (!filtered) return false
        val directory = !link(source) && source.isDirectory
        return ignores.excludedByGit(source, directory) || (!directory && ignores.excluded(source, false))
    }

    private fun preserveLink(source: MPPPath, output: MPPPath, filtered: Boolean) {
        val target = resolveLink(source)
        if (target.isDirectory && source.startsWith(target)) {
            cyclicInput(source)
        }
        if (!filtered || !ignores.excluded(source, target.isDirectory)) {
            put(output, StagedInput(source, InputKind.LINK, mapped(target)))
        }
    }

    private fun visitDirectory(source: MPPPath, output: MPPPath, filtered: Boolean, ancestors: MutableSet<Path>) {
        val canonical = source.toJVMPath().toRealPath()
        if (!ancestors.add(canonical)) cyclicInput(source)
        try {
            if (!filtered || !ignores.excluded(source, true)) put(output, StagedInput(source, InputKind.DIRECTORY))
            source.listEntries().sortedBy { it.toString() }.forEach {
                visit(it, output.resolve(it.fileName), filtered, ancestors)
            }
        } finally {
            ancestors.remove(canonical)
        }
    }

    private fun resolveLink(source: MPPPath): MPPPath {
        var target = source
        val seen = mutableSetOf<MPPPath>()
        while (link(target)) {
            if (!seen.add(target)) cyclicInput(source)
            val raw = Files.readSymbolicLink(target.toJVMPath())
            target = target.parent.resolve(raw.toString()).normalize()
            // Every external hop needs an explicit mapping, even in hard-copy mode.
            mapped(target)
        }
        if (!present(target)) missingInput(source)
        // Ancestor links can escape lexical containment as well.
        val real = target.toJVMPath().toRealPath().toMPPPath()
        val allowed = listOf(root) + mappings.map { it.first }
        if (allowed.none { physical(real).startsWith(physical(it)) }) {
            invalidInput(source)
        }
        return target
    }

}

private fun validateLinks(root: MPPPath, entries: Map<String, StagedInput>) {
    val children = mutableMapOf<MPPPath, MutableSet<MPPPath>>()
    for (name in entries.keys) {
        var child = root.resolve(name)
        while (child != root) {
            children.getOrPut(child.parent) { mutableSetOf() }.add(child)
            child = child.parent
        }
    }
    val active = mutableSetOf<MPPPath>()
    val done = mutableSetOf<MPPPath>()
    fun visit(path: MPPPath) {
        if (path in done) return
        if (!active.add(path)) cyclicInput(path)
        val input = entries[relative(root, path)]
        if (input?.kind == InputKind.LINK) visit(checkNotNull(input.target))
        else children[path].orEmpty().forEach { visit(it) }
        active.remove(path)
        done.add(path)
    }
    visit(root)
}
