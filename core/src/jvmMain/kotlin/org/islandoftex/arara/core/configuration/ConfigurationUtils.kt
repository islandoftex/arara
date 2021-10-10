// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.localization.LanguageController

actual object ConfigurationUtils {
    @JvmStatic
    actual val defaultFileTypePatterns = mapOf(
        "tex" to "^\\s*%\\s+",
        "dtx" to "^\\s*%\\s+",
        "ltx" to "^\\s*%\\s+",
        "drv" to "^\\s*%\\s+",
        "ins" to "^\\s*%\\s+"
    )

    @JvmStatic
    actual val defaultFileTypes: List<FileType> by lazy {
        defaultFileTypePatterns
            .map { (extension, pattern) ->
                org.islandoftex.arara.core.files.FileType(extension, pattern)
            }.distinct()
    }

    @JvmStatic
    actual val applicationPath: MPPPath
        @Throws(AraraException::class)
        get() = kotlin.runCatching {
            MPPPath(
                this::class.java.protectionDomain.codeSource
                    .location.toURI().path
            ).parent.normalize()
        }.getOrElse {
            throw AraraException(
                LanguageController.messages.ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION,
                it
            )
        }
}
