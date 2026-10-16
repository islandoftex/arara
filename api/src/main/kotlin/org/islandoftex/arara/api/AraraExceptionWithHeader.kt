// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * An interpreter error marked to be presented with the rule header:
 * when such an error escapes a rule evaluation, the interpreter
 * rethrows it prefixed with the rule identifier and location, so the
 * terminal shows the rule context alongside the message.
 */
public class AraraExceptionWithHeader : AraraException {
    /**
     * Constructor that takes the message of the error.
     */
    public constructor(message: String) : super(message)
    /**
     * Constructor that takes the message of the error and the
     * underlying cause.
     */
    public constructor(message: String, exception: Exception) : super(message, exception)
}
