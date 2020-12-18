// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

public actual class MPPPath : Path {
    internal val path: Path

    public constructor(pathString: String) {
        path = Paths.get(pathString)
    }

    public constructor(initPath: Path) {
        path = initPath
    }

    public actual val isAbsolute: Boolean
        @JvmName("mppIsAbsolute")
        get() = path.isAbsolute
    public override fun isAbsolute(): Boolean = isAbsolute

    public actual val fileName: String
        get() = path.fileName.toString()
    public actual val parent: MPPPath
        get() = MPPPath(path.parent)

    public actual val exists: Boolean
        get() = path.exists()
    public actual val isDirectory: Boolean
        get() = path.isDirectory()

    public actual fun startsWith(p: MPPPath): Boolean =
        path.startsWith(p.path)

    override fun startsWith(p: Path): Boolean =
            path.startsWith(p)

    public actual fun endsWith(p: MPPPath): Boolean =
        path.endsWith(p.path)

    override fun endsWith(p: Path): Boolean =
        path.endsWith(p)

    public actual override fun normalize(): MPPPath =
            MPPPath(path.normalize())

    override fun relativize(p: Path): Path =
            path.relativize(p)

    public actual override fun resolve(p: String): MPPPath =
            MPPPath(path.resolve(p))

    public actual fun resolve(p: MPPPath): MPPPath =
            MPPPath(path.resolve(p.path))

    override fun resolve(p: Path): Path =
            MPPPath(path.resolve(p))

    public actual override fun resolveSibling(p: String): MPPPath =
            MPPPath(path.resolveSibling(p))

    public actual fun resolveSibling(p: MPPPath): MPPPath =
            MPPPath(path.resolveSibling(p.path))

    override fun toUri(): URI = path.toUri()

    public actual override fun toAbsolutePath(): MPPPath =
            MPPPath(path.toAbsolutePath())

    override fun toRealPath(vararg options: LinkOption?): Path =
            path.toRealPath(*options)

    public fun toJVMPath(): Path = path

    override fun toString(): String = path.toString()

    override fun register(
        p0: WatchService?,
        p1: Array<out WatchEvent.Kind<*>>?,
        vararg p2: WatchEvent.Modifier?
    ): WatchKey =
            path.register(p0, p1, *p2)

    override fun getFileSystem(): FileSystem =
            path.fileSystem

    override fun getRoot(): Path =
            path.root

    override fun getFileName(): Path =
            path.fileName

    public override fun getParent(): Path =
            path.parent

    override fun getNameCount(): Int =
            path.nameCount

    override fun getName(p: Int): Path =
            path.getName(p)

    override fun subpath(i: Int, j: Int): Path =
            path.subpath(i, j)

    override fun hashCode(): Int = path.hashCode()
    override fun compareTo(other: Path?): Int =
            path.compareTo(other)
    override fun equals(other: Any?): Boolean =
            path == other
}
