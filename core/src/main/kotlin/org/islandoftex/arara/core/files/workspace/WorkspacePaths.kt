// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.core.localization.LanguageController

internal val messages get() = LanguageController.messages
internal const val STATE = ".arara-workspace"
internal fun present(path: MPPPath): Boolean = Files.exists(path.toJVMPath(), NOFOLLOW_LINKS)
internal fun link(path: MPPPath): Boolean = Files.isSymbolicLink(path.toJVMPath())
internal fun relative(root: MPPPath, path: MPPPath): String =
    root.toJVMPath().relativize(path.toJVMPath()).joinToString("/")

internal fun destination(root: MPPPath, name: String): MPPPath {
    val raw = Path.of(name)
    val path = root.resolve(name).normalize()
    val traversal = raw.isAbsolute || raw.any { it.toString() == ".." }
    val outside = path == root || !path.startsWith(root) || path.startsWith(root.resolve(STATE))
    if (traversal || outside) {
        throw AraraException(messages.errorBuildInputInvalid(name))
    }
    return path
}

internal fun noLinkedParents(root: MPPPath, path: MPPPath) {
    var parent = path.parent
    while (parent.startsWith(root)) {
        if (link(parent)) throw AraraException(messages.errorBuildInputInvalid(parent.toString()))
        if (parent == root) break
        parent = parent.parent
    }
}

internal fun physical(path: MPPPath): Path {
    var ancestor = path.toJVMPath()
    val missing = mutableListOf<String>()
    while (!Files.exists(ancestor, NOFOLLOW_LINKS)) {
        missing += ancestor.fileName.toString()
        ancestor = ancestor.parent
    }
    return missing.asReversed().fold(ancestor.toRealPath()) { base, part -> base.resolve(part) }.normalize()
}

internal fun missingInput(source: MPPPath): Nothing =
    throw AraraException(messages.errorBuildInputMissing(source.toString()))

internal fun cyclicInput(source: MPPPath): Nothing =
    throw AraraException(messages.errorBuildLinkCycle(source.toString()))

internal fun invalidInput(source: MPPPath): Nothing =
    throw AraraException(messages.errorBuildInputInvalid(source.toString()))
