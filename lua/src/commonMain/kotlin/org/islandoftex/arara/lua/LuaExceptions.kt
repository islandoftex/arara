// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException

class ProjectParseException : AraraException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}
class ProjectValidationException(s: String) : AraraException(s)
