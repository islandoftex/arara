// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import korlibs.io.async.runBlockingNoJs
import korlibs.io.async.runBlockingNoSuspensions
import korlibs.io.async.use
import korlibs.io.file.VfsFile
import korlibs.io.file.VfsOpenMode
import korlibs.io.file.baseName
import korlibs.io.file.fullPathNormalized
import korlibs.io.file.isAbsolute
import korlibs.io.file.std.LocalVfs
import korlibs.io.stream.copyTo
import korlibs.io.stream.openAsync
import korlibs.time.DateTime
import org.islandoftex.arara.api.AraraIOException

public actual class MPPPath {
    internal var vfsFile: VfsFile = LocalVfs["."]

    public actual constructor(path: String) {
        vfsFile = LocalVfs[path]
    }

    public constructor(initPath: MPPPath) {
        vfsFile = initPath.vfsFile
    }

    public actual val isAbsolute: Boolean =
        runBlockingNoSuspensions {
            vfsFile.pathInfo.isAbsolute()
        }

    public actual val fileName: String = vfsFile.baseName

    public actual val fileSize: Long
        get() = runBlockingNoJs {
            vfsFile.size()
        }

    public actual val lastModified: DateTime
        get() = runBlockingNoJs {
            vfsFile.stat().modifiedTime
        }

    public actual val parent: MPPPath = MPPPath(vfsFile.parent.path)

    public actual val exists: Boolean =
        runBlockingNoJs { vfsFile.exists() }

    public actual val isDirectory: Boolean =
        runBlockingNoJs { vfsFile.isDirectory() }

    public actual val isRegularFile: Boolean =
        runBlockingNoJs { vfsFile.isFile() }

    public actual fun startsWith(p: MPPPath): Boolean =
        vfsFile.path.startsWith(p.toString())

    // TODO: implement proper normalization
    public actual fun normalize(): MPPPath = MPPPath(
        LocalVfs[vfsFile.fullPathNormalized].absolutePath
            .replace("/./", "/")
            .replace("//", "/")
    )

    public actual fun resolve(p: String): MPPPath =
        MPPPath(vfsFile[p].path)

    public actual fun resolve(p: MPPPath): MPPPath =
        resolve(p.toString())

    public actual fun resolveSibling(p: String): MPPPath =
        MPPPath(vfsFile.parent[p].path)

    public actual fun resolveSibling(p: MPPPath): MPPPath =
        MPPPath(vfsFile.parent[p.toString()].path)

    @Throws(AraraIOException::class)
    public actual fun readLines(): List<String> =
        runBlockingNoJs {
            vfsFile.readLines()
        }.toList()

    @Throws(AraraIOException::class)
    public actual fun readText(): String =
        runBlockingNoJs {
            vfsFile.readString()
        }

    @Throws(AraraIOException::class)
    public actual fun writeText(text: String, append: Boolean) {
        runBlockingNoJs {
            if (isRegularFile) {
                val openMode = if (append)
                    VfsOpenMode.APPEND
                else
                // if not appending choose the same open mode
                // korio would use instead
                    VfsOpenMode.CREATE_OR_TRUNCATE
                vfsFile.vfs.open(vfsFile.absolutePath, openMode)
                    .use {
                        text.openAsync().copyTo(this)
                    }
            } else {
                throw AraraIOException("Can only write text to files.")
            }
        }
    }

    override fun toString(): String = vfsFile.path

    override fun hashCode(): Int = vfsFile.path.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MPPPath

        if (vfsFile != other.vfsFile) return false
        if (isAbsolute != other.isAbsolute) return false
        if (fileName != other.fileName) return false
        if (parent != other.parent) return false
        if (exists != other.exists) return false
        if (isDirectory != other.isDirectory) return false
        if (isRegularFile != other.isRegularFile) return false

        return true
    }

    public actual operator fun div(p: String): MPPPath = resolve(p)
    public actual operator fun div(p: MPPPath): MPPPath = resolve(p.vfsFile.path)
}
