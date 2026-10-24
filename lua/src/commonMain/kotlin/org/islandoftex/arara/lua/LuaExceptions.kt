// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException

/** A Lua project script failed to run or contained malformed fields. */
class ProjectParseException : AraraException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}
/** A parsed project specification violates an invocation-wide constraint. */
class ProjectValidationException(s: String) : AraraException(s)
