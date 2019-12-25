// SPDX-License-Identifier: BSD-3-Clause

package org.islandoftex.arara.filehandling

import kotlinx.serialization.Serializable

/**
 * The XML database model, which keeps track on file changes. I am using the
 * Simple framework to marshall and unmarshall objects and XML files.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
@Serializable
data class Database(
        // the whole database is implemented as a map, where
        // the key is the absolute canonical file and the value
        // is its corresponding CRC32 hash; the XML map is done
        // inline, so it does not clutter the output a lot
        val map: MutableMap<String, String> = mutableMapOf()
) : MutableMap<String, String> by map
