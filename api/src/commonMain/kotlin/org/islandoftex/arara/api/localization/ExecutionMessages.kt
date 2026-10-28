// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for execution messages. */
public interface ExecutionMessages {
    /**
     * The error shown when a system command fails for an unspecified
     * reason.
     */
    public val errorRunGenericException: String

    /**
     * The error shown when a system command is interrupted.
     */
    public val errorRunInterruptedException: String

    /**
     * The error shown when a system command reports an invalid exit
     * value.
     */
    public val errorRunInvalidExitValueException: String

    /**
     * The error shown when a system command fails due to an IO problem.
     */
    public val errorRunIoException: String

    /**
     * The error shown when a system command reaches the configured
     * timeout.
     */
    public val errorRunTimeoutException: String

    /**
     * The error shown when a session key that is about to be read does
     * not exist.
     */
    public fun errorSessionObtainUnknownKey(key: String): String

    /**
     * The error shown when a session key that is about to be removed
     * does not exist.
     */
    public fun errorSessionRemoveUnknownKey(key: String): String
}
