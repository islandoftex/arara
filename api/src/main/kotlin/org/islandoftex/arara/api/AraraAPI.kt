// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * Store information about arara's build.
 */
object AraraAPI {
    /**
     * arara's version.
     */
    val version = AraraAPI::class.java.`package`.implementationVersion
            ?: "DEVELOPMENT BUILD"
}
