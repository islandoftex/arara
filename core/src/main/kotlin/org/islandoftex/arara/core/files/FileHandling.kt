// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Files
import java.nio.file.Path
import org.islandoftex.arara.api.AraraException

object FileHandling {
    /**
     * Gets a normalized path from the provided path.
     *
     * Please note that this is not the real path but a normalized absolute
     * path. The file represented by this does not have to exist.
     *
     * @param path The path to the file.
     * @return The normalized path from the provided path.
     */
    @Throws(AraraException::class)
    fun normalize(path: Path): Path = path.toAbsolutePath().normalize()

    /**
     * Resolve the sibling of a file with just its extension changed.
     *
     * @param path The path to use as base.
     * @param extension The extension.
     * @return The full file path to the sibling.
     */
    fun changeExtension(path: Path, extension: String): Path {
        val name = path.fileName.toString().substringBeforeLast('.') +
                ".$extension"
        return normalize(path.resolveSibling(name))
    }

    /**
     * Checks whether a directory is under a root directory.
     *
     * @param child Directory to be inspected.
     * @param parent Root directory.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubDirectory(child: Path, parent: Path): Boolean {
        return if (Files.isDirectory(child) && Files.isDirectory(parent)) {
            normalize(child).startsWith(normalize(parent))
        } else {
            false
        }
    }
}
