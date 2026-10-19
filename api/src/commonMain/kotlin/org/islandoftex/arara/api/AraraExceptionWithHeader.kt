// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * A general interpreter exception to be thrown with the intention of
 * formatting it with appropriate debug context.
 */
public class AraraExceptionWithHeader : AraraException {
    public constructor(message: String) : super(message)
    public constructor(message: String, exception: Exception) : super(message, exception)
}
