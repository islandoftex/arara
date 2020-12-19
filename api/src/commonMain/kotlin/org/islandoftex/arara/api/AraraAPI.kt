// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * Store information about arara's build.
 */
public expect object AraraAPI {
    /**
     * arara's version.
     */
    public val version: String
}
