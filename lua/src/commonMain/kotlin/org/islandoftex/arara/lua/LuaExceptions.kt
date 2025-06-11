// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua

import org.islandoftex.arara.api.AraraException

class ProjectParseException(
    s: String,
) : AraraException(s)

class ProjectValidationException(
    s: String,
) : AraraException(s)
