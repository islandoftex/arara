// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import com.charleskorn.kaml.Yaml
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.Database
import org.islandoftex.arara.api.files.MPPPath
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
    override fun contains(path: MPPPath): Boolean =
            FileHandling.normalize(path.toJVMPath()).toString() in map

    /**
     * Get the hash value associated with a file.
     *
     * @param path The path to a file. Should use the same mechanics as
     *   [contains].
     * @return The hash value associated with the file if any and `null` if
     *   the element is not in the database.
     */
    override fun get(path: MPPPath): Long? =
            map[FileHandling.normalize(path.toJVMPath()).toString()]

    /**
     * Set the hash value for a given file.
     *
     * @param path The path of a file. Try to resolve it to an absolute path.
     * @param hash The hash value of the file.
     */
    override fun set(path: MPPPath, hash: Long) {
        map[FileHandling.normalize(path.toJVMPath()).toString()] = hash
    }

    /**
     * Remove the entry associated with the key (file).
     *
     * @param path The file acting as key in the database.
     * @throws NoSuchElementException exception if the [path] is not contained
     *   within the database.
     */
    @Throws(NoSuchElementException::class)
    override fun remove(path: MPPPath) {
        val normalPath = FileHandling.normalize(path.toJVMPath()).toString()
        if (normalPath in map)
            map.remove(normalPath)
        else
            throw NoSuchElementException("Attempt to remove non-existent path " +
                    "from database.")
    }

    /**
     * Save the database.
     *
     * @param path Where to save it.
     */
    @Throws(AraraException::class)
    override fun save(path: MPPPath) {
        runCatching {
            val content = "!database\n" +
                    Yaml.default.encodeToString(serializer(), this)
            path.toJVMPath().writeText(content)
        }.getOrElse {
            throw AraraException(
                    LanguageController.messages.ERROR_SAVE_COULD_NOT_SAVE_XML
                            .format(path.fileName), it
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
        fun load(path: MPPPath): Database {
            return if (!path.exists) {
                Database()
            } else {
                runCatching {
                    val text = path.toJVMPath().readText()
                    if (!text.startsWith("!database"))
                        throw AraraException("Database should start with !database")
                    Yaml.default.decodeFromString(serializer(), text)
                }.getOrElse {
                    throw AraraException(LanguageController
                            .messages.ERROR_LOAD_COULD_NOT_LOAD_XML
                            .format(path.fileName), it
                    )
                }
            }
        }
    }
}
