// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.cli.configuration.ConfigurationSnapshot
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.watch.WatchInputs
import org.islandoftex.arara.core.watch.WatchSnapshot

internal data class WatchGraph(
    val projects: List<Project>,
    val controlFiles: Set<MPPPath>,
    val ruleDirectories: Set<MPPPath> = emptySet(),
    val excluded: Set<MPPPath> = emptySet(),
    val configuration: Map<String, WatchProjectConfiguration> = emptyMap()
) {
    fun observe(): WatchObservation {
        val controls = controlFiles + ruleDirectories.filter { it.isDirectory }.flatMap { directory ->
            directory.listEntries().filter { it.fileName.endsWith(".lua") || it.fileName.endsWith(".yaml") }
        }
        val fingerprints = controls.associateWith { path ->
            if (path.isRegularFile) FileHandling.calculateHash(path) else null
        }
        return WatchObservation(WatchInputs(projects, excluded).capture(), fingerprints,
            (controls.map { it.parent } + ruleDirectories).filter { it.isDirectory }.toSet())
    }

    fun affected(changed: Set<String>): Set<String> {
        val affected = changed.toMutableSet()
        do {
            val added = projects.filter { it.dependencies.any(affected::contains) }.map { it.name }
            val previousSize = affected.size
            affected.addAll(added)
        } while (affected.size != previousSize)
        return affected
    }
}

internal data class WatchObservation(
    val inputs: Map<String, WatchSnapshot>, val controls: Map<MPPPath, Long?>, val controlDirectories: Set<MPPPath>
) {
    val directories: Set<MPPPath> get() = controlDirectories + inputs.values.flatMap { it.directories }
    val problems: Set<String> get() = inputs.values.flatMap { it.problems }.toSet()

    fun changedProjects(previous: WatchObservation): Set<String> = inputs.filter { (name, snapshot) ->
        previous.inputs[name]?.let { snapshot.changesSince(it).isNotEmpty() } ?: true
    }.keys
}

internal data class WatchProjectConfiguration(val snapshot: ConfigurationSnapshot, val file: MPPPath?)
