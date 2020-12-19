// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.islandoftex.arara.api.AraraIOException

public actual class MPPPath {
    internal val path: Path

    public constructor(pathString: String) {
        path = Paths.get(pathString)
    }

    public constructor(initPath: Path) {
        path = initPath
    }

    public constructor(initPath: MPPPath) {
        path = initPath.path
    }

    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = path.isAbsolute

    public actual val fileName: String
        get() = path.fileName.toString()
    public actual val parent: MPPPath
        get() = this.takeIf { path == path.root } ?: MPPPath(path.parent)

    public actual val exists: Boolean
        get() = path.exists()
    public actual val isDirectory: Boolean
        get() = path.isDirectory()
    public actual val isRegularFile: Boolean
        get() = path.isRegularFile()

    public actual fun startsWith(p: MPPPath): Boolean =
        path.startsWith(p.path)

    public actual fun endsWith(p: MPPPath): Boolean =
        path.endsWith(p.path)

    public actual fun normalize(): MPPPath =
            MPPPath(path.toAbsolutePath().normalize())

    public actual fun resolve(p: String): MPPPath =
            MPPPath(path.resolve(p))

    public actual fun resolve(p: MPPPath): MPPPath =
            MPPPath(path.resolve(p.path))

    public actual fun resolveSibling(p: String): MPPPath =
            MPPPath(path.resolveSibling(p))

    public actual fun resolveSibling(p: MPPPath): MPPPath =
            MPPPath(path.resolveSibling(p.path))

    public actual fun toAbsolutePath(): MPPPath =
            MPPPath(path.toAbsolutePath())

    public fun toJVMPath(): Path = path

    public actual fun readLines(): List<String> =
            if (isRegularFile)
                path.readLines()
            else
                throw AraraIOException("Can only read lines from files.")

    public actual fun readText(): String =
            if (isRegularFile)
                path.readText()
            else
                throw AraraIOException("Can only read text from files.")

    public actual fun writeText(text: String): Unit =
            if (isRegularFile)
                path.writeText(text)
            else
                throw AraraIOException("Can only write text to files.")

    override fun toString(): String = path.toString()

    override fun hashCode(): Int = path.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPPPath

        if (path != other.path) return false

        return true
    }

    public operator fun div(p: String): MPPPath =
            MPPPath(path / p)
    public operator fun div(p: Path): MPPPath =
            MPPPath(path / p)
    public operator fun div(p: MPPPath): MPPPath =
            MPPPath(path / p.path)
}
