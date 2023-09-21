// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.session.ExecutionStatus

/**
 * Exception class to represent that the interpreter should stop for some
 * reason
 */
internal class HaltExpectedException(
    msg: String,
    val status: ExecutionStatus,
) : AraraException(msg)
