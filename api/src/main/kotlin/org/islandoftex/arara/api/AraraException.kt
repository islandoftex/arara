// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * Implements the specific exception model for arara.
 */
public open class AraraException : Exception {
    /**
     * The underlying exception, used to hold more details
     * on what really happened
     */
    public val exception: Exception?

    /**
     * Constructor. Takes the exception message.
     * @param message The exception message.
     */
    public constructor(message: String) : super(message) {
        this.exception = null
    }

    /**
     * Constructor. Takes the exception message and the underlying exception.
     * @param message The exception message.
     * @param exception The underlying exception object.
     */
    public constructor(message: String, exception: Exception) : super(message) {
        this.exception = exception
    }

    /**
     * Constructor. Takes the exception message and the underlying exception.
     * @param message The exception message.
     * @param throwable The underlying exception as generic throwable.
     */
    public constructor(message: String, throwable: Throwable) : super(message) {
        this.exception = RuntimeException(throwable)
    }

    /**
     * Checks if there is an underlying exception defined in the current object.
     * @return A boolean value indicating if the current object has an
     * underlying exception.
     */
    public fun hasException(): Boolean = exception?.message != null
}
