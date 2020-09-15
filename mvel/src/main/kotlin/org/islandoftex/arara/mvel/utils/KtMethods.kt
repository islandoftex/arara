// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import kotlin.time.ExperimentalTime

/**
 * Implements some auxiliary methods for runtime evaluation.
 *
 * @author Island of TeX
 * @version 6.0
 * @since 6.0
 */
@ExperimentalTime
object KtMethods {
    /**
     * A map of rule method names to method pointers.
     */
    @JvmStatic
    val ruleMethods: Map<String, Any> by lazy {
        val map = conditionalMethods.toMutableMap()
        try {
            // TODO: remove reflection
            val methodsKotlin = Class.forName("org.islandoftex.arara.mvel.utils.KtRuleMethods").methods
            listOf("halt", "getOriginalFile", "getOriginalReference",
                    "trimSpaces", "getBasename", "getFiletype", "replicatePattern",
                    "throwError", "getSession", "buildString", "getCommand",
                    "getCommandWithWorkingDirectory", "isVerboseMode",
                    "showMessage", "isOnPath", "unsafelyExecuteSystemCommand",
                    "listFilesByExtensions", "listFilesByPatterns",
                    "writeToFile", "readFromFile", "isSubdirectory",
                    "isEmpty", "isNotEmpty", "isTrue", "isFalse",
                    "isWindows", "isLinux", "isMac", "isUnix", "isCygwin",
                    "checkClass", "isString", "isList", "isMap", "isBoolean"
            ).forEach { name: String ->
                map[name] = methodsKotlin.first { it.name == name }
            }
        } catch (exception: Exception) {
            // quack, quack, quack
        }
        map
    }

    /**
     * Get conditional methods.
     *
     * @return A map of method names to method pointers.
     */
    @JvmStatic
    val conditionalMethods: Map<String, Any> by lazy {
        val map = mutableMapOf<String, Any>()
        try {
            val methodsKotlin = ConditionalMethods::class.java.methods
            listOf("exists", "missing", "changed", "unchanged",
                    "found", "toFile", "showDropdown", "showInput",
                    "showOptions", "currentFile", "loadClass", "loadObject"
            ).forEach { name: String ->
                map[name] = methodsKotlin.first { it.name == name }
            }
        } catch (exception: java.lang.Exception) {
            // quack, quack, quack
        }
        map
    }
}
