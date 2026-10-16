// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import org.eclipse.jgit.ignore.FastIgnoreRule
import org.eclipse.jgit.ignore.IgnoreNode
import org.islandoftex.arara.api.files.MPPPath

/** Git rules retain directory scope; Docker rules apply from the source root. */
internal class InputIgnores(private val root: MPPPath) {
    private val git = mutableMapOf<MPPPath, List<FastIgnoreRule>>()
    private val docker = dockerRules(root.resolve(".dockerignore"))

    fun excluded(path: MPPPath, directory: Boolean): Boolean =
        excludedByGit(path, directory) || excludedByDocker(path, directory)

    fun excludedByGit(path: MPPPath, directory: Boolean): Boolean {
        val components = relative(root, path).split('/')
        var parent = root
        val scopes = mutableListOf<Pair<MPPPath, List<FastIgnoreRule>>>()
        for ((index, component) in components.withIndex()) {
            scopes += parent to git.getOrPut(parent) { gitRules(parent.resolve(".gitignore")) }
            val current = parent.resolve(component)
            val isDirectory = index < components.lastIndex || directory
            // Git cannot re-include files whose parent is excluded.
            if (gitExcluded(scopes, current, isDirectory)) return true
            parent = current
        }
        return false
    }

    private fun excludedByDocker(path: MPPPath, directory: Boolean): Boolean {
        val components = relative(root, path).split('/')
        val ancestors = components.indices.map { components.take(it + 1).joinToString("/") }
        var excluded = false
        // Docker may re-include children of an excluded directory.
        for (rule in docker) {
            if (ancestors.dropLast(1).any { rule.isMatch(it, true, true) } ||
                rule.isMatch(ancestors.last(), directory, true)) excluded = rule.result
        }
        return excluded
    }

    private fun gitExcluded(
        scopes: List<Pair<MPPPath, List<FastIgnoreRule>>>, current: MPPPath, directory: Boolean
    ): Boolean {
        var excluded = false
        for ((base, rules) in scopes) for (rule in rules) {
            if (rule.isMatch(relative(base, current), directory)) excluded = rule.result
        }
        return excluded
    }

    private fun gitRules(file: MPPPath): List<FastIgnoreRule> =
        if (!file.exists || link(file)) emptyList() else file.read { input ->
            IgnoreNode().apply { parse(input.inputStream()) }.rules
        }

    private fun dockerRules(file: MPPPath): List<FastIgnoreRule> =
        if (!file.exists || link(file)) emptyList() else file.readLines().mapNotNull { line ->
            if (line.startsWith('#')) return@mapNotNull null
            var pattern = line.trim()
            val include = pattern.startsWith('!')
            if (include) pattern = pattern.drop(1)
            pattern = cleanDockerPattern(pattern.trim('/'))
            if (pattern.isEmpty() || pattern == ".") return@mapNotNull null
            FastIgnoreRule((if (include) "!" else "") + "/" + pattern).takeUnless { it.isEmpty }
        }

    private fun cleanDockerPattern(pattern: String): String =
        pattern.split('/').fold(mutableListOf<String>()) { parts, part ->
            when (part) {
                ".", "" -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
                else -> parts.add(part)
            }
            parts
        }.joinToString("/")

}
