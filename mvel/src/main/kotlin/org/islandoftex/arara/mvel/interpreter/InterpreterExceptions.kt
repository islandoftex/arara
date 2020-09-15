// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import org.islandoftex.arara.api.AraraException

// TODO: re-add internal keyword

/**
 * A general interpreter exception to be thrown with the intention of
 * formatting it with appropriate debug context.
 */
class AraraExceptionWithHeader : AraraException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}

/**
 * Exception class to represent that the interpreter should stop for some
 * reason
 */
class HaltExpectedException(msg: String) : AraraException(msg)
