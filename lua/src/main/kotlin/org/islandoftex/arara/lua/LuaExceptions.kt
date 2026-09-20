// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException

class ProjectParseException(
    s: String,
    exception: Exception? = null
) : AraraException(s, exception ?: RuntimeException(s))

class ProjectValidationException(
    s: String,
    exception: Exception? = null
) : AraraException(s, exception ?: RuntimeException(s))
