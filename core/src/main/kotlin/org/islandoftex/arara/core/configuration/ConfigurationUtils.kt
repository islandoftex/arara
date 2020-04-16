// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.file.Path
import java.nio.file.Paths
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.core.files.FileHandling
import org.islandoftex.arara.core.localization.LanguageController

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

    /**
     * The canonical absolute application path.
     *
     * Please note that this might return wrong results if accessed by a
     * front-end that accesses the `core` library from another location.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val applicationPath: Path
        @Throws(AraraException::class)
        get() {
            try {
                var path = this::class.java.protectionDomain.codeSource
                        .location.path
                path = URLDecoder.decode(path, "UTF-8")
                return FileHandling.normalize(Paths.get(File(path).toURI()).parent)
            } catch (exception: UnsupportedEncodingException) {
                throw AraraException(
                        LanguageController.messages.ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION,
                        exception
                )
            }
        }
}
