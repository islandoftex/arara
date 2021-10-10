// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.async.runBlockingNoSuspensions
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.fullPathNormalized
import com.soywiz.korio.file.isAbsolute
import com.soywiz.korio.file.std.LocalVfs
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

    public actual val parent: MPPPath = MPPPath(vfsFile.parent.path)

    public actual val exists: Boolean =
        runBlockingNoJs { vfsFile.exists() }

    public actual val isDirectory: Boolean =
        runBlockingNoJs { vfsFile.isDirectory() }

    public actual val isRegularFile: Boolean =
        runBlockingNoJs { vfsFile.isFile() }

    public actual fun startsWith(p: MPPPath): Boolean =
        vfsFile.path.startsWith(p.toString())

    public actual fun endsWith(p: MPPPath): Boolean =
        vfsFile.path.endsWith(p.toString())

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

    public actual fun toAbsolutePath(): MPPPath =
        MPPPath(vfsFile.absolutePath)

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
    public actual fun writeText(text: String) {
        runBlockingNoJs {
            vfsFile.write(text.encodeToByteArray())
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

    public operator fun div(p: String): MPPPath = resolve(p)
    public operator fun div(p: MPPPath): MPPPath = resolve(p.vfsFile.path)
}
