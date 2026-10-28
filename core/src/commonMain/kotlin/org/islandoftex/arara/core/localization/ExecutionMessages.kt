// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for execution messages. */
interface ExecutionMessages {
    /**
     * The error shown when a system command fails for an unspecified
     * reason.
     */
    val errorRunGenericException: String

    /**
     * The error shown when a system command is interrupted.
     */
    val errorRunInterruptedException: String

    /**
     * The error shown when a system command reports an invalid exit
     * value.
     */
    val errorRunInvalidExitValueException: String

    /**
     * The error shown when a system command fails due to an IO problem.
     */
    val errorRunIoException: String

    /**
     * The error shown when a system command reaches the configured
     * timeout.
     */
    val errorRunTimeoutException: String

    /**
     * The error shown when a session key that is about to be read does
     * not exist.
     */
    fun errorSessionObtainUnknownKey(key: String): String

    /**
     * The error shown when a session key that is about to be removed
     * does not exist.
     */
    fun errorSessionRemoveUnknownKey(key: String): String
}
