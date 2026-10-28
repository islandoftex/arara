// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import java.nio.charset.CodingErrorAction
import java.nio.file.FileSystemLoopException
import java.nio.file.Path
import kotlin.jvm.Throws
import okio.BufferedSource
import okio.buffer
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.time.DateTime

/** Native implementation of the focused PathReading contract. */
internal class NativePathReading(private val identity: NativePathIdentity) : PathReading {
    /**
     * Read lines from the file specified at the current path. Fails with an
     * [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    override fun readLines(): List<String> {
        if (identity.exists && !identity.isRegularFile) throw AraraIOException("Can only read lines from files.")
        return read { source ->
            val decoder = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            java.io.InputStreamReader(source.inputStream(), decoder).buffered().readLines()
        }
    }

    /**
     * Read whole text from the file specified at the current path. Fails with
     * an [AraraIOException] exception if the file is a directory or access is
     * impossible.
     */
    @Throws(AraraIOException::class)
    override fun readText(): String {
        if (identity.exists && !identity.isRegularFile) throw AraraIOException("Can only read text from files.")
        return read { source -> source.inputStream().reader(Charsets.UTF_8).readText() }
    }

    /**
     * Absolute normalized backend path; never exports the provided spelling.
     */
    override fun toOkioPath(): okio.Path = identity.toOkioPath()

    /**
     * Strict lookup: missing files, cycles and inspection failures raise [AraraIOException].
     */
    @Throws(AraraIOException::class)
    override fun metadata(): PathMetadata = identity.io {
        val metadata = identity.resolvedMetadata().second
        PathMetadata(
            metadata.isRegularFile,
            metadata.isDirectory,
            metadata.size,
            metadata.lastModifiedAtMillis?.let(::DateTime)
        )
    }

    /**
     * The source belongs to this call and is closed after [block] returns.
     */
    @Throws(AraraIOException::class)
    override fun <T> read(block: (BufferedSource) -> T): T = identity.io {
        val (resolved, metadata) = identity.resolvedMetadata()
        if (!metadata.isRegularFile) throw AraraIOException("Can only read from files: ${identity.referencePath}")
        identity.fileSystem.source(resolved).buffer().use(block)
    }

    /**
     * Direct children from this backend, including directories and special files.
     */
    @Throws(AraraIOException::class)
    override fun listEntries(): List<MPPPath> = identity.io {
        identity.fileSystem.list(toOkioPath())
            .map { MPPPath(identity.referencePath.fileSystem.getPath(it.toString()), identity.fileSystem) }
    }

    /**
     * Follow directory links, detecting ancestor cycles without suppressing sibling aliases.
     */
    @Throws(AraraIOException::class)
    override fun listFiles(recursive: Boolean): List<MPPPath> = identity.io {
        val files = mutableListOf<MPPPath>()
        val ancestors = mutableSetOf<okio.Path>()

        fun visit(path: MPPPath, descend: Boolean) {
            val (resolved, metadata) = NativePathIdentity(path.providedPath, identity.fileSystem).resolvedMetadata()
            if (metadata.isRegularFile) {
                files.add(path)
            } else if (metadata.isDirectory && descend) {
                val canonical = identity.fileSystem.canonicalize(resolved)
                if (!ancestors.add(canonical)) throw FileSystemLoopException(path.toString())
                try {
                    identity.fileSystem.list(path.toOkioPath()).forEach { child ->
                        try {
                            visit(MPPPath(
                                identity.referencePath.fileSystem.getPath(child.toString()), identity.fileSystem
                            ), recursive)
                        } catch (_: java.io.FileNotFoundException) {
                            // Broken links and entries removed during traversal are not regular files.
                        }
                    }
                } finally {
                    ancestors.remove(canonical)
                }
            }
        }

        visit(identity.path(identity.providedPath), true)
        files.sortedBy { it.toString() }
    }
}
