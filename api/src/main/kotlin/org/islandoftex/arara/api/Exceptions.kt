// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api

/**
 * Implements the specific exception model for arara.
 */
public open class AraraException : Exception {
    /**
     * The underlying exception, used to hold more details
     * on what really happened. Note that this mirrors the inherited
     * cause of the exception; prefer reading the cause in new code.
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
    public constructor(message: String, exception: Exception) :
            super(message, exception) {
        this.exception = exception
    }

    /**
     * Constructor. Takes the exception message and the underlying exception.
     * @param message The exception message.
     * @param throwable The underlying exception as generic throwable.
     */
    public constructor(message: String, throwable: Throwable) :
            this(message, RuntimeException(throwable))

    /**
     * Checks if there is an underlying exception with a message defined in
     * the current object. Note that the check includes the message: an
     * underlying exception without a message of its own counts as absent.
     * @return A boolean value indicating if the current object has an
     * underlying exception with a message.
     */
    public fun hasException(): Boolean = exception?.message != null
}

/**
 * When in [org.islandoftex.arara.api.configuration.ExecutionMode.SAFE_RUN],
 * this exception is thrown to indicate a violation of the safety contract
 * and that a certain feature was requested that is not allowed in safe run.
 */
public class SafeRunViolationException(msg: String) : AraraException(msg)

/**
 * When performing file system operations using
 * [org.islandoftex.arara.api.files.MPPPath] this exception is thrown if an
 * error occurs.
 */
public class AraraIOException : AraraException {
    /**
     * Constructor that takes the message of the failure.
     */
    public constructor(msg: String) : super(msg)

    /**
     * Constructor that takes the message of the failure and the
     * underlying cause.
     */
    public constructor(msg: String, exception: Exception) :
            super(msg, exception)
}
