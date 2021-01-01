// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.std.localVfs
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.AraraIOException

public actual class MPPPath {
    internal val path: Path
    internal val vfsFile: VfsFile

    public actual constructor(path: String) {
        this.path = Paths.get(path)
        vfsFile = localVfs(this.path.toAbsolutePath().normalize().toString())
    }

    public constructor(initPath: Path) {
        path = initPath
        vfsFile = localVfs(path.toAbsolutePath().normalize().toString())
    }

    public constructor(initPath: MPPPath) {
        path = initPath.path
        vfsFile = localVfs(path.toAbsolutePath().normalize().toString())
    }

    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = path.isAbsolute

    public actual val fileName: String
        get() = vfsFile.baseName
    public actual val parent: MPPPath
        get() = this.takeIf { path == path.root } ?: MPPPath(path.parent)

    public actual val exists: Boolean
        get() = runBlockingNoJs { vfsFile.exists() }
    public actual val isDirectory: Boolean
        get() = runBlockingNoJs { vfsFile.isDirectory() }
    public actual val isRegularFile: Boolean
        get() = runBlockingNoJs { vfsFile.isFile() }

    public actual fun startsWith(p: MPPPath): Boolean =
        path.startsWith(p.path)

    public actual fun endsWith(p: MPPPath): Boolean =
        path.endsWith(p.path)

    public actual fun normalize(): MPPPath =
            MPPPath(path.toAbsolutePath().normalize())

    public actual fun resolve(p: String): MPPPath =
            MPPPath(vfsFile[p].absolutePath)

    public actual fun resolve(p: MPPPath): MPPPath =
            MPPPath(path.resolve(p.path))

    public actual fun resolveSibling(p: String): MPPPath =
            MPPPath(vfsFile.parent[p].absolutePath)

    public actual fun resolveSibling(p: MPPPath): MPPPath =
            MPPPath(path.resolveSibling(p.path))

    public actual fun toAbsolutePath(): MPPPath =
            MPPPath(path.toAbsolutePath())

    public fun toJVMPath(): Path = path

    public actual fun readLines(): List<String> = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.readLines().toList()
        else
            throw AraraIOException("Can only read lines from files.")
    }

    public actual fun readText(): String = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.readString()
        else
            throw AraraIOException("Can only read text from files.")
    }

    public actual fun writeText(text: String): Unit = runBlockingNoJs {
        if (isRegularFile)
            vfsFile.writeString(text)
        else
            throw AraraIOException("Can only write text to files.")
    }

    override fun toString(): String = vfsFile.absolutePath

    override fun hashCode(): Int = vfsFile.absolutePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        if (vfsFile.absolutePath != vfsFile.absolutePath) return false

        return true
    }

    public actual operator fun div(p: String): MPPPath = resolve(p)
    public actual operator fun div(p: MPPPath): MPPPath = resolve(p)
    public operator fun div(p: Path): MPPPath = resolve(MPPPath(p))
}
