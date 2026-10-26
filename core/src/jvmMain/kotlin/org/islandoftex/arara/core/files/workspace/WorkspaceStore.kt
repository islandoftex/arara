// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import com.charleskorn.kaml.Yaml
import java.nio.file.Files
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.files.Database
import org.islandoftex.arara.core.files.FileHandling

@Serializable
internal data class WorkspaceState(val owner: String, val inputs: Map<String, InputKind> = emptyMap())

internal class WorkspaceStore(project: Project) {
    private val root = project.executionDirectory.normalize()
    private val stateDirectory = root.resolve(STATE)
    private val manifest = stateDirectory.resolve("inputs.yaml")
    private val hashes = stateDirectory.resolve("hashes.yaml")
    private val owner = project.workingDirectory.normalize().toString() + "\n" + project.name

    fun owned(): WorkspaceState {
        if (link(root) || link(stateDirectory)) unmanaged()
        if (link(manifest) || !manifest.isRegularFile) unmanaged()
        val state = try {
            Yaml.default.decodeFromString(WorkspaceState.serializer(), manifest.readText())
        } catch (_: SerializationException) {
            unmanaged()
        }
        if (state.owner != owner) unmanaged()
        state.inputs.keys.forEach { destination(root, it) }
        return state
    }

    private fun unmanaged(): Nothing = throw AraraException(messages.errorBuildWorkspaceUnmanaged(root.toString()))

    fun sync(inputs: Map<String, StagedInput>, copiedFile: () -> Unit) {
        val previous = if (present(root)) {
            if (root.isDirectory && !link(root) && root.listEntries().isEmpty()) WorkspaceState(owner)
            else owned()
        } else WorkspaceState(owner)
        checkPlan(inputs, previous)
        root.createDirectories()
        stateDirectory.createDirectories()
        // Claim an empty directory before staging, so interrupted initial copies remain cleanable.
        if (!manifest.exists) save(previous)
        if (link(hashes)) unmanaged()
        val database = Database.load(hashes)
        removeStale(previous, inputs, database)
        val completed = previous.inputs.filterKeys { it in inputs }.toMutableMap()
        try {
            for ((name, input) in inputs) {
                stageInput(name, input, database, copiedFile)
                completed[name] = input.kind
            }
        } finally {
            // Persist successful copies together, including when a later copy fails.
            writeAtomic(hashes) { database.save(it) }
            save(WorkspaceState(owner, completed.toSortedMap()))
        }
    }

    private fun removeStale(
        previous: WorkspaceState, inputs: Map<String, StagedInput>, database: org.islandoftex.arara.api.files.Database
    ) {
        for ((name, kind) in previous.inputs.entries.sortedByDescending { it.key.length }) {
            if (inputs[name]?.kind != kind) {
                val path = destination(root, name)
                noLinkedParents(root, path)
                removeStaged(path, kind)
                if (path in database) database.remove(path)
            }
        }
    }

    private fun stageInput(
        name: String, input: StagedInput, database: org.islandoftex.arara.api.files.Database, copiedFile: () -> Unit
    ) {
        val path = destination(root, name)
        noLinkedParents(root, path)
        path.parent.createDirectories()
        when (input.kind) {
            InputKind.DIRECTORY -> path.createDirectories()
            InputKind.LINK -> {
                val relativeTarget = path.parent.toJVMPath().relativize(checkNotNull(input.target).toJVMPath())
                if (!link(path) || Files.readSymbolicLink(path.toJVMPath()) != relativeTarget) {
                    Files.deleteIfExists(path.toJVMPath())
                    Files.createSymbolicLink(path.toJVMPath(), relativeTarget)
                }
            }
            InputKind.FILE -> {
                val hash = FileHandling.calculateHash(input.source)
                if (!present(path) || database[path] != hash) {
                    val temporary = Files.createTempFile(path.parent.toJVMPath(), ".arara-input-", ".tmp")
                    try {
                        Files.copy(input.source.toJVMPath(), temporary, REPLACE_EXISTING, COPY_ATTRIBUTES)
                        replaceAtomic(temporary.toMPPPath(), path)
                        copiedFile()
                    } finally {
                        Files.deleteIfExists(temporary)
                    }
                }
                database[path] = hash
            }
        }
    }

    private fun checkPlan(inputs: Map<String, StagedInput>, previous: WorkspaceState) {
        for ((name, input) in inputs) {
            val path = destination(root, name)
            val replacedParent = checkParents(path, inputs, previous)
            if (!replacedParent) checkExisting(path, input, previous.inputs[name])
        }
    }

    private fun collision(path: MPPPath): Nothing =
        throw AraraException(messages.errorBuildInputCollision(path.toString()))

    private fun checkExisting(path: MPPPath, input: StagedInput, previous: InputKind?) {
        if (link(path) && input.kind != InputKind.LINK && previous != InputKind.LINK) collision(path)
        val sharedDirectory = input.kind == InputKind.DIRECTORY && path.isDirectory && !link(path)
        if (present(path) && previous == null && !sharedDirectory) collision(path)
    }

    private fun checkParents(
        path: MPPPath, inputs: Map<String, StagedInput>, previous: WorkspaceState
    ): Boolean {
        var replacingLink = false
        var parent = path.parent
        while (parent != root) {
            val name = relative(root, parent)
            val replacedLink = previous.inputs[name] == InputKind.LINK && inputs[name]?.kind == InputKind.DIRECTORY
            if (replacedLink) replacingLink = true
            if (link(parent) && !replacedLink) {
                throw AraraException(messages.errorBuildInputInvalid(parent.toString()))
            }
            if (inputs[name]?.kind in setOf(InputKind.LINK, InputKind.FILE)) collision(path)
            parent = parent.parent
        }
        return replacingLink
    }

    private fun save(state: WorkspaceState) = writeAtomic(manifest) {
        it.writeText(Yaml.default.encodeToString(WorkspaceState.serializer(), state))
    }

}
