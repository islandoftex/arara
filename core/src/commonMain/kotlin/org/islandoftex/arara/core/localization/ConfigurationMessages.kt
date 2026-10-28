// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for configuration messages. */
interface ConfigurationMessages {
    /** Class loading is unavailable in safe mode. */
    val errorSafeModeClassLoading: String

    /** Arbitrary system commands are unavailable in safe mode. */
    val errorSafeModeSystemCommand: String

    /** A dialog could not read an answer because terminal input ended. */
    val errorDialogInputClosed: String

    /** A dialog was requested without a host renderer. */
    val errorDialogsNotConfigured: String

    /** Invalid preamble prepend policy; argument: supplied value. */
    fun errorConfigurationPrepend(value: String): String

    /**
     * The error shown when the configuration file cannot be parsed.
     */
    val errorConfigurationGenericError: String

    /**
     * The error shown when the loop cap in the configuration is not a
     * positive number.
     */
    val errorConfigurationLoopsInvalidRange: String

    /**
     * The error shown when the application path cannot be determined
     * due to an encoding problem.
     */
    val errorGetApplicationPathEncodingException: String
}
