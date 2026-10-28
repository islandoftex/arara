// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.Environment

/** Operating-system predicates and platform-specific value selection. */
object OperatingSystemRuleHelpers {
    /**
     * Checks if Windows is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isWindows(): Boolean = Environment.checkOS(Environment.SupportedOS.WINDOWS)

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isCygwin(): Boolean = Environment.checkOS(Environment.SupportedOS.CYGWIN)

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isLinux(): Boolean = Environment.checkOS(Environment.SupportedOS.LINUX)

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isMac(): Boolean = Environment.checkOS(Environment.SupportedOS.MACOS)

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isUnix(): Boolean = Environment.checkOS(Environment.SupportedOS.UNIX)

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isWindows(yes: Any?, no: Any?): Any? =
        if (Environment.checkOS(Environment.SupportedOS.WINDOWS)) yes else no

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isCygwin(yes: Any?, no: Any?): Any? =
        if (Environment.checkOS(Environment.SupportedOS.CYGWIN)) yes else no

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isLinux(yes: Any?, no: Any?): Any? =
        if (Environment.checkOS(Environment.SupportedOS.LINUX)) yes else no

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isMac(yes: Any?, no: Any?): Any? =
        if (Environment.checkOS(Environment.SupportedOS.MACOS)) yes else no

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isUnix(yes: Any?, no: Any?): Any? =
        if (Environment.checkOS(Environment.SupportedOS.UNIX)) yes else no
}
