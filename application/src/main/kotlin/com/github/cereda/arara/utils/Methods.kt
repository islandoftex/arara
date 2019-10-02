/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils

import com.github.cereda.arara.controller.ConfigurationController
import com.github.cereda.arara.controller.LanguageController
import com.github.cereda.arara.model.*
import java.io.File
import java.util.*

/**
 * Implements some auxiliary methods for runtime evaluation.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object Methods {
    // the language controller
    private val messages = LanguageController

    // the session controller
    val session = Session()

    /**
     * Gets the original file.
     *
     * @return The original file.
     */
    val originalFile: String
        get() {
            val file = ConfigurationController["execution.reference"] as File
            return file.name
        }

    /**
     * Gets the original reference.
     *
     * @return The original reference.
     */
    val originalReference: File
        get() = ConfigurationController["execution.reference"] as File

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isWindows: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("windows")

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isCygwin: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("cygwin")

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isLinux: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("linux")

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isMac: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("mac")

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isUnix: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("unix")

    /**
     * Checks if AIX is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isAIX: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("aix")

    /**
     * Checks if Irix is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isIrix: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("irix")

    /**
     * Checks if OS2 is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isOS2: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("os2")

    /**
     * Checks if Solaris is the underlying operating system.
     *
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    val isSolaris: Boolean
        @Throws(AraraException::class)
        get() = CommonUtils.checkOS("solaris")

    /**
     * Checks if the execution is in verbose mode.
     *
     * @return A boolean value indicating if the execution is in verbose mode.
     */
    val isVerboseMode: Boolean
        get() = ConfigurationController["execution.verbose"] as Boolean

    /**
     * Adds the rule methods to the provided map.
     *
     * @param map The map.
     */
    fun addRuleMethods(map: MutableMap<String, Any>) {
        addConditionalMethods(map)
        try {
            // TODO: check signatures
            map["getOriginalFile"] = Methods::class.java.getMethod("getOriginalFile")
            map["getOriginalReference"] = Methods::class.java.getMethod("getOriginalReference")
            map["isEmpty"] = Methods::class.java.getMethod("isEmpty", String::class.java)
            map["isNotEmpty"] = Methods::class.java.getMethod("isNotEmpty", String::class.java)
            map["isEmpty"] = Methods::class.java.getMethod("isEmpty", String::class.java, Any::class.java)
            map["isNotEmpty"] = Methods::class.java.getMethod("isNotEmpty", String::class.java, Any::class.java)
            map["isEmpty"] = Methods::class.java.getMethod("isEmpty", String::class.java, Any::class.java, Any::class.java)
            map["isNotEmpty"] = Methods::class.java.getMethod("isNotEmpty", String::class.java, Any::class.java, Any::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", String::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", String::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", String::class.java, Any::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", String::class.java, Any::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", String::class.java, Any::class.java, Any::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", String::class.java, Any::class.java, Any::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", String::class.java, Any::class.java, Any::class.java, Any::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", String::class.java, Any::class.java, Any::class.java, Any::class.java)
            map["trimSpaces"] = Methods::class.java.getMethod("trimSpaces", String::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", Boolean::class.javaPrimitiveType, Any::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", Boolean::class.javaPrimitiveType, Any::class.java)
            map["isTrue"] = Methods::class.java.getMethod("isTrue", Boolean::class.javaPrimitiveType, Any::class.java, Any::class.java)
            map["isFalse"] = Methods::class.java.getMethod("isFalse", Boolean::class.javaPrimitiveType, Any::class.java, Any::class.java)
            map["getBasename"] = Methods::class.java.getMethod("getBasename", File::class.java)
            map["getBasename"] = Methods::class.java.getMethod("getBasename", String::class.java)
            map["getFullBasename"] = Methods::class.java.getMethod("getFullBasename", File::class.java)
            map["getFullBasename"] = Methods::class.java.getMethod("getFullBasename", String::class.java)
            map["getFiletype"] = Methods::class.java.getMethod("getFiletype", File::class.java)
            map["getFiletype"] = Methods::class.java.getMethod("getFiletype", String::class.java)
            map["throwError"] = Methods::class.java.getMethod("throwError", String::class.java)
            map["getSession"] = Methods::class.java.getMethod("getSession")
            map["isWindows"] = Methods::class.java.getMethod("isWindows")
            map["isLinux"] = Methods::class.java.getMethod("isLinux")
            map["isMac"] = Methods::class.java.getMethod("isMac")
            map["isUnix"] = Methods::class.java.getMethod("isUnix")
            map["isAIX"] = Methods::class.java.getMethod("isAIX")
            map["isIrix"] = Methods::class.java.getMethod("isIrix")
            map["isOS2"] = Methods::class.java.getMethod("isOS2")
            map["isSolaris"] = Methods::class.java.getMethod("isSolaris")
            map["isCygwin"] = Methods::class.java.getMethod("isCygwin")
            map["isWindows"] = Methods::class.java.getMethod("isWindows", Any::class.java, Any::class.java)
            map["isLinux"] = Methods::class.java.getMethod("isLinux", Any::class.java, Any::class.java)
            map["isMac"] = Methods::class.java.getMethod("isMac", Any::class.java, Any::class.java)
            map["isUnix"] = Methods::class.java.getMethod("isUnix", Any::class.java, Any::class.java)
            map["isAIX"] = Methods::class.java.getMethod("isAIX", Any::class.java, Any::class.java)
            map["isIrix"] = Methods::class.java.getMethod("isIrix", Any::class.java, Any::class.java)
            map["isOS2"] = Methods::class.java.getMethod("isOS2", Any::class.java, Any::class.java)
            map["isSolaris"] = Methods::class.java.getMethod("isSolaris", Any::class.java, Any::class.java)
            map["isCygwin"] = Methods::class.java.getMethod("isCygwin", Any::class.java, Any::class.java)
            map["replicatePattern"] = Methods::class.java.getMethod("replicatePattern", String::class.java, List::class.java)
            map["buildString"] = Methods::class.java.getMethod("buildString", Array<Any>::class.java)
            map["addQuotes"] = Methods::class.java.getMethod("addQuotes", Any::class.java)
            map["getCommand"] = Methods::class.java.getMethod("getCommand", List::class.java)
            map["getCommand"] = Methods::class.java.getMethod("getCommand", Array<Any>::class.java)
            map["getTrigger"] = Methods::class.java.getMethod("getTrigger", String::class.java)
            map["getTrigger"] = Methods::class.java.getMethod("getTrigger", String::class.java, Array<Any>::class.java)
            map["checkClass"] = Methods::class.java.getMethod("checkClass", Class::class.java, Any::class.java)
            map["isString"] = Methods::class.java.getMethod("isString", Any::class.java)
            map["isList"] = Methods::class.java.getMethod("isList", Any::class.java)
            map["isMap"] = Methods::class.java.getMethod("isMap", Any::class.java)
            map["isBoolean"] = Methods::class.java.getMethod("isBoolean", Any::class.java)
            map["isVerboseMode"] = Methods::class.java.getMethod("isVerboseMode")
            map["showMessage"] = Methods::class.java.getMethod("showMessage", Int::class.javaPrimitiveType, String::class.java, String::class.java)
            map["showMessage"] = Methods::class.java.getMethod("showMessage", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java, String::class.java)
            map["isOnPath"] = Methods::class.java.getMethod("isOnPath", String::class.java)
            map["unsafelyExecuteSystemCommand"] = Methods::class.java.getMethod("unsafelyExecuteSystemCommand", Command::class.java)
            map["mergeVelocityTemplate"] = Methods::class.java.getMethod("mergeVelocityTemplate", File::class.java, File::class.java, Map::class.java)
            map["getCommandWithWorkingDirectory"] = Methods::class.java.getMethod("getCommandWithWorkingDirectory", String::class.java, List::class.java)
            map["getCommandWithWorkingDirectory"] = Methods::class.java.getMethod("getCommandWithWorkingDirectory", String::class.java, Array<Any>::class.java)
            map["getCommandWithWorkingDirectory"] = Methods::class.java.getMethod("getCommandWithWorkingDirectory", File::class.java, List::class.java)
            map["getCommandWithWorkingDirectory"] = Methods::class.java.getMethod("getCommandWithWorkingDirectory", File::class.java, Array<Any>::class.java)
            map["listFilesByExtensions"] = Methods::class.java.getMethod("listFilesByExtensions", File::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["listFilesByExtensions"] = Methods::class.java.getMethod("listFilesByExtensions", String::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["listFilesByPatterns"] = Methods::class.java.getMethod("listFilesByPatterns", File::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["listFilesByPatterns"] = Methods::class.java.getMethod("listFilesByPatterns", String::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["writeToFile"] = Methods::class.java.getMethod("writeToFile", File::class.java, String::class.java, Boolean::class.javaPrimitiveType)
            map["writeToFile"] = Methods::class.java.getMethod("writeToFile", File::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["writeToFile"] = Methods::class.java.getMethod("writeToFile", String::class.java, String::class.java, Boolean::class.javaPrimitiveType)
            map["writeToFile"] = Methods::class.java.getMethod("writeToFile", String::class.java, List::class.java, Boolean::class.javaPrimitiveType)
            map["readFromFile"] = Methods::class.java.getMethod("readFromFile", File::class.java)
            map["readFromFile"] = Methods::class.java.getMethod("readFromFile", String::class.java)
            map["isSubdirectory"] = Methods::class.java.getMethod("isSubdirectory", File::class.java)
        } catch (_: Exception) {
            // quack, quack, quack
        }
    }

    /**
     * Adds conditional methods to the provided map.
     *
     * @param map The map.
     */
    fun addConditionalMethods(map: MutableMap<String, Any>) {
        try {
            map["exists"] = Methods::class.java.getMethod("exists", String::class.java)
            map["exists"] = Methods::class.java.getMethod("exists", File::class.java)
            map["missing"] = Methods::class.java.getMethod("missing", String::class.java)
            map["missing"] = Methods::class.java.getMethod("missing", File::class.java)
            map["changed"] = Methods::class.java.getMethod("changed", String::class.java)
            map["changed"] = Methods::class.java.getMethod("changed", File::class.java)
            map["unchanged"] = Methods::class.java.getMethod("unchanged", String::class.java)
            map["unchanged"] = Methods::class.java.getMethod("unchanged", File::class.java)
            map["found"] = Methods::class.java.getMethod("found", String::class.java, String::class.java)
            map["found"] = Methods::class.java.getMethod("found", File::class.java, String::class.java)
            map["toFile"] = Methods::class.java.getMethod("toFile", String::class.java)
            map["showDropdown"] = Methods::class.java.getMethod("showDropdown", Int::class.javaPrimitiveType, String::class.java, String::class.java, Array<Any>::class.java)
            map["showDropdown"] = Methods::class.java.getMethod("showDropdown", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java, String::class.java, Array<Any>::class.java)
            map["showInput"] = Methods::class.java.getMethod("showInput", Int::class.javaPrimitiveType, String::class.java, String::class.java)
            map["showInput"] = Methods::class.java.getMethod("showInput", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java, String::class.java)
            map["showOptions"] = Methods::class.java.getMethod("showOptions", Int::class.javaPrimitiveType, String::class.java, String::class.java, Array<Any>::class.java)
            map["showOptions"] = Methods::class.java.getMethod("showOptions", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java, String::class.java, Array<Any>::class.java)
            map["currentFile"] = Methods::class.java.getMethod("currentFile")
            map["loadClass"] = Methods::class.java.getMethod("loadClass", File::class.java, String::class.java)
            map["loadClass"] = Methods::class.java.getMethod("loadClass", String::class.java, String::class.java)
            map["loadObject"] = Methods::class.java.getMethod("loadObject", File::class.java, String::class.java)
            map["loadObject"] = Methods::class.java.getMethod("loadObject", String::class.java, String::class.java)
        } catch (exception: Exception) {
            // quack, quack, quack
        }
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    fun isEmpty(string: String): Boolean {
        return string.isEmpty()
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @return A boolean value.
     */
    fun isNotEmpty(string: String): Boolean {
        return !isEmpty(string)
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or empty string.
     */
    fun isEmpty(string: String, yes: Any): Any {
        return if (isEmpty(string)) yes else ""
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or empty string.
     */
    fun isNotEmpty(string: String, yes: Any): Any {
        return if (isNotEmpty(string)) yes else ""
    }

    /**
     * Checks if the string is empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     */
    fun isEmpty(string: String, yes: Any, no: Any): Any {
        return if (isEmpty(string)) yes else no
    }

    /**
     * Checks if the string is not empty.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     */
    fun isNotEmpty(string: String, yes: Any, no: Any): Any {
        return if (isNotEmpty(string)) yes else no
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String): Boolean {
        return !isEmpty(string) && CommonUtils.checkBoolean(string)
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String): Boolean {
        return !isEmpty(string) && !CommonUtils.checkBoolean(string)
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any): Any {
        return if (isTrue(string)) yes else ""
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @return An object or an empty string.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any): Any {
        return if (isFalse(string)) yes else ""
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any, no: Any): Any {
        return if (isTrue(string)) yes else no
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string The string.
     * @param yes    Object to return if true.
     * @param no     Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any, no: Any): Any {
        return if (isFalse(string)) yes else no
    }

    /**
     * Checks if the string holds a true value.
     *
     * @param string   The string.
     * @param yes      Object to return if true.
     * @param no       Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isTrue(string: String, yes: Any, no: Any,
               fallback: Any): Any {
        return if (isEmpty(string)) fallback else if (isTrue(string)) yes else no
    }

    /**
     * Checks if the string holds a false value.
     *
     * @param string   The string.
     * @param yes      Object to return if true.
     * @param no       Object to return if false.
     * @param fallback Object to return if string is empty.
     * @return One of the three options.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isFalse(string: String, yes: Any, no: Any,
                fallback: Any): Any {
        return if (isEmpty(string)) fallback else if (isFalse(string)) yes else no
    }

    /**
     * Trim spaces from the string.
     *
     * @param string The string.
     * @return A trimmed string.
     */
    fun trimSpaces(string: String): String {
        return string.trim()
    }

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @return An object or an empty string.
     */
    fun isTrue(value: Boolean, yes: Any): Any {
        return if (value) yes else ""
    }

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @return An object or an empty string.
     */
    fun isFalse(value: Boolean, yes: Any): Any {
        return if (!value) yes else ""
    }

    /**
     * Checks if the expression resolves to true.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @param no    Object to return if false.
     * @return One of the two objects.
     */
    fun isTrue(value: Boolean, yes: Any, no: Any): Any {
        return if (value) yes else no
    }

    /**
     * Checks if the expression resolves to false.
     *
     * @param value The expression.
     * @param yes   Object to return if true.
     * @param no    Object to return if false.
     * @return One of the two objects.
     */
    fun isFalse(value: Boolean, yes: Any, no: Any): Any {
        return if (!value) yes else no
    }

    /**
     * Gets the basename.
     *
     * @param file The file.
     * @return The basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getBasename(file: File): String {
        return if (file.isFile) {
            CommonUtils.getBasename(file)
        } else {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_BASENAME_NOT_A_FILE,
                            file.name
                    )
            )
        }
    }

    /**
     * Gets the basename.
     *
     * @param filename The string.
     * @return The basename.
     */
    fun getBasename(filename: String): String {
        return CommonUtils.getBasename(filename)
    }

    /**
     * Gets the file type.
     *
     * @param file The provided file.
     * @return The file type.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getFiletype(file: File): String {
        return if (file.isFile) {
            CommonUtils.getFiletype(file)
        } else {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_FILETYPE_NOT_A_FILE,
                            file.name
                    )
            )
        }
    }

    /**
     * Gets the file type.
     *
     * @param filename The provided string.
     * @return The file type.
     */
    fun getFiletype(filename: String): String {
        return CommonUtils.getFiletype(filename)
    }

    /**
     * Replicates the pattern to each element of a list.
     *
     * @param pattern The pattern.
     * @param values  The list.
     * @return A list of strings containing the pattern applied to the list.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun replicatePattern(pattern: String,
                         values: List<Any>): List<Any> {
        return CommonUtils.replicateList(pattern, values)
    }

    /**
     * Throws an exception.
     *
     * @param text The text to be thrown as the exception message.
     * @throws AraraException The exception to be thrown by this method.
     */
    @Throws(AraraException::class)
    fun throwError(text: String) {
        throw AraraException(text)
    }

    /**
     * Checks if Windows is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isWindows(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("windows")) yes else no
    }

    /**
     * Checks if we are inside a Cygwin environment.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isCygwin(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("cygwin")) yes else no
    }

    /**
     * Checks if Linux is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isLinux(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("linux")) yes else no
    }

    /**
     * Checks if Mac is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isMac(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("mac")) yes else no
    }

    /**
     * Checks if Unix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isUnix(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("unix")) yes else no
    }

    /**
     * Checks if AIX is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isAIX(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("aix")) yes else no
    }

    /**
     * Checks if Irix is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isIrix(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("irix")) yes else no
    }

    /**
     * Checks if OS2 is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isOS2(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("os2")) yes else no
    }

    /**
     * Checks if Solaris is the underlying operating system.
     *
     * @param yes Object to return if true.
     * @param no  Object to return if false.
     * @return One of the two objects.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun isSolaris(yes: Any, no: Any): Any {
        return if (CommonUtils.checkOS("solaris")) yes else no
    }

    /**
     * Checks if the file exists according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun exists(extension: String): Boolean {
        return CommonUtils.exists(extension)
    }

    /**
     * Checks if the file is missing according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun missing(extension: String): Boolean {
        return !exists(extension)
    }

    /**
     * Checks if the file has changed, according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun changed(extension: String): Boolean {
        return CommonUtils.hasChanged(extension)
    }

    /**
     * Checks if the file is unchanged according to its extension.
     *
     * @param extension The extension.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun unchanged(extension: String): Boolean {
        return !changed(extension)
    }

    /**
     * Checks if the file exists.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    fun exists(filename: File): Boolean {
        return CommonUtils.exists(filename)
    }

    /**
     * Checks if the file is missing.
     *
     * @param filename The file.
     * @return A boolean value.
     */
    fun missing(filename: File): Boolean {
        return !exists(filename)
    }

    /**
     * Checks if the file has changed.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun changed(filename: File): Boolean {
        return CommonUtils.hasChanged(filename)
    }

    /**
     * Checks if the file is unchanged.
     *
     * @param filename The file.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun unchanged(filename: File): Boolean {
        return !changed(filename)
    }

    /**
     * Build a string based on an array of objects.
     *
     * @param objects Array of objects.
     * @return A string built from the array.
     */
    fun buildString(vararg objects: Any): String {
        return CommonUtils.generateString(*objects)
    }

    /**
     * Encloses the provided object into double quotes.
     *
     * @param object The object.
     * @return The object enclosed in double quotes.
     */
    fun addQuotes(`object`: Any): String {
        return CommonUtils.addQuotes(`object`)
    }

    /**
     * Checks if the file contains the regex, based on its extension.
     *
     * @param extension The extension.
     * @param regex     The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun found(extension: String, regex: String): Boolean {
        return CommonUtils.checkRegex(extension, regex)
    }

    /**
     * Checks if the file contains the provided regex.
     *
     * @param file  The file.
     * @param regex The regex.
     * @return A boolean value.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun found(file: File, regex: String): Boolean {
        return CommonUtils.checkRegex(file, regex)
    }

    /**
     * Gets the command based on a list of strings.
     *
     * @param elements The list of strings.
     * @return A command.
     */
    fun getCommand(elements: List<String>): Command {
        return Command(elements)
    }

    /**
     * Gets the command based on an array of objects.
     *
     * @param elements Array of objects.
     * @return A command.
     */
    fun getCommand(vararg elements: Any): Command {
        return Command(*elements)
    }

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as string.
     *
     * @param path     String path representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(path: String,
                                       vararg elements: Any): Command {
        val command = Command(*elements)
        command.workingDirectory = File(path)
        return command
    }

    /**
     * Gets the command based on an array of objects and with the provided
     * working directory as file.
     *
     * @param file     File representing the working directory.
     * @param elements Array of elements.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(file: File,
                                       vararg elements: Any): Command {
        val command = Command(*elements)
        command.workingDirectory = file
        return command
    }

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as string.
     *
     * @param path     String path representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(path: String,
                                       elements: List<String>): Command {
        val command = Command(elements)
        command.workingDirectory = File(path)
        return command
    }

    /**
     * Gets the command based on a list of strings and with the provided
     * working directory as file.
     *
     * @param file     File representing the working directory.
     * @param elements List of strings.
     * @return A command.
     */
    fun getCommandWithWorkingDirectory(file: File,
                                       elements: List<String>): Command {
        val command = Command(elements)
        command.workingDirectory = file
        return command
    }

    /**
     * Gets the trigger.
     *
     * @param action The action name.
     * @return The trigger.
     */
    fun getTrigger(action: String): Trigger {
        return Trigger(action, listOf())
    }

    /**
     * Gets the trigger.
     *
     * @param action     The action name.
     * @param parameters The trigger parameters.
     * @return A trigger.
     */
    fun getTrigger(action: String, vararg parameters: Any): Trigger {
        return Trigger(action, parameters.toList())
    }

    /**
     * Checks if the object is an intance of the provided class.
     *
     * @param clazz  The class.
     * @param object The object.
     * @return A boolean value.
     */
    fun checkClass(clazz: Class<*>, `object`: Any): Boolean {
        return CommonUtils.checkClass(clazz, `object`)
    }

    /**
     * Checks if the object is a string.
     *
     * @param object The object.
     * @return A boolean value.
     */
    fun isString(`object`: Any): Boolean {
        return checkClass(String::class.java, `object`)
    }

    /**
     * Checks if the object is a list.
     *
     * @param object The object.
     * @return A boolean value.
     */
    fun isList(`object`: Any): Boolean {
        return checkClass(List::class.java, `object`)
    }

    /**
     * Checks if the object is a map.
     *
     * @param object The object.
     * @return A boolean value.
     */
    fun isMap(`object`: Any): Boolean {
        return checkClass(Map::class.java, `object`)
    }

    /**
     * Checks if the object is a boolean.
     *
     * @param object The object.
     * @return A boolean value.
     */
    fun isBoolean(`object`: Any): Boolean {
        return checkClass(Boolean::class.java, `object`)
    }

    /**
     * Returns a file object based on the provided name.
     *
     * @param name The file name.
     * @return A file object.
     */
    fun toFile(name: String): File {
        return File(name)
    }

    /**
     * Shows the message.
     *
     * @param width Integer value, in pixels.
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     */
    fun showMessage(width: Int, type: Int,
                    title: String, text: String) {
        MessageUtils.showMessage(width, type, title, text)
    }

    /**
     * Shows the message. It relies on the default width.
     *
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     */
    fun showMessage(type: Int, title: String, text: String) {
        MessageUtils.showMessage(type, title, text)
    }

    /**
     * Shows a message with options presented as an array of buttons.
     *
     * @param width   Integer value, in pixels.
     * @param type    Type of message.
     * @param title   Title of the message.
     * @param text    Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    fun showOptions(width: Int, type: Int, title: String,
                    text: String, vararg buttons: Any): Int {
        return MessageUtils.showOptions(width, type, title, text, *buttons)
    }

    /**
     * Shows a message with options presented as an array of buttons. It relies
     * on the default width.
     *
     * @param type    Type of message.
     * @param title   Title of the message.
     * @param text    Text of the message.
     * @param buttons An array of objects to be presented as buttons.
     * @return The index of the selected button, starting from 1.
     */
    fun showOptions(type: Int, title: String,
                    text: String, vararg buttons: Any): Int {
        return MessageUtils.showOptions(type, title, text, *buttons)
    }

    /**
     * Shows a message with a text input.
     *
     * @param width Integer value, in pixels.
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(width: Int, type: Int,
                  title: String, text: String): String {
        return MessageUtils.showInput(width, type, title, text)
    }

    /**
     * Shows a message with a text input. It relies on the default width.
     *
     * @param type  Type of message.
     * @param title Title of the message.
     * @param text  Text of the message.
     * @return The string representing the input text.
     */
    fun showInput(type: Int, title: String, text: String): String {
        return MessageUtils.showInput(type, title, text)
    }

    /**
     * Shows a message with options presented as a dropdown list of elements.
     *
     * @param width    Integer value, in pixels.
     * @param type     Type of message.
     * @param title    Title of the message.
     * @param text     Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    fun showDropdown(width: Int, type: Int, title: String,
                     text: String, vararg elements: Any): Int {
        return MessageUtils.showDropdown(width, type, title, text, *elements)
    }

    /**
     * Shows a message with options presented as a dropdown list of elements. It
     * relies on the default width.
     *
     * @param type     Type of message.
     * @param title    Title of the message.
     * @param text     Text of the message.
     * @param elements An array of objects representing the elements.
     * @return The index of the selected element, starting from 1.
     */
    fun showDropdown(type: Int, title: String,
                     text: String, vararg elements: Any): Int {
        return MessageUtils.showDropdown(type, title, text, *elements)
    }

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    fun isOnPath(command: String): Boolean {
        return CommonUtils.isOnPath(command)
    }

    /**
     * Gets the full basename.
     *
     * @param file The file.
     * @return The full basename of the provided file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getFullBasename(file: File): String {
        return if (file.isFile) {
            CommonUtils.getFullBasename(file)
        } else {
            throw AraraException(
                    CommonUtils.ruleErrorHeader + messages.getMessage(
                            Messages.ERROR_BASENAME_NOT_A_FILE,
                            file.name
                    )
            )
        }
    }

    /**
     * Gets the full basename.
     *
     * @param name The string.
     * @return The full basename.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun getFullBasename(name: String): String {
        return getFullBasename(File(name))
    }

    /**
     * Unsafely executes a system command from the underlying operating system
     * and returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @return A pair containing the exit status and the system command output
     * as a string.
     */
    fun unsafelyExecuteSystemCommand(command: Command): Pair<Int, String> {
        return UnsafeUtils.executeSystemCommand(command)
    }

    /**
     * Merges the provided template with a context map and writes the result in
     * an output file. This method relies on Apache Velocity.
     *
     * @param input  The input file.
     * @param output The output file.
     * @param map    The context map.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun mergeVelocityTemplate(input: File, output: File,
                              map: Map<String, Any>) {
        VelocityUtils.mergeVelocityTemplate(input, output, map)
    }

    /**
     * Gets the file reference for the current directive. It is important to
     * observe that version 4.0 of arara replicates the directive when 'files'
     * is detected amongst the parameters, so each instance will have a
     * different reference.
     *
     * @return A file reference for the current directive.
     */
    fun currentFile(): File {
        return ConfigurationController["execution.directive.reference"] as File
    }

    /**
     * Loads a class from the provided file, potentially a Java archive.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(file: File, name: String): Pair<Int, Class<*>> {
        return ClassLoadingUtils.loadClass(file, name)
    }

    /**
     * Loads a class from the provided string reference, representing a file.
     *
     * @param ref  String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(ref: String, name: String): Pair<Int, Class<*>> {
        return ClassLoadingUtils.loadClass(File(ref), name)
    }

    /**
     * Loads a class from the provided file, instantiating it.
     *
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(file: File, name: String): Pair<Int, Any> {
        return ClassLoadingUtils.loadObject(file, name)
    }

    /**
     * Loads a class from the provided string reference, instantiating it.
     *
     * @param ref  String reference representing a file.
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(ref: String, name: String): Pair<Int, Any> {
        return ClassLoadingUtils.loadObject(File(ref), name)
    }

    /**
     * List all files from the provided directory according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param directory  The provided directory.
     * @param extensions The list of extensions.
     * @param recursive  A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByExtensions(directory: File,
                              extensions: List<String>, recursive: Boolean): List<File> {
        return FileSearchingUtils.listFilesByExtensions(
                directory,
                extensions,
                recursive
        )
    }

    /**
     * List all files from the provided string path according to the list of
     * extensions. The leading dot must be omitted, unless it is part of the
     * extension.
     *
     * @param path       The provided path as plain string.
     * @param extensions The list of extensions.
     * @param recursive  A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByExtensions(path: String,
                              extensions: List<String>, recursive: Boolean): List<File> {
        return FileSearchingUtils.listFilesByExtensions(
                File(path),
                extensions,
                recursive
        )
    }

    /**
     * List all files from the provided directory matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param directory The provided directory.
     * @param patterns  The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByPatterns(directory: File,
                            patterns: List<String>, recursive: Boolean): List<File> {
        return FileSearchingUtils.listFilesByPatterns(
                directory,
                patterns,
                recursive
        )
    }

    /**
     * List all files from the provided path matching the list of file
     * name patterns. Such list can contain wildcards.
     *
     * @param path      The provided path as plain string.
     * @param patterns  The list of file name patterns.
     * @param recursive A flag indicating whether the search is recursive.
     * @return A list of files.
     */
    fun listFilesByPatterns(path: String,
                            patterns: List<String>, recursive: Boolean): List<File> {
        return FileSearchingUtils.listFilesByPatterns(
                File(path),
                patterns,
                recursive
        )
    }

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param file   The file.
     * @param text   The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(file: File, text: String, append: Boolean): Boolean {
        return FileHandlingUtils.writeToFile(file, text, append)
    }

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     *
     * @param path   The path.
     * @param text   The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(path: String, text: String,
                    append: Boolean): Boolean {
        return FileHandlingUtils.writeToFile(File(path), text, append)
    }

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param file   The file.
     * @param lines  The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(file: File, lines: List<String>,
                    append: Boolean): Boolean {
        return FileHandlingUtils.writeToFile(file, lines, append)
    }

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     *
     * @param path   The path.
     * @param lines  The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    fun writeToFile(path: String, lines: List<String>,
                    append: Boolean): Boolean {
        return FileHandlingUtils.writeToFile(File(path), lines, append)
    }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param file The file.
     * @return A list of strings.
     */
    fun readFromFile(file: File): List<String> {
        return FileHandlingUtils.readFromFile(file)
    }

    /**
     * Reads the provided file into a list of strings.
     *
     * @param path The path.
     * @return A list of strings.
     */
    fun readFromFile(path: String): List<String> {
        return FileHandlingUtils.readFromFile(File(path))
    }

    /**
     * Checks whether a directory is under the project directory.
     *
     * @param directory The directory to be inspected.
     * @return Logical value indicating whether the directoy is under root.
     * @throws AraraException There was a problem with path retrieval.
     */
    @Throws(AraraException::class)
    fun isSubdirectory(directory: File): Boolean {
        return CommonUtils.isSubDirectory(directory, originalReference)
    }
}
