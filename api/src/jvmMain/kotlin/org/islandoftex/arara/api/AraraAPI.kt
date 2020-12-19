// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

public actual object AraraAPI {
    /**
     * arara's version.
     */
    public actual val version: String =
            AraraAPI::class.java.`package`.implementationVersion
                    ?: "DEVELOPMENT BUILD"
}
