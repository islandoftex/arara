// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for configuration messages. */
public interface ConfigurationMessages {
    /** Class loading is unavailable in safe mode. */
    public val errorSafeModeClassLoading: String

    /** Arbitrary system commands are unavailable in safe mode. */
    public val errorSafeModeSystemCommand: String

    /** A dialog could not read an answer because terminal input ended. */
    public val errorDialogInputClosed: String

    /** A dialog was requested without a host renderer. */
    public val errorDialogsNotConfigured: String

    /** Invalid preamble prepend policy; argument: supplied value. */
    public fun errorConfigurationPrepend(value: String): String

    /**
     * The error shown when the configuration file cannot be parsed.
     */
    public val errorConfigurationGenericError: String

    /**
     * The error shown when the loop cap in the configuration is not a
     * positive number.
     */
    public val errorConfigurationLoopsInvalidRange: String

    /**
     * The error shown when the application path cannot be determined
     * due to an encoding problem.
     */
    public val errorGetApplicationPathEncodingException: String
}
