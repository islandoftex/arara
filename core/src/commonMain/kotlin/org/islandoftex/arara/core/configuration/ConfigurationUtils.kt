// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath

/**
 * Utilities to be used when configuring arara.
 */
expect object ConfigurationUtils {
    /**
     * This map contains all file types that arara accepts
     * and their corresponding search patterns (for comments).
     */
    val defaultFileTypePatterns: Map<String, String>

    /**
     * List of default file types provided by arara.
     */
    val defaultFileTypes: List<FileType>

    /**
     * The canonical absolute application path.
     *
     * Please note that this might return wrong results if accessed by a
     * front-end that accesses the `core` library from another location.
     */
    val applicationPath: MPPPath
}
