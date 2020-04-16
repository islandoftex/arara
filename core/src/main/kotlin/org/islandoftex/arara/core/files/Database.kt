// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import com.charleskorn.kaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Database
import org.islandoftex.arara.core.localization.LanguageController

/**
 * The database model, which keeps track on file changes.
 *
 * This database is a map because it maps files to hashes. So the key will
 * always be a file representation and the value always a string.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
data class Database(
    /**
     * The whole database is implemented as a map, where
     * the key is the absolute canonical file and the value
     * is its corresponding CRC32 hash.
     */
    private val map: MutableMap<String, Long> = mutableMapOf()
) : Database {
    /**
     * Check whether the database contains a file.
     *
     * @param path The path to a file. It should be implemented in a way that
     *   non-canonical paths are dealt with.
     * @return Whether the object represented by the path is in the database.
     */
    override fun contains(path: Path): Boolean =
            path.toAbsolutePath().toString() in map

    /**
     * Get the hash value associated with a file.
     *
     * @param path The path to a file. Should use the same mechanics as
     *   [contains].
     * @return The hash value associated with the file if any and `null` if
     *   the element is not in the database.
     */
    override fun get(path: Path): Long? = map[path.toAbsolutePath().toString()]

    /**
     * Set the hash value for a given file.
     *
     * @param path The path of a file. Try to resolve it to an absolute path.
     * @param hash The hash value of the file.
     */
    override fun set(path: Path, hash: Long) {
        map[path.toAbsolutePath().toString()] = hash
    }

    /**
     * Remove the entry associated with the key (file).
     *
     * @param path The file acting as key in the database.
     */
    override fun remove(path: Path) {
        map.remove(path.toAbsolutePath().toString())
    }

    /**
     * Save the database.
     *
     * @param path Where to save it.
     */
    @Throws(AraraException::class)
    override fun save(path: Path) {
        runCatching {
            val content = "!database\n" +
                    Yaml.default.stringify(serializer(), this)
            path.toFile().writeText(content)
        }.getOrElse {
            throw AraraException(
                    LanguageController.messages.ERROR_SAVE_COULD_NOT_SAVE_XML
                            .format(path.fileName.toString()), it
            )
        }
    }

    companion object {
        /**
         * Loads the YAML file representing the database.
         *
         * @return The database object.
         * @throws AraraException Something wrong happened, to be caught in the
         *   higher levels.
         */
        @Throws(AraraException::class)
        fun load(path: Path): Database {
            return if (!Files.exists(path)) {
                Database()
            } else {
                runCatching {
                    val text = path.toFile().readText()
                    if (!text.startsWith("!database"))
                        throw AraraException("Database should start with !database")
                    Yaml.default.parse(serializer(), text)
                }.getOrElse {
                    throw AraraException(LanguageController
                            .messages.ERROR_LOAD_COULD_NOT_LOAD_XML
                            .format(path.fileName.toString()), it
                    )
                }
            }
        }
    }
}
