// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.AraraException

/**
 * A general interpreter exception to be thrown with the intention of
 * formatting it with appropriate debug context.
 */
internal class AraraExceptionWithHeader : AraraException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}

/**
 * Exception class to represent that the interpreter should stop for some
 * reason
 */
internal class HaltExpectedException(msg: String) : AraraException(msg)
