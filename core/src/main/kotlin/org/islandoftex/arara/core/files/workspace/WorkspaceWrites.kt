// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files.workspace

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

internal fun writeAtomic(path: MPPPath, write: (MPPPath) -> Unit) {
    val temporary = Files.createTempFile(path.parent.toJVMPath(), ".arara-state-", ".tmp").toMPPPath()
    try {
        write(temporary)
        replaceAtomic(temporary, path)
    } finally {
        Files.deleteIfExists(temporary.toJVMPath())
    }
}

internal fun replaceAtomic(source: MPPPath, target: MPPPath) {
    try {
        Files.move(source.toJVMPath(), target.toJVMPath(), REPLACE_EXISTING, ATOMIC_MOVE)
    } catch (_: AtomicMoveNotSupportedException) {
        Files.move(source.toJVMPath(), target.toJVMPath(), REPLACE_EXISTING)
    }
}

internal fun removeStaged(path: MPPPath, kind: InputKind) {
    try {
        Files.deleteIfExists(path.toJVMPath())
    } catch (exception: DirectoryNotEmptyException) {
        if (kind != InputKind.DIRECTORY) throw exception
        // Generated files remain owned by the user; never remove them as stale inputs.
    }
}
