// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.files.FileType

object ConfigurationUtils {
    /**
     * This map contains all file types that arara accepts
     * and their corresponding search patterns (for comments).
     */
    val defaultFileTypePatterns = mapOf(
            "tex" to "^\\s*%\\s+",
            "dtx" to "^\\s*%\\s+",
            "ltx" to "^\\s*%\\s+",
            "drv" to "^\\s*%\\s+",
            "ins" to "^\\s*%\\s+"
    )

    /**
     * List of default file types provided by arara.
     */
    val defaultFileTypes: List<FileType> by lazy {
        defaultFileTypePatterns
                .map { (extension, pattern) ->
                    org.islandoftex.arara.core.files.FileType(extension, pattern)
                }.toSet().toList()
    }
}
