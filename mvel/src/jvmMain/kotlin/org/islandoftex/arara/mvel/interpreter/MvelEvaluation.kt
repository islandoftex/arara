// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader

/** User templates may invoke arbitrary JVM methods, so their runtime failures share one boundary. */
@Suppress("TooGenericExceptionCaught")
internal inline fun <T> withMvelErrorHandling(
    header: Boolean = false, message: () -> String, evaluate: () -> T
): T = try {
    evaluate()
} catch (exception: RuntimeException) {
    throw if (header) AraraExceptionWithHeader(message(), exception) else AraraException(message(), exception)
}
