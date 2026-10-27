// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.watch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project

class WatchChangesTest {

    @Test
    fun debounceUsesLastEditAndConsumptionPreservesSubsequentEdits() {
        var now = 0L
        val changes = WatchChanges(300) { now }
        changes.record(setOf("one"), false)
        now = 200_000_000
        changes.record(setOf("two"), true)
        now = 300_000_000
        assertNull(changes.take())
        now = 500_000_000
        assertEquals(WatchBatch(setOf("one", "two"), true), changes.take())
        assertNull(changes.take())
        changes.record(setOf("three"), false)
        now = 800_000_000
        assertEquals(WatchBatch(setOf("three"), false), changes.take())
    }

    @Test
    fun sourceChangesInvalidateTransitiveDependentsButNotUnrelatedProjects() {
        val root = MPPPath(".")
        val graph = WatchGraph(listOf(Project("a", root, emptySet()),
            Project("b", root, emptySet(), setOf("a")), Project("c", root, emptySet(), setOf("b")),
            Project("d", root, emptySet())), emptySet())
        assertEquals(setOf("a", "b", "c"), graph.affected(setOf("a")))
        assertEquals(setOf("b", "c"), graph.affected(setOf("b")))
    }

    @Test
    fun repairedScannerFailuresRequestReload() {
        val changes = WatchChanges(0)
        changes.failed("unavailable")
        assertEquals("unavailable", changes.issue())
        changes.failed(null)
        assertEquals(WatchBatch(emptySet(), true), changes.take())
    }
}
