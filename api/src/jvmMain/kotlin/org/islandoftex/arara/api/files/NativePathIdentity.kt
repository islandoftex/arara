// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.io.IOException
import java.nio.file.FileSystemLoopException
import java.nio.file.Path
import okio.FileMetadata
import okio.FileSystem
import okio.Path.Companion.toPath
import org.islandoftex.arara.api.AraraIOException

internal class NativePathIdentity(
    val providedPath: Path,
    val fileSystem: FileSystem,
    val suppliedSpelling: String = providedPath.toString()
) {
    val referencePath: Path = providedPath.toAbsolutePath().normalize()
    val exists: Boolean get() = probe { io { resolvedMetadata() }; true }
    val isDirectory: Boolean get() = probe { io { resolvedMetadata().second.isDirectory } }
    val isRegularFile: Boolean get() = probe { io { resolvedMetadata().second.isRegularFile } }
    fun path(value: Path): MPPPath = MPPPath(value, fileSystem)
    fun toOkioPath(): okio.Path = referencePath.toString().toPath()

    fun resolvedMetadata(): Pair<okio.Path, FileMetadata> {
        var path = toOkioPath()
        val visited = mutableSetOf<okio.Path>()
        while (true) {
            if (!visited.add(path)) throw FileSystemLoopException(path.toString())
            val metadata = fileSystem.metadata(path)
            val target = metadata.symlinkTarget ?: return path to metadata
            path = (if (target.isAbsolute) target else path.parent!!.resolve(target)).normalized()
        }
    }

    inline fun probe(block: () -> Boolean): Boolean = try {
        block()
    } catch (_: AraraIOException) {
        false
    } catch (_: SecurityException) {
        false
    }

    inline fun <T> io(operation: () -> T): T = try {
        operation()
    } catch (exception: IOException) {
        throw AraraIOException(exception.toString(), exception)
    }
}
