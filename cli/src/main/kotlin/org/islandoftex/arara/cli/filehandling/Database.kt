// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.filehandling

import kotlinx.serialization.Serializable

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
    val map: MutableMap<String, String> = mutableMapOf()
) : MutableMap<String, String> by map
