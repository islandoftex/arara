// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

import org.islandoftex.arara.api.AraraException

/** A project script failed to run or contained malformed fields. */
public class ProjectParseException : AraraException {
    public constructor(message: String) : super(message)
    public constructor(message: String, exception: Exception) : super(message, exception)
}
/** A parsed project specification violates an invocation-wide constraint. */
public class ProjectValidationException(s: String) : AraraException(s)
