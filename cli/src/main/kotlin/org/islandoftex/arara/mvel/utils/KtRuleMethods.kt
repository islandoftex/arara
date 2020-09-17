// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.Environment.checkOS

@Suppress("unused", "TooManyFunctions")
object KtRuleMethods {
    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    @JvmStatic
    fun isOnPath(command: String): Boolean = Environment.isOnPath(command)

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(): Boolean = checkOS("windows")

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(): Boolean = checkOS("cygwin")

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(): Boolean = checkOS("linux")

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(): Boolean = checkOS("mac")

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(): Boolean = checkOS("unix")

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isWindows(yes: Any?, no: Any?): Any? = if (checkOS("windows")) yes else no

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isCygwin(yes: Any?, no: Any?): Any? = if (checkOS("cygwin")) yes else no

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isLinux(yes: Any?, no: Any?): Any? = if (checkOS("linux")) yes else no

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isMac(yes: Any?, no: Any?): Any? = if (checkOS("mac")) yes else no

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun isUnix(yes: Any?, no: Any?): Any? = if (checkOS("unix")) yes else no
}
