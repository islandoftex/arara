// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import org.islandoftex.arara.api.AraraException

/**
 * A general interpreter exception to be thrown with the intention of
 * formatting it with appropriate debug context.
 */
class AraraExceptionWithHeader : AraraException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
}
